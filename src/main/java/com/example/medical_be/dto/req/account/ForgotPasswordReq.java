package com.example.medical_be.dto.req.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordReq(
        @NotBlank(message = "{account.email.require}")
        @Email(message = "{account.email.invalid}")
        String email
) {}
