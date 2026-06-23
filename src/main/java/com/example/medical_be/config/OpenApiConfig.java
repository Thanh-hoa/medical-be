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
    OpenAPI customOpenAPI() {
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
    GroupedOpenApi group01Authentication() {
        return GroupedOpenApi.builder()
                .group("01-authentication")
                .displayName("01. Authentication APIs")
                .pathsToMatch(
                        APIRoutes.API_V1 + "/" + APIRoutes.LOGIN,
                        APIRoutes.API_V1 + "/" + APIRoutes.LOGOUT,
                        APIRoutes.API_V1 + "/" + APIRoutes.REFRESH_TOKEN)
                .build();
    }

    @Bean
    GroupedOpenApi group02AccountManagement() {
        return GroupedOpenApi.builder()
                .group("02-account-management")
                .displayName("02. Account Management APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/account/**")
                .build();
    }

    @Bean
    GroupedOpenApi group03Common() {
        return GroupedOpenApi.builder()
                .group("03-common")
                .displayName("03. Common APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/common/**")
                .build();
    }

    @Bean
    GroupedOpenApi group04Patient() {
        return GroupedOpenApi.builder()
                .group("04-patient")
                .displayName("04. Patient Management APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/patient/**")
                .build();
    }

    @Bean
    GroupedOpenApi group05MedicalRecord() {
        return GroupedOpenApi.builder()
                .group("05-medical-record")
                .displayName("05. Medical Record APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/medical-record/**")
                .build();
    }

    @Bean
    GroupedOpenApi group06MenuPermission() {
        return GroupedOpenApi.builder()
                .group("06-menu-permission")
                .displayName("06. Menu Permission APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/config/**")
                .build();
    }
   @Bean
    GroupedOpenApi group07AuditLogs() {
        return GroupedOpenApi.builder()
                .group("07-audit-logs")
                .displayName("07. Audit Logs APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/audit-logs/**")
                .build();
    }
    @Bean
    GroupedOpenApi group08Dashboard() {
        return GroupedOpenApi.builder()
                .group("08-dashboard")
                .displayName("08. Dashboard APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/dashboard/**")
                .build();
    }

    @Bean
    GroupedOpenApi group09Medicine() {
        return GroupedOpenApi.builder()
                .group("09-medicine")
                .displayName("09. Medicine Catalog APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/medicine/**")
                .build();
    }
    @Bean
    GroupedOpenApi group10Prescription() {
        return GroupedOpenApi.builder()
                .group("10-prescription")
                .displayName("10. Prescription APIs")
                .pathsToMatch(APIRoutes.API_V1 + "/prescription/**")
                .build();
    }
}
