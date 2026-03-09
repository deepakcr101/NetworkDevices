package com.deepak.NetworkDevices.exception;

import com.deepak.NetworkDevices.dto.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.neo4j.driver.exceptions.TransientException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.ServiceNotFoundException;
import javax.naming.ServiceUnavailableException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var msg = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).filter(Objects::nonNull).findFirst().orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage(), req);
    }
    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ApiError> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "The Database is currently unreachable." +
                "Please try again later.", req);
    }

    @ExceptionHandler(TransientException.class)
    public ResponseEntity<ApiError> handleTransientException(TransientException ex, HttpServletRequest req) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "DATABSE_TRANSIENT_ERROR", ex.getMessage(), req);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        var err = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                req.getRequestURI(),
                UUID.randomUUID().toString(),
                OffsetDateTime.now());
        return ResponseEntity.status(status).body(err);
    }
}

