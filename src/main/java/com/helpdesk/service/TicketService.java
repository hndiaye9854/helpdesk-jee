package com.helpdesk.service;

import java.util.List;

import com.helpdesk.dao.TicketSearchCriteria;
import com.helpdesk.model.Ticket;
import com.helpdesk.model.enums.TicketStatus;

public interface TicketService {
    Ticket create(Ticket ticket, Long authorId);
    Ticket update(Ticket ticket);
    void delete(Long id);
    Ticket findById(Long id);
    List<Ticket> search(TicketSearchCriteria criteria);
    Ticket changeStatus(Long ticketId, TicketStatus newStatus);
    Ticket assign(Long ticketId, Long technicianId);
}