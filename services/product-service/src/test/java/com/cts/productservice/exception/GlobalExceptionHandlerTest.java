package com.cts.productservice.exception;

import com.cts.productservice.exception.custom.DuplicateResourceException;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.exception.custom.UnauthorizedAccessException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler} verifying that each exception type
 * maps to the expected HTTP status and a fully populated {@link ErrorResponse}.
 * <p>
 * The handler is invoked directly with a {@link MockHttpServletRequest} so the
 * status mapping, the error-body assembly in {@code buildResponse}, and the
 * validation-error flattening are all exercised without a web container.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/1");

    @Test
    void handleResourceNotFound_returns404WithBody() {
        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFound(new ResourceNotFoundException("Product not found: 1"), request);

        ErrorResponse body = response.getBody();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Not Found");
        assertThat(body.getErrorClass()).isEqualTo("ResourceNotFoundException");
        assertThat(body.getMessage()).isEqualTo("Product not found: 1");
        assertThat(body.getPath()).isEqualTo("/api/products/1");
        assertThat(body.getTimestamp()).isNotNull();
    }

    @Test
    void handleDuplicateResource_returns409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateResource(new DuplicateResourceException("Category already exists"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void handleInvalidOperation_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidOperation(new InvalidOperationException("Insufficient stock"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Insufficient stock");
    }

    @Test
    void handleUnauthorizedAccess_returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnauthorizedAccess(new UnauthorizedAccessException("Access denied"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getStatus()).isEqualTo(403);
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGeneric(new IllegalStateException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getErrorClass()).isEqualTo("IllegalStateException");
        assertThat(response.getBody().getMessage()).isEqualTo("boom");
    }

    @Test
    void handleValidation_flattensFieldErrorsInto400() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "createProductDTO");
        bindingResult.addError(new FieldError("createProductDTO", "productName", "must not be blank"));
        bindingResult.addError(new FieldError("createProductDTO", "price", "must be positive"));

        Method method = SampleTarget.class.getDeclaredMethod("handle", String.class);
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(new MethodParameter(method, 0), bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage())
                .contains("productName: must not be blank")
                .contains("price: must be positive");
    }

    /** Helper target whose method supplies a real {@link MethodParameter} for the validation test. */
    @SuppressWarnings("unused")
    private static final class SampleTarget {
        void handle(String body) {
            // no-op; only used reflectively to build a MethodParameter
        }
    }
}
