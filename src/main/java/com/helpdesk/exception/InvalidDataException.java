package com.helpdesk.exception;

import java.util.Map;

/** Erreurs de validation, par champ : servira à afficher les erreurs dans les formulaires. */
public class InvalidDataException extends HelpdeskException {

    private final Map<String, String> errors;

    public InvalidDataException(Map<String, String> errors) {
        super("Données invalides : " + errors);
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}