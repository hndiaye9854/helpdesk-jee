package com.helpdesk.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.helpdesk.dao.TicketDao;
import com.helpdesk.dao.TicketSearchCriteria;
import com.helpdesk.model.Ticket;
import com.helpdesk.persistence.EntityManagerProvider;

import jakarta.persistence.criteria.*;

public class TicketDaoJpa extends AbstractJpaDao<Ticket, Long> implements TicketDao {

    public TicketDaoJpa(EntityManagerProvider emProvider) {
        super(Ticket.class, emProvider);
    }

    /** Charge le ticket avec son auteur et son technicien en UNE seule requête. */
    @Override
    public Optional<Ticket> findByIdWithDetails(Long id) {
        return em().createQuery("""
                SELECT t FROM Ticket t
                JOIN FETCH t.author
                LEFT JOIN FETCH t.assignee
                WHERE t.id = :id
                """, Ticket.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<Ticket> findAll() {
        return search(TicketSearchCriteria.empty());
    }

    /** Recherche dynamique : seuls les critères renseignés sont appliqués. */
    @Override
    public List<Ticket> search(TicketSearchCriteria criteria) {
        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<Ticket> query = cb.createQuery(Ticket.class);
        Root<Ticket> ticket = query.from(Ticket.class);

        // Évite le problème N+1 : auteur et technicien chargés dans la même requête
        ticket.fetch("author", JoinType.INNER);
        ticket.fetch("assignee", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();
        if (criteria.hasTitle()) {
            predicates.add(cb.like(cb.lower(ticket.get("title")),
                    "%" + criteria.getTitle().trim().toLowerCase() + "%"));
        }
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(ticket.get("status"), criteria.getStatus()));
        }
        if (criteria.getPriority() != null) {
            predicates.add(cb.equal(ticket.get("priority"), criteria.getPriority()));
        }
        if (criteria.getAssigneeId() != null) {
            predicates.add(cb.equal(ticket.get("assignee").get("id"), criteria.getAssigneeId()));
        }

        query.select(ticket)
             .where(predicates.toArray(Predicate[]::new))
             .orderBy(cb.desc(ticket.get("createdAt")));

        return em().createQuery(query).getResultList();
    }

    @Override
    public long countByUser(Long userId) {
        return em().createQuery("""
                SELECT COUNT(t) FROM Ticket t
                WHERE t.author.id = :userId OR t.assignee.id = :userId
                """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}