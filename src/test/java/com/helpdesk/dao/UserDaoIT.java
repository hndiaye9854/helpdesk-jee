package com.helpdesk.dao;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.helpdesk.dao.impl.UserDaoJpa;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;

import jakarta.persistence.PersistenceException;

class UserDaoIT extends AbstractDaoIT {

    private final UserDao userDao = new UserDaoJpa(tx);

    @Test
    void save_shouldGenerateIdAndCreationDate() {
        User saved = tx.inTransaction(() ->
                userDao.save(new User("Awa", "Diop", "awa.diop@helpdesk.local", Role.USER)));

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void findByEmail_shouldBeCaseInsensitive() {
        tx.inTransaction(() -> userDao.save(new User("Awa", "Diop", "awa.diop@helpdesk.local", Role.USER)));

        var found = tx.inTransaction(() -> userDao.findByEmail("AWA.DIOP@helpdesk.local"));

        assertTrue(found.isPresent());
    }

    @Test
    void save_shouldRejectDuplicateEmail() {
        tx.inTransaction(() -> userDao.save(new User("Awa", "Diop", "dup@helpdesk.local", Role.USER)));

        assertThrows(PersistenceException.class, () -> tx.inTransaction(() ->
                userDao.save(new User("Moussa", "Fall", "dup@helpdesk.local", Role.USER))));
    }

    @Test
    void findByRole_shouldReturnOnlyTechnicians() {
        tx.runInTransaction(() -> {
            userDao.save(new User("Awa", "Diop", "awa@helpdesk.local", Role.USER));
            userDao.save(new User("Ibrahima", "Sow", "ibrahima@helpdesk.local", Role.TECHNICIAN));
        });

        var technicians = tx.inTransaction(() -> userDao.findByRole(Role.TECHNICIAN));

        assertEquals(1, technicians.size());
        assertEquals("Sow", technicians.get(0).getLastName());
    }
}