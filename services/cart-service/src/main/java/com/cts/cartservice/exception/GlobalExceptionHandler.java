package com.cts.cartservice.exception;

import com.cts.cartservice.exception.custom.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShoppingCartNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShoppingCartNotFound(ShoppingCartNotFoundException ex,
                                                                    HttpServletRequest request) {
        log.warn("Shopping cart not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCartOperation(InvalidCartOperationException ex,
                                                                    HttpServletRequest request) {
        log.warn("Invalid cart operation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex,
                                                                  HttpServletRequest request) {
        log.error("Service unavailable: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ErrorResponse> handleDownstream(DownstreamException ex,
                                                          HttpServletRequest request) {
        log.error("Downstream Exception: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        return buildResponse(status, ex, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        String fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex, request.getRequestURI(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request.getRequestURI(), ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, Exception ex, String path, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorClass(ex.getClass().getSimpleName())
                .message(message)
                .path(path)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}