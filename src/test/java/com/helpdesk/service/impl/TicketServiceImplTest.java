package com.helpdesk.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.helpdesk.dao.TicketDao;
import com.helpdesk.dao.UserDao;
import com.helpdesk.exception.BusinessRuleException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;
import com.helpdesk.model.enums.TicketPriority;
import com.helpdesk.model.enums.TicketStatus;
import com.helpdesk.service.TransactionTestSupport;
import com.helpdesk.service.validation.EntityValidator;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private TicketDao ticketDao;
    @Mock private UserDao userDao;

    private TicketServiceImpl service;
    private User author;
    private User technician;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(ticketDao, userDao,
                TransactionTestSupport.directTransactionManager(), new EntityValidator());
        author = user(1L, Role.USER);
        technician = user(2L, Role.TECHNICIAN);
    }

    private static User user(Long id, Role role) {
        User u = new User("Prénom" + id, "Nom" + id, "user" + id + "@helpdesk.local", role);
        u.setId(id);
        return u;
    }

    private Ticket ticket(Long id, TicketStatus status, User assignee) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setTitle("Imprimante en panne");
        t.setDescription("Elle n'imprime plus");
        t.setStatus(status);
        t.setPriority(TicketPriority.HIGH);
        t.setAuthor(author);
        t.setAssignee(assignee);
        t.setVersion(1L);
        return t;
    }

    // --- Création ---

    @Test
    void create_shouldForceOpenStatusAndIgnoreAssignee() {
        when(userDao.findById(1L)).thenReturn(Optional.of(author));
        when(ticketDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Ticket input = ticket(99L, TicketStatus.CLOSED, technician); // Tentative de "tricher"
        Ticket created = service.create(input, 1L);

        assertNull(created.getId());
        assertEquals(TicketStatus.OPEN, created.getStatus());
        assertNull(created.getAssignee());
        assertEquals(author, created.getAuthor());
    }

    @Test
    void create_shouldFailWhenAuthorDoesNotExist() {
        when(userDao.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.create(ticket(null, TicketStatus.OPEN, null), 42L));
        verify(ticketDao, never()).save(any());
    }

    // --- Statut ---

    @Test
    void changeStatus_shouldApplyAllowedTransition() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.IN_PROGRESS, technician)));

        Ticket result = service.changeStatus(10L, TicketStatus.RESOLVED);

        assertEquals(TicketStatus.RESOLVED, result.getStatus());
    }

    @Test
    void changeStatus_shouldRejectForbiddenTransition() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.OPEN, null)));

        assertThrows(BusinessRuleException.class, () -> service.changeStatus(10L, TicketStatus.CLOSED));
    }

    @Test
    void changeStatus_toInProgress_shouldRequireAssignee() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.OPEN, null)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                service.changeStatus(10L, TicketStatus.IN_PROGRESS));
        assertTrue(ex.getMessage().contains("technicien"));
    }

    // --- Attribution ---

    @Test
    void assign_shouldAssignTechnician() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.OPEN, null)));
        when(userDao.findById(2L)).thenReturn(Optional.of(technician));

        Ticket result = service.assign(10L, 2L);

        assertEquals(technician, result.getAssignee());
    }

    @Test
    void assign_shouldRejectNonTechnician() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.OPEN, null)));
        when(userDao.findById(1L)).thenReturn(Optional.of(author));

        assertThrows(BusinessRuleException.class, () -> service.assign(10L, 1L));
    }

    @Test
    void assign_shouldRejectClosedTicket() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.CLOSED, technician)));

        assertThrows(BusinessRuleException.class, () -> service.assign(10L, 2L));
        verifyNoInteractions(userDao);
    }

    // --- Modification / suppression ---

    @Test
    void update_shouldRejectClosedTicket() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.CLOSED, technician)));

        assertThrows(BusinessRuleException.class, () ->
                service.update(ticket(10L, TicketStatus.CLOSED, technician)));
    }

    @Test
    void update_shouldNotChangeStatusOrAssignee() {
        Ticket existing = ticket(10L, TicketStatus.OPEN, null);
        when(ticketDao.findByIdWithDetails(10L)).thenReturn(Optional.of(existing));

        Ticket data = ticket(10L, TicketStatus.CLOSED, technician);
        data.setTitle("Nouveau titre");
        Ticket updated = service.update(data);

        assertEquals("Nouveau titre", updated.getTitle());
        assertEquals(TicketStatus.OPEN, updated.getStatus());
        assertNull(updated.getAssignee());
    }

    @Test
    void delete_shouldRefuseTicketInProgress() {
        when(ticketDao.findByIdWithDetails(10L))
                .thenReturn(Optional.of(ticket(10L, TicketStatus.IN_PROGRESS, technician)));

        assertThrows(BusinessRuleException.class, () -> service.delete(10L));
        verify(ticketDao, never()).delete(any());
    }
}