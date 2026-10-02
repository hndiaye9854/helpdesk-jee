package com.helpdesk.exception;

public class ConcurrentUpdateException extends HelpdeskException {
    public ConcurrentUpdateException(String resource) {
        super(resource + " a été modifié par un autre utilisateur. Rechargez la page et recommencez.");
    }
}