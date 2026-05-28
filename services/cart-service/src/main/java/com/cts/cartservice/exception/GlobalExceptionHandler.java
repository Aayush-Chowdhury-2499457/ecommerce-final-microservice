package com.cts.cartservice.exception;

import com.cts.cartservice.exception.custom.InvalidCartOperationException;
import com.cts.cartservice.exception.custom.ProductNotFoundException;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import com.cts.cartservice.exception.custom.ShoppingCartNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ShoppingCartNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleShoppingCartNotFound(ShoppingCartNotFoundException ex){
        log.warn("Shopping cart not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(
            ProductNotFoundException ex) {
        log.warn("Product not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCartOperation(
            InvalidCartOperationException ex) {
        log.warn("Invalid cart operation: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailable(
            ServiceUnavailableException ex) {
        log.error("Service unavailable: {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }


    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("errorClass", ex.getClass().getSimpleName());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
