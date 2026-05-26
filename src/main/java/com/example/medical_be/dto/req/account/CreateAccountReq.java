package com.example.medical_be.dto.req.account;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;



@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateAccountReq (
                @NotBlank(message = "{account.name.require}") String name,
                @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4}$", message = "{account.birthday.invalid_format}") String birthday,

                @NotBlank(message = "{account.email.require}") @Email(message = "{account.email.invalid}") String email,

                @Pattern(regexp = "male|female|other", message = "{account.gender.invalid}") String gender,

                @NotBlank(message = "{account.phone.require}") String phoneNumber,

                @NotEmpty(message = "{account.roles.require}") List<Long> roles,
                String photo 
    ){ }
