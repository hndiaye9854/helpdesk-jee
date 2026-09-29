package dao;


import java.util.List;
import java.util.Optional;

import model.Ticket;

public interface TicketDao extends GenericDao<Ticket, Long> {
    Optional<Ticket> findByIdWithDetails(Long id);
    List<Ticket> search(TicketSearchCriteria criteria);
    long countByUser(Long userId);
}