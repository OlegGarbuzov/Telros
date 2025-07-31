package com.telros.telros.exception;

/**
 * Исключение, выбрасываемое когда пользователь не найден
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long id) {
        super(String.format("Пользователь с ID %d не найден", id));
    }

    public UserNotFoundException(String username, String field) {
        super(String.format("Пользователь с %s '%s' не найден", field, username));
    }
}