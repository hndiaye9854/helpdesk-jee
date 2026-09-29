package com.helpdesk.exception;

public abstract class HelpdeskException extends RuntimeException {
    protected HelpdeskException(String message) {
        super(message);
    }
}