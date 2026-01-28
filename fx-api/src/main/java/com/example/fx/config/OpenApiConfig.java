package com.example.fx.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FX Pricing & Booking REST API")
                        .version("1.0.0")
                        .description("A comprehensive REST API for pricing (quotes) and booking (trades) of FX trades. " +
                                "Supports extensive attributes for both quote and trade entities with in-memory H2 database.")
                        .contact(new Contact().name("API Support"))
                        .license(new License().name("MIT License")))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server"));
    }
}
