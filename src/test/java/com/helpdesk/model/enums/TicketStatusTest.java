package com.helpdesk.model.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TicketStatusTest {

    @ParameterizedTest(name = "{0} → {1} est autorisée")
    @CsvSource({
        "OPEN, IN_PROGRESS",
        "IN_PROGRESS, OPEN",
        "IN_PROGRESS, RESOLVED",
        "RESOLVED, IN_PROGRESS",
        "RESOLVED, CLOSED"
    })
    void allowedTransitions(TicketStatus from, TicketStatus to) {
        assertTrue(from.canTransitionTo(to));
    }

    @ParameterizedTest(name = "{0} → {1} est interdite")
    @CsvSource({
        "OPEN, RESOLVED",
        "OPEN, CLOSED",
        "IN_PROGRESS, CLOSED",
        "CLOSED, OPEN",
        "CLOSED, IN_PROGRESS",
        "CLOSED, RESOLVED"
    })
    void forbiddenTransitions(TicketStatus from, TicketStatus to) {
        assertFalse(from.canTransitionTo(to));
    }

    @Test
    void closed_shouldBeTheOnlyFinalStatus() {
        assertTrue(TicketStatus.CLOSED.isFinal());
        assertFalse(TicketStatus.RESOLVED.isFinal());
    }
}