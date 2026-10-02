package com.helpdesk.web;

/** Paramètre HTTP absent ou mal formé : réponse 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}