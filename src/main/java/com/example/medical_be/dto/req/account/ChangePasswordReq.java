package com.example.medical_be.dto.req.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ChangePasswordReq(
        @NotBlank(message = "{account.password.require}")
        String currentPassword,
        @NotBlank(message = "{account.password.require}")
        String newPassword,
        @NotBlank(message = "{account.password.require}")
        String repeatNewPassword
) {}
