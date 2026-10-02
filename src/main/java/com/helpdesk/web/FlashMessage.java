package com.helpdesk.web;

/** Message affiché une seule fois, après une redirection (pattern PRG). */
public class FlashMessage {

    private final String type;    // success, danger, warning : classes d'alerte Bootstrap
    private final String message;

    public FlashMessage(String type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getType() { return type; }
    public String getMessage() { return message; }
}