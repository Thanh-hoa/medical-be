package com.example.medical_be.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.medical_be.routes.APIRoutes;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.server.url:http://localhost:8080}")
    private String serverUrl;

    @Value("${app.server.description:Development Server}")
    private String serverDescription;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Medical BE API Documentation")
                        .description("Tài liệu API cho hệ thống quản lý y tế")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Nguyễn Thanh Hòa")
                                .email("hoa25102020@gmail.com")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication")));
    }

        @Bean
        public GroupedOpenApi authenticationOpenApiGroup() {
                return GroupedOpenApi.builder()
                        .group("Authentication")
                        .pathsToMatch(
                                APIRoutes.API_V1 + "/" + APIRoutes.LOGIN,
                                APIRoutes.API_V1 + "/" + APIRoutes.LOGOUT,
                                APIRoutes.API_V1 + "/" + APIRoutes.REFRESH_TOKEN
                        )
                        .displayName("01. Authentication APIs")
                        .build();
        }
        @Bean
        public GroupedOpenApi userOpenApiGroup() {
                return GroupedOpenApi.builder()
                        .group("Account Management")
                        .pathsToMatch(APIRoutes.API_V1 + "/account/**")
                        .displayName("02.  Account Management APIs")
                        .build();
        }

        @Bean
        public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("common")
                .pathsToMatch(APIRoutes.API_V1 + "/common/**")
                .displayName("03. Common APIs")
                .build();
    }
}

