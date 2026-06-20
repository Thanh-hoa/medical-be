package com.example.medical_be.dto.res;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionItemRes {
    Long id;
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
