package com.cts.paymentservice.config;

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
 * springdoc OpenAPI configuration: API metadata, bearer-JWT security scheme, and hiding
 * of internal gateway headers from the generated documentation.
 */
@Slf4j
@Configuration
public class OpenApiConfig {
    /**
     * Builds the OpenAPI definition with title, server, and bearer security scheme.
     *
     * @return the configured {@link OpenAPI} bean
     */
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
            .info(new Info().title("Payment-Service API").version("v1"))
            .servers(List.of(new Server().url("http://localhost:8080")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
    /**
     * Removes internal gateway headers ({@code X-User-Id}, {@code X-User-Role}) from the
     * generated API documentation.
     *
     * @return the operation customizer
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