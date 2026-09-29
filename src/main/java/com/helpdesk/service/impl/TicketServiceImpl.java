package com.helpdesk.service.impl;

import java.util.List;
import java.util.Objects;

import com.helpdesk.dao.TicketDao;
import com.helpdesk.dao.TicketSearchCriteria;
import com.helpdesk.dao.UserDao;
import com.helpdesk.exception.BusinessRuleException;
import com.helpdesk.exception.ConcurrentUpdateException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.TicketPriority;
import com.helpdesk.model.enums.TicketStatus;
import com.helpdesk.persistence.TransactionManager;
import com.helpdesk.service.TicketService;
import com.helpdesk.service.validation.EntityValidator;

public class TicketServiceImpl implements TicketService {

    private final TicketDao ticketDao;
    private final UserDao userDao;
    private final TransactionManager tx;
    private final EntityValidator validator;

    public TicketServiceImpl(TicketDao ticketDao, UserDao userDao,
                             TransactionManager tx, EntityValidator validator) {
        this.ticketDao = ticketDao;
        this.userDao = userDao;
        this.tx = tx;
        this.validator = validator;
    }

    @Override
    public Ticket create(Ticket ticket, Long authorId) {
        return tx.inTransaction(() -> {
            User author = userDao.findById(authorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", authorId));

            // Le service impose l'état initial, quoi que contienne la requête
            ticket.setId(null);
            ticket.setAuthor(author);
            ticket.setAssignee(null);
            ticket.setStatus(TicketStatus.OPEN);
            if (ticket.getPriority() == null) {
                ticket.setPriority(TicketPriority.MEDIUM);
            }
            if (ticket.getTitle() != null) {
                ticket.setTitle(ticket.getTitle().trim());
            }

            validator.validate(ticket);
            return ticketDao.save(ticket);
        });
    }

    @Override
    public Ticket update(Ticket data) {
        if (data.getId() == null) {
            throw new IllegalArgumentException("L'identifiant est obligatoire pour une mise à jour");
        }
        return tx.inTransaction(() -> {
            Ticket existing = getOrThrow(data.getId());
            ensureNotClosed(existing);
            checkVersion(existing, data);

            // Seuls titre, description et priorité se modifient ici.
            // Statut et technicien ont leurs propres opérations, avec leurs règles.
            existing.setTitle(data.getTitle() != null ? data.getTitle().trim() : null);
            existing.setDescription(data.getDescription());
            existing.setPriority(data.getPriority());

            validator.validate(existing); // En cas d'échec : rollback, rien n'est écrit
            return existing;
        });
    }

    @Override
    public void delete(Long id) {
        tx.runInTransaction(() -> {
            Ticket ticket = getOrThrow(id);
            if (ticket.getStatus() == TicketStatus.IN_PROGRESS) {
                throw new BusinessRuleException(
                        "Le ticket #" + id + " est en cours de traitement et ne peut pas être supprimé");
            }
            ticketDao.delete(ticket);
        });
    }

    @Override
    public Ticket findById(Long id) {
        return tx.inTransaction(() -> getOrThrow(id));
    }

    @Override
    public List<Ticket> search(TicketSearchCriteria criteria) {
        return tx.inTransaction(() ->
                ticketDao.search(criteria != null ? criteria : TicketSearchCriteria.empty()));
    }

    @Override
    public Ticket changeStatus(Long ticketId, TicketStatus newStatus) {
        Objects.requireNonNull(newStatus, "Le nouveau statut est obligatoire");
        return tx.inTransaction(() -> {
            Ticket ticket = getOrThrow(ticketId);
            TicketStatus current = ticket.getStatus();

            if (current == newStatus) {
                return ticket; // Opération idempotente : rien à faire
            }
            if (!current.canTransitionTo(newStatus)) {
                throw new BusinessRuleException(
                        "Transition interdite : " + current.getLabel() + " → " + newStatus.getLabel());
            }
            if (newStatus == TicketStatus.IN_PROGRESS && ticket.getAssignee() == null) {
                throw new BusinessRuleException(
                        "Le ticket doit être attribué à un technicien avant d'être pris en charge");
            }
            ticket.setStatus(newStatus);
            return ticket;
        });
    }

    @Override
    public Ticket assign(Long ticketId, Long technicianId) {
        Objects.requireNonNull(technicianId, "Le technicien est obligatoire");
        return tx.inTransaction(() -> {
            Ticket ticket = getOrThrow(ticketId);
            ensureNotClosed(ticket);

            User technician = userDao.findById(technicianId)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", technicianId));
            if (!technician.isTechnician()) {
                throw new BusinessRuleException(technician.getFullName() + " n'est pas technicien");
            }
            ticket.setAssignee(technician);
            return ticket;
        });
    }

    // --- Méthodes internes ---

    /** Charge toujours le ticket AVEC auteur et technicien : utilisable ensuite dans les JSP. */
    private Ticket getOrThrow(Long id) {
        return ticketDao.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", id));
    }

    private void ensureNotClosed(Ticket ticket) {
        if (ticket.getStatus().isFinal()) {
            throw new BusinessRuleException(
                    "Le ticket #" + ticket.getId() + " est clôturé et ne peut plus être modifié");
        }
    }

    private void checkVersion(Ticket existing, Ticket data) {
        if (data.getVersion() != null && !data.getVersion().equals(existing.getVersion())) {
            throw new ConcurrentUpdateException("Le ticket");
        }
    }
}