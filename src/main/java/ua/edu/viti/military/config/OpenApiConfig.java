package ua.edu.viti.military.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
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
                                        "**Технології:** Spring Boot 3.2, Spring Data JPA, PostgreSQL"
                        )
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
}
