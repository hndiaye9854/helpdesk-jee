package com.helpdesk.model.enums;

public enum TicketPriority {
    LOW("Basse"),
    MEDIUM("Moyenne"),
    HIGH("Haute"),
    CRITICAL("Critique");

    private final String label;

    TicketPriority(String label) { this.label = label; }

    public String getLabel() { return label; }
}