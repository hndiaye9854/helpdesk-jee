package com.helpdesk.model.enums;

import java.util.EnumSet;
import java.util.Set;

public enum TicketStatus {
    OPEN("Ouvert"),
    IN_PROGRESS("En cours"),
    RESOLVED("Résolu"),
    CLOSED("Clôturé");

    private final String label;

    TicketStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

    /**
     * Cycle de vie d'un ticket :
     * OPEN ⇄ IN_PROGRESS ⇄ RESOLVED → CLOSED (état final)
     */
    public Set<TicketStatus> allowedTransitions() {
        return switch (this) {
            case OPEN        -> EnumSet.of(IN_PROGRESS);
            case IN_PROGRESS -> EnumSet.of(OPEN, RESOLVED);
            case RESOLVED    -> EnumSet.of(IN_PROGRESS, CLOSED);
            case CLOSED      -> EnumSet.noneOf(TicketStatus.class);
        };
    }

    public boolean canTransitionTo(TicketStatus target) {
        return allowedTransitions().contains(target);
    }

    public boolean isFinal() {
        return this == CLOSED;
    }
}