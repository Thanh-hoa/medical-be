package com.example.medical_be.dto.req.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ResetPasswordReq(
        @NotBlank(message = "{token.require}")
        String token,
        @NotBlank(message = "{account.password.require}")
        String password,
        @NotBlank(message = "{account.password.require}")
        String repeatPassword
) {}
