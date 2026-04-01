package com.minileetcode.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniLeetCodeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mini LeetCode API")
                        .description(
                            "A portfolio project demonstrating a full-stack Spring Boot REST API " +
                            "platform inspired by LeetCode. Features user management, problem CRUD, " +
                            "and submission tracking with statistics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mini LeetCode Portfolio")
                                .url("https://github.com/your-username/minileetcode"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
