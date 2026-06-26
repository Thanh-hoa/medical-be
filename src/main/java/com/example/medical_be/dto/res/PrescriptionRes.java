package com.example.medical_be.dto.res;

import java.time.LocalDateTime;
import java.util.List;

import com.example.medical_be.entity.enums.PrescriptionDurationOption;
import com.example.medical_be.entity.enums.PrescriptionStatus;

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
public class PrescriptionRes {
    Long id;
    String prescriptionNumber;
    Long medicalRecordId;
    Long patientId;
    Long doctorId;
    String doctorName;
    PrescriptionStatus status;
    String hospitalName;
    String receiverName;
    String insuranceCode;
    String receiverAddress;
    String diagnosis;
    PrescriptionDurationOption durationOption;
    Integer durationDays;
    String advice;
    LocalDateTime issuedAt;
    LocalDateTime createdAt;
    List<PrescriptionItemRes> items;
}
