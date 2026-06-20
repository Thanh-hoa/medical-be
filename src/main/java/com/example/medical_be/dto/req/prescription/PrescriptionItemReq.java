package com.example.medical_be.dto.req.prescription;

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
public class PrescriptionItemReq {
    Long medicineId;
    String medicineName;
    String strength;
    String unit;
    Integer quantity;
    String morningDose;
    String noonDose;
    String afternoonDose;
    String eveningDose;
    String instruction;
    Integer sortOrder;
}
