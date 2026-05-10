package com.example.medical_be.dto.req;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DeleteAccountReq {
    @NotNull(message = "{account.ids.require}")
    @NotEmpty(message = "{account.ids.not_empty}")
    private List<Long> accountIds;
}