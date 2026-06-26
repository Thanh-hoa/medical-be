package com.example.medical_be.dto.req.account;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateProfileReq(
        String photo,

        @NotBlank(message = "{account.name.require}")
        String name,

        @NotBlank(message = "{account.phone.require}")
        String phoneNumber,

        @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "{account.birthday.invalid_format}")
        String birthday,

        @NotBlank(message = "{account.email.require}")
        @Email(message = "{account.email.invalid}")
        String email,

        @Pattern(regexp = "male|female|other", message = "{account.gender.invalid}")
        String gender) {
}