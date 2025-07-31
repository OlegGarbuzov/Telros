package com.telros.telros.exception;

/**
 * Исключение, выбрасываемое при ошибках безопасности
 */
public class SecurityException extends RuntimeException {
    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityException(String operation, String resource) {
        super(String.format("Отказано в доступе к операции '%s' для ресурса '%s'", operation, resource));
    }
}