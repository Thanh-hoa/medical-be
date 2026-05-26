package com.example.medical_be.dto.res;

import java.time.LocalDateTime;

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
public class MedicalRecordSummaryRes {
    Long id;
    String recordNumber;
    String status;
    String department;
    String recordType;
    String fileName;
    String fileType;
    Long uploadedBy;
    PatientRes patient;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
