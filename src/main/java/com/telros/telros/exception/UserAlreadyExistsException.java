package com.telros.telros.exception;

/**
 * Исключение, выбрасываемое когда пользователь уже существует
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String value, String field) {
        super(String.format("Пользователь с %s '%s' уже существует", field, value));
    }
}