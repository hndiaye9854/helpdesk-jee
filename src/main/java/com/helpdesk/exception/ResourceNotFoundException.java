package com.helpdesk.exception;

public class ResourceNotFoundException extends HelpdeskException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " introuvable (id=" + id + ")");
    }
}