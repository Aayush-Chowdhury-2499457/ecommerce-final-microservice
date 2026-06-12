package com.cts.productservice.exception;

import com.cts.productservice.exception.custom.DuplicateResourceException;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.exception.custom.UnauthorizedAccessException;
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
 * Centralized exception handler that translates exceptions thrown anywhere in the
 * service into consistent {@link ErrorResponse} payloads with appropriate HTTP
 * status codes.
 * <p>
 * Annotated with {@link RestControllerAdvice} so it applies globally to every REST
 * controller. Each handler logs the failure and delegates to {@link #buildResponse}
 * to construct the response body.
 *
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles missing-resource failures and maps them to {@code 404 NOT FOUND}.
     *
     * @param ex      the thrown exception describing the missing resource
     * @param request the current request, used to capture the request URI
     * @return a {@code 404} response wrapping the error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex, request.getRequestURI(), ex.getMessage());
    }

    /**
     * Handles duplicate-resource conflicts and maps them to {@code 409 CONFLICT}.
     *
     * @param ex      the thrown exception describing the conflicting resource
     * @param request the current request, used to capture the request URI
     * @return a {@code 409} response wrapping the error details
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex,
                                                                 HttpServletRequest request) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex, request.getRequestURI(), ex.getMessage());
    }

    /**
     * Handles invalid business operations and maps them to {@code 400 BAD REQUEST}.
     *
     * @param ex      the thrown exception describing the invalid operation
     * @param request the current request, used to capture the request URI
     * @return a {@code 400} response wrapping the error details
     */
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex,
                                                                HttpServletRequest request) {
        log.warn("Invalid operation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex, request.getRequestURI(), ex.getMessage());
    }

    /**
     * Handles bean-validation failures on request bodies, flattening the individual
     * field errors into a single message and mapping to {@code 400 BAD REQUEST}.
     *
     * @param ex      the validation exception carrying the binding result
     * @param request the current request, used to capture the request URI
     * @return a {@code 400} response listing each invalid field and its message
     */
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

    /**
     * Handles authorization failures and maps them to {@code 403 FORBIDDEN}.
     *
     * @param ex      the thrown exception describing the denied access
     * @param request the current request, used to capture the request URI
     * @return a {@code 403} response wrapping the error details
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex,
                                                                  HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, ex, request.getRequestURI(), ex.getMessage());
    }

    /**
     * Fallback handler for any otherwise-unhandled exception, mapping it to
     * {@code 500 INTERNAL SERVER ERROR}. The full stack trace is logged at error level.
     *
     * @param ex      the unexpected exception
     * @param request the current request, used to capture the request URI
     * @return a {@code 500} response wrapping the error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request.getRequestURI(), ex.getMessage());
    }

    /**
     * Builds a populated {@link ErrorResponse} and wraps it in a {@link ResponseEntity}
     * carrying the supplied status. Shared by all handler methods.
     *
     * @param status  the HTTP status to return
     * @param ex      the exception that triggered the response, used for its class name
     * @param path    the request URI that produced the error
     * @param message the client-facing error message
     * @return a {@link ResponseEntity} with the given status and a populated error body
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status,
                                                        Exception ex,
                                                        String path,
                                                        String message) {
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
