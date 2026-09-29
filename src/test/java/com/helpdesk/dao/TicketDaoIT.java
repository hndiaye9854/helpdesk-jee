package com.helpdesk.dao;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.helpdesk.dao.impl.TicketDaoJpa;
import com.helpdesk.dao.impl.UserDaoJpa;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;
import com.helpdesk.model.enums.TicketPriority;
import com.helpdesk.model.enums.TicketStatus;

class TicketDaoIT extends AbstractDaoIT {

    private final UserDao userDao = new UserDaoJpa(tx);
    private final TicketDao ticketDao = new TicketDaoJpa(tx);

    private User author;
    private User technician;

    @BeforeEach
    void setUp() {
        tx.runInTransaction(() -> {
            author = userDao.save(new User("Awa", "Diop", "awa@helpdesk.local", Role.USER));
            technician = userDao.save(new User("Ibrahima", "Sow", "ibrahima@helpdesk.local", Role.TECHNICIAN));

            ticketDao.save(newTicket("Imprimante en panne", TicketPriority.HIGH, TicketStatus.OPEN, null));
            ticketDao.save(newTicket("Accès VPN impossible", TicketPriority.CRITICAL, TicketStatus.IN_PROGRESS, technician));
            ticketDao.save(newTicket("Écran qui clignote", TicketPriority.LOW, TicketStatus.OPEN, technician));
        });
    }

    private Ticket newTicket(String title, TicketPriority priority, TicketStatus status, User assignee) {
        Ticket t = new Ticket();
        t.setTitle(title);
        t.setDescription("Description de : " + title);
        t.setPriority(priority);
        t.setStatus(status);
        t.setAuthor(author);
        t.setAssignee(assignee);
        return t;
    }

    @Test
    void findByIdWithDetails_shouldLoadRelationsUsableOutsideTransaction() {
        Long id = tx.inTransaction(() ->
                ticketDao.search(TicketSearchCriteria.empty().title("VPN")).get(0).getId());

        Ticket ticket = tx.inTransaction(() -> ticketDao.findByIdWithDetails(id)).orElseThrow();

        // Transaction terminée : pas de LazyInitializationException grâce au JOIN FETCH
        assertEquals("Awa Diop", ticket.getAuthor().getFullName());
        assertEquals("Ibrahima Sow", ticket.getAssignee().getFullName());
    }

    @Test
    void search_withoutCriteria_shouldReturnAllTickets() {
        assertEquals(3, tx.inTransaction(() -> ticketDao.search(TicketSearchCriteria.empty())).size());
    }

    @Test
    void search_byTitle_shouldBeCaseInsensitiveAndPartial() {
        var result = tx.inTransaction(() -> ticketDao.search(TicketSearchCriteria.empty().title("imprim")));
        assertEquals(1, result.size());
    }

    @Test
    void search_byStatus() {
        var result = tx.inTransaction(() -> ticketDao.search(TicketSearchCriteria.empty().status(TicketStatus.OPEN)));
        assertEquals(2, result.size());
    }

    @Test
    void search_byPriority() {
        var result = tx.inTransaction(() -> ticketDao.search(TicketSearchCriteria.empty().priority(TicketPriority.CRITICAL)));
        assertEquals(1, result.size());
    }

    @Test
    void search_byAssignee() {
        var result = tx.inTransaction(() -> ticketDao.search(TicketSearchCriteria.empty().assigneeId(technician.getId())));
        assertEquals(2, result.size());
    }

    @Test
    void search_withCombinedCriteria() {
        var result = tx.inTransaction(() -> ticketDao.search(TicketSearchCriteria.empty()
                .assigneeId(technician.getId())
                .status(TicketStatus.OPEN)));
        assertEquals(1, result.size());
        assertEquals("Écran qui clignote", result.get(0).getTitle());
    }

    @Test
    void countByUser_shouldCountAuthoredAndAssignedTickets() {
        assertEquals(3, tx.inTransaction(() -> ticketDao.countByUser(author.getId())));
        assertEquals(2, tx.inTransaction(() -> ticketDao.countByUser(technician.getId())));
    }
}