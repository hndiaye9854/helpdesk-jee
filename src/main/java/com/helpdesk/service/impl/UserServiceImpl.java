package com.helpdesk.service.impl;

import java.util.List;

import com.helpdesk.dao.TicketDao;
import com.helpdesk.dao.UserDao;
import com.helpdesk.exception.BusinessRuleException;
import com.helpdesk.exception.ConcurrentUpdateException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;
import com.helpdesk.persistence.TransactionManager;
import com.helpdesk.service.UserService;
import com.helpdesk.service.validation.EntityValidator;

public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final TicketDao ticketDao;
    private final TransactionManager tx;
    private final EntityValidator validator;

    public UserServiceImpl(UserDao userDao, TicketDao ticketDao,
                           TransactionManager tx, EntityValidator validator) {
        this.userDao = userDao;
        this.ticketDao = ticketDao;
        this.tx = tx;
        this.validator = validator;
    }

    @Override
    public User create(User user) {
        normalize(user);
        validator.validate(user);
        return tx.inTransaction(() -> {
            ensureEmailIsAvailable(user.getEmail(), null);
            return userDao.save(user);
        });
    }

    @Override
    public User update(User data) {
        if (data.getId() == null) {
            throw new IllegalArgumentException("L'identifiant est obligatoire pour une mise à jour");
        }
        normalize(data);
        validator.validate(data);
        return tx.inTransaction(() -> {
            User existing = getOrThrow(data.getId());
            checkVersion(existing, data);
            ensureEmailIsAvailable(data.getEmail(), existing.getId());

            // On ne copie QUE les champs modifiables (jamais id, createdAt, version)
            existing.setFirstName(data.getFirstName());
            existing.setLastName(data.getLastName());
            existing.setEmail(data.getEmail());
            existing.setRole(data.getRole());
            return existing; // Entité gérée : Hibernate détecte les changements et fait l'UPDATE au commit
        });
    }

    @Override
    public void delete(Long id) {
        tx.runInTransaction(() -> {
            User user = getOrThrow(id);
            if (ticketDao.countByUser(id) > 0) {
                throw new BusinessRuleException(
                        "Impossible de supprimer " + user.getFullName() + " : des tickets lui sont associés");
            }
            userDao.delete(user);
        });
    }

    @Override
    public User findById(Long id) {
        return tx.inTransaction(() -> getOrThrow(id));
    }

    @Override
    public List<User> findAll() {
        return tx.inTransaction(userDao::findAll);
    }

    @Override
    public List<User> findTechnicians() {
        return tx.inTransaction(() -> userDao.findByRole(Role.TECHNICIAN));
    }

    // --- Méthodes internes ---

    private User getOrThrow(Long id) {
        return userDao.findById(id).orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    private void ensureEmailIsAvailable(String email, Long currentUserId) {
        userDao.findByEmail(email)
               .filter(other -> !other.getId().equals(currentUserId))
               .ifPresent(other -> {
                   throw new BusinessRuleException("L'email " + email + " est déjà utilisé");
               });
    }

    private void checkVersion(User existing, User data) {
        if (data.getVersion() != null && !data.getVersion().equals(existing.getVersion())) {
            throw new ConcurrentUpdateException("L'utilisateur");
        }
    }

    private void normalize(User user) {
        if (user.getEmail() != null) user.setEmail(user.getEmail().trim().toLowerCase());
        if (user.getFirstName() != null) user.setFirstName(user.getFirstName().trim());
        if (user.getLastName() != null) user.setLastName(user.getLastName().trim());
    }
}