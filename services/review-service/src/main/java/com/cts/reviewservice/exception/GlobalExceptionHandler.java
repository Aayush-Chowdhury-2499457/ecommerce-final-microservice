package com.cts.reviewservice.exception;

import com.cts.reviewservice.exception.custom.DownstreamException;
import com.cts.reviewservice.exception.custom.DuplicateReviewException;
import com.cts.reviewservice.exception.custom.UnauthorizedAccessException;
import com.cts.reviewservice.exception.custom.PurchaseNotVerifiedException;
import com.cts.reviewservice.exception.custom.ResourceNotFoundException;
import com.cts.reviewservice.exception.custom.ServiceUnavailableException;
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
 * Centralized exception handling that maps domain and framework exceptions
 * to consistent {@link ErrorResponse} bodies and HTTP statuses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Maps {@link ResourceNotFoundException} to 404 Not Found. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps authorization and purchase-verification failures to 403 Forbidden. */
    @ExceptionHandler({UnauthorizedAccessException.class, PurchaseNotVerifiedException.class})
    public ResponseEntity<ErrorResponse> forbidden(RuntimeException ex, HttpServletRequest request) {
        log.warn("Forbidden: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link DuplicateReviewException} to 409 Conflict. */
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponse> duplicate(DuplicateReviewException ex, HttpServletRequest request) {
        log.warn("Duplicate review: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link ServiceUnavailableException} to 503 Service Unavailable. */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> serviceDown(ServiceUnavailableException ex, HttpServletRequest request) {
        log.error("Service unavailable: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps {@link DownstreamException} to the propagated downstream status. */
    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ErrorResponse> downstream(DownstreamException ex, HttpServletRequest request) {
        log.error("Downstream Exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.valueOf(ex.getStatusCode()), ex, request.getRequestURI(), ex.getMessage());
    }

    /** Maps bean validation failures to 400 Bad Request with field details. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", fieldErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), fieldErrors);
    }

    /** Catch-all mapping for unexpected exceptions to 500 Internal Server Error. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
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