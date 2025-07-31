package com.telros.telros.exception;

/**
 * Исключение, выбрасываемое при ошибках обработки файлов
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileProcessingException(String operation, String fileName, Throwable cause) {
        super(String.format("Ошибка при %s файла '%s': %s", operation, fileName, cause.getMessage()), cause);
    }
}