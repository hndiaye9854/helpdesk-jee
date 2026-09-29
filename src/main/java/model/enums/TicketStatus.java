package model.enums;


public enum TicketStatus {
    OPEN("Ouvert"),
    IN_PROGRESS("En cours"),
    RESOLVED("Résolu"),
    CLOSED("Clôturé");

    private final String label;

    TicketStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}