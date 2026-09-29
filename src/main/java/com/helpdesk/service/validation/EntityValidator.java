package com.helpdesk.service.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.helpdesk.exception.InvalidDataException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/** Applique les contraintes Bean Validation (@NotBlank, @Email...) déclarées sur les entités. */
public class EntityValidator {

    // Coûteux à créer : une seule instance pour toute l'application
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    private final Validator validator = FACTORY.getValidator();

    public <T> void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            Map<String, String> errors = new LinkedHashMap<>();
            for (ConstraintViolation<T> v : violations) {
                errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage());
            }
            throw new InvalidDataException(errors);
        }
    }
}