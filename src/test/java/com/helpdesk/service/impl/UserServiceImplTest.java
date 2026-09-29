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
import com.helpdesk.exception.ConcurrentUpdateException;
import com.helpdesk.exception.InvalidDataException;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;
import com.helpdesk.service.TransactionTestSupport;
import com.helpdesk.service.validation.EntityValidator;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserDao userDao;
    @Mock private TicketDao ticketDao;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userDao, ticketDao,
                TransactionTestSupport.directTransactionManager(), new EntityValidator());
    }

    private static User user(Long id, String email, Role role) {
        User u = new User("Awa", "Diop", email, role);
        u.setId(id);
        u.setVersion(1L);
        return u;
    }

    @Test
    void create_shouldNormalizeEmailAndSave() {
        when(userDao.findByEmail("awa@helpdesk.local")).thenReturn(Optional.empty());
        when(userDao.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User created = service.create(new User(" Awa ", "Diop", "  AWA@Helpdesk.LOCAL ", Role.USER));

        assertEquals("awa@helpdesk.local", created.getEmail());
        assertEquals("Awa", created.getFirstName());
        verify(userDao).save(created);
    }

    @Test
    void create_shouldRejectDuplicateEmail() {
        when(userDao.findByEmail("awa@helpdesk.local"))
                .thenReturn(Optional.of(user(2L, "awa@helpdesk.local", Role.USER)));

        assertThrows(BusinessRuleException.class, () ->
                service.create(new User("Awa", "Diop", "awa@helpdesk.local", Role.USER)));
        verify(userDao, never()).save(any());
    }

    @Test
    void create_shouldRejectInvalidDataWithFieldErrors() {
        InvalidDataException ex = assertThrows(InvalidDataException.class, () ->
                service.create(new User("", "Diop", "pas-un-email", Role.USER)));

        assertTrue(ex.getErrors().containsKey("firstName"));
        assertTrue(ex.getErrors().containsKey("email"));
        verifyNoInteractions(userDao);
    }

    @Test
    void update_shouldAllowKeepingOwnEmail() {
        User existing = user(1L, "awa@helpdesk.local", Role.USER);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));
        when(userDao.findByEmail("awa@helpdesk.local")).thenReturn(Optional.of(existing));

        User data = user(1L, "awa@helpdesk.local", Role.TECHNICIAN);
        User updated = service.update(data);

        assertEquals(Role.TECHNICIAN, updated.getRole());
    }

    @Test
    void update_shouldRejectStaleVersion() {
        User existing = user(1L, "awa@helpdesk.local", Role.USER);
        existing.setVersion(3L);
        when(userDao.findById(1L)).thenReturn(Optional.of(existing));

        User staleData = user(1L, "awa@helpdesk.local", Role.USER); // version 1

        assertThrows(ConcurrentUpdateException.class, () -> service.update(staleData));
    }

    @Test
    void delete_shouldRefuseWhenUserHasTickets() {
        when(userDao.findById(1L)).thenReturn(Optional.of(user(1L, "awa@helpdesk.local", Role.USER)));
        when(ticketDao.countByUser(1L)).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> service.delete(1L));
        verify(userDao, never()).delete(any());
    }

    @Test
    void delete_shouldDeleteUserWithoutTickets() {
        User u = user(1L, "awa@helpdesk.local", Role.USER);
        when(userDao.findById(1L)).thenReturn(Optional.of(u));
        when(ticketDao.countByUser(1L)).thenReturn(0L);

        service.delete(1L);

        verify(userDao).delete(u);
    }
}