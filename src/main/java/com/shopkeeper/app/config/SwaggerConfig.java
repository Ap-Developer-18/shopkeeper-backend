package com.shopkeeper.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI shopkeeperOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("https://shopkeeper-backend-production-8150.up.railway.app").description("Railway Production HTTPS"),
                        new Server().url("http://localhost:8080").description("Local Development")
                ))
                .info(new Info()
                        .title("Shopkeeper App API")
                        .description("Login portal, billing, khata book, and stock management for shopkeepers")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}