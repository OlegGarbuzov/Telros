package com.telros.telros.exception;

import com.telros.telros.dto.response.MessageResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для контроллеров
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка исключения EntityNotFoundException
     *
     * @param ex      исключение
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler({EntityNotFoundException.class, UserNotFoundException.class})
    public ResponseEntity<MessageResponse> handleEntityNotFoundException(RuntimeException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Обработка исключения UserAlreadyExistsException
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Обработка исключения FileProcessingException
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<MessageResponse> handleFileProcessingException(FileProcessingException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Обработка исключения UsernameNotFoundException
     *
     * @param ex      исключение
     * @param request запрос
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Обработка исключения AccessDeniedException
     *
     * @param ex      исключение
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<MessageResponse> handleSecurityExceptions(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Обработка исключения MethodArgumentNotValidException (ошибки валидации)
     *
     * @param ex      исключение
     * @return ответ с сообщениями об ошибках валидации
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    public ResponseEntity<Object> handleValidationExceptions(Exception ex, WebRequest request) {
        if (ex instanceof MethodArgumentNotValidException) {
            Map<String, String> errors = new HashMap<>();
            ((MethodArgumentNotValidException) ex).getBindingResult().getAllErrors().forEach((error) -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
        } else if (ex instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) ex;
            if (validationEx.getErrors() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationEx.getErrors());
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(validationEx.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new MessageResponse("Ошибка валидации"));
    }

    /**
     * Обработка исключения ConstraintViolationException (ошибки валидации)
     *
     * @param ex      исключение
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse("Ошибка валидации: " + ex.getMessage()));
    }

    /**
     * Обработка исключения MaxUploadSizeExceededException (превышение размера загружаемого файла)
     *
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MessageResponse> handleMaxSizeException() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(new MessageResponse("Превышен максимальный размер файла!"));
    }

    /**
     * Обработка всех остальных исключений
     *
     * @param ex      исключение
     * @return ответ с сообщением об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Произошла ошибка: " + ex.getMessage()));
    }
}