package dao;


import model.enums.TicketPriority;
import model.enums.TicketStatus;

/** Critères de recherche optionnels : un critère null est ignoré. */
public class TicketSearchCriteria {

    private String title;
    private TicketStatus status;
    private TicketPriority priority;
    private Long assigneeId;

    public static TicketSearchCriteria empty() {
        return new TicketSearchCriteria();
    }

    public TicketSearchCriteria title(String title) { this.title = title; return this; }
    public TicketSearchCriteria status(TicketStatus status) { this.status = status; return this; }
    public TicketSearchCriteria priority(TicketPriority priority) { this.priority = priority; return this; }
    public TicketSearchCriteria assigneeId(Long assigneeId) { this.assigneeId = assigneeId; return this; }

    public String getTitle() { return title; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public Long getAssigneeId() { return assigneeId; }

    public boolean hasTitle() { return title != null && !title.isBlank(); }
}
