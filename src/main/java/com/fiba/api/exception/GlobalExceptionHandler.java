package com.fiba.api.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для унифицированного формата ошибок API
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.error("Ресурс не найден: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "error");
        body.put("code", HttpStatus.NOT_FOUND.value());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        return new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {
        
        log.error("Неверный запрос: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "error");
        body.put("code", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        log.error("Ошибка аутентификации: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "error");
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Ошибка аутентификации");
        body.put("details", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        log.error("Неверные учетные данные: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "error");
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Неверное имя пользователя или пароль");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFoundException(
            UsernameNotFoundException ex, WebRequest request) {
        
        log.error("Пользователь не найден: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "error");
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Пользователь не найден");
        body.put("details", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.error("Ошибка валидации: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        body.put("status", "error");
        body.put("code", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Ошибка валидации данных");
        body.put("errors", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("timestamp", System.currentTimeMillis());
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationError handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ValidationError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getDescription(false),
                LocalDateTime.now(),
                errors
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Недостаточно прав для доступа к этому ресурсу",
                request.getDescription(false),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument error: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", ex.getMessage());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex) {
        log.error("IO error: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Ошибка при обработке файла");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Размер файла превышает допустимый предел (10MB)");
        body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex) {
        log.error("Unexpected error: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Внутренняя ошибка сервера");
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
} 