package com.cts.productservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Springdoc / OpenAPI configuration for the Product Service.
 * <p>
 * Declares the API metadata, the default server, and a bearer-token (JWT) security
 * scheme so the generated documentation reflects the authentication contract. It
 * also hides the gateway-injected internal headers from the published specification.
 *
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Builds the {@link OpenAPI} document describing this service, including its
     * title, version, default server, and bearer-token security scheme.
     *
     * @return the configured {@link OpenAPI} metadata bean
     */
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info().title("Product-Service API").version("v1"))
            .servers(List.of(new Server().url("http://localhost:8080")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    /**
     * Customizes every documented operation to strip the gateway-internal
     * {@code X-User-Id} and {@code X-User-Role} headers, which are populated by the
     * API Gateway rather than supplied by API consumers.
     *
     * @return an {@link OperationCustomizer} that removes the internal headers
     */
    @Bean
    public OperationCustomizer hideGatewayHeaders() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                operation.getParameters().removeIf(p ->
                        "header".equalsIgnoreCase(p.getIn()) &&
                                ("X-User-Id".equalsIgnoreCase(p.getName())
                                        || "X-User-Role".equalsIgnoreCase(p.getName())));
            }
            return operation;
        };
    }
}
