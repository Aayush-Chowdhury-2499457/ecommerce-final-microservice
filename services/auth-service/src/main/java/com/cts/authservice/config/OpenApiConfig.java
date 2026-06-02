package com.cts.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration defining API metadata, the JWT bearer
 * security scheme, and customizers that hide gateway-injected headers.
 */
@Slf4j
@Configuration
public class OpenApiConfig {
    /**
     * Builds the OpenAPI definition including title, server, and JWT security scheme.
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI apiInfo() {
        log.debug("Initializing OpenAPI definition for Auth-Service");
        return new OpenAPI()
            .info(new Info().title("Auth-Service API").version("v1"))
            .servers(List.of(new Server().url("http://localhost:8080")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    /**
     * Removes gateway-injected headers ({@code X-User-Id}, {@code X-User-Role})
     * from the generated API documentation.
     *
     * @return an {@link OperationCustomizer} that strips those headers
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
