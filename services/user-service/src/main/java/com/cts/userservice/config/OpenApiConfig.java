package com.cts.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI/Swagger documentation and security scheme for the service.
 */
@Slf4j
@Configuration
public class OpenApiConfig {
    /** Builds the OpenAPI metadata, server list and bearer-token security scheme. */
    @Bean
    public OpenAPI apiInfo() {
        log.debug("Initializing OpenAPI metadata for user-service");
        return new OpenAPI()
            .info(new Info().title("User-Service API").version("v1"))
            .servers(List.of(new Server().url("http://localhost:8080")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
    /** Hides the gateway-injected X-User-Id / X-User-Role headers from the generated docs. */
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