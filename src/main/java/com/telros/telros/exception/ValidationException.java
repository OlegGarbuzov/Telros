package com.telros.telros.exception;

import java.util.Map;

/**
 * Исключение, выбрасываемое при ошибках валидации
 */
public class ValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = null;
    }

    public ValidationException(Map<String, String> errors) {
        super("Ошибка валидации данных");
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}