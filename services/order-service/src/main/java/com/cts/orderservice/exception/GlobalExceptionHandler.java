package com.cts.orderservice.exception;

import com.cts.orderservice.exception.custom.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized exception handling for all controllers. Maps domain and framework
 * exceptions to consistent {@link ErrorResponse} bodies with the appropriate HTTP status:
 * 404 (not found), 400 (cancellation / illegal state / validation), 503 (service
 * unavailable), the downstream status (downstream errors), 403 (unauthorized), and
 * 500 (anything else).
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Maps {@link ResourceNotFoundException} to HTTP 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link OrderCancellationException} to HTTP 400. */
    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<ErrorResponse> handleCancellation(OrderCancellationException ex, HttpServletRequest request) {
        log.warn("Order cancellation error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link IllegalStateException} (e.g. empty cart) to HTTP 400. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("Invalid order operation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link ServiceUnavailableException} to HTTP 503. */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {
        log.error("Service unavailable: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link DownstreamException} to the original downstream HTTP status. */
    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ErrorResponse> handleDownstream(DownstreamException ex, HttpServletRequest request) {
        log.error("Downstream Exception: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode());
        return buildResponse(status, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps bean-validation failures ({@link MethodArgumentNotValidException}) to HTTP 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), fieldErrors);
    }

    /** Maps {@link UnauthorizedAccessException} to HTTP 403. */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Catch-all mapping any unhandled exception to HTTP 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Builds a uniform {@link ErrorResponse} body for the given status and message. */
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