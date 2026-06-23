package com.example.medical_be.dto.req.prescription;

import java.util.List;

import com.example.medical_be.entity.enums.PrescriptionDurationOption;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UpdatePrescriptionReq {
    String hospitalName;
    String receiverName;
    String insuranceCode;
    String receiverAddress;
    String diagnosis;
    PrescriptionDurationOption durationOption;
    Integer durationDays;
    String advice;
    List<PrescriptionItemReq> items;
}
