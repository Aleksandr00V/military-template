package ua.edu.viti.military.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Military Vehicle Transport API - Варіант B")
                        .version("1.0.0")
                        .description(
                                "REST API для системи управління військовим автотранспортом.\n\n" +
                                        "**Функціональність:**\n" +
                                        "- Управління категоріями транспорту\n" +
                                        "- CRUD операції з транспортними засобами\n" +
                                        "- Управління водіями\n" +
                                        "- Фільтрація по статусу, типу палива, категорії\n" +
                                        "- Контроль технічного обслуговування\n" +
                                        "- Моніторинг термінів дії посвідчень водіїв\n\n" +
                                        "**Авторизація:** Використовуйте `/api/auth/login` для отримання JWT токена.\n\n" +
                                        "**Технології:** Spring Boot 3.3, Spring Security, JWT, PostgreSQL"
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ))
                // Додаємо JWT Security Schema
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введіть JWT токен (без префіксу 'Bearer ')")
                        )
                );
    }
}
