package com.telros.telros.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация Swagger/OpenAPI документации
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Telros API",
                version = "1.0",
                description = "REST API для управления пользователями и их контактной информацией",
                contact = @Contact(
                        name = "Telros",
                        email = "info@telros.ru",
                        url = "https://telros.ru"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Локальный сервер"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}