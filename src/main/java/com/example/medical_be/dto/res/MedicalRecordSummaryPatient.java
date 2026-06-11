package com.example.medical_be.dto.res;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordSummaryPatient {
    PatientRes patient;
    List<Item> records;
    Long totalRecords;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        Long id;
        String recordNumber;
        String status;
        String department;
        String signerName;
        String diagnosis;
    }
}
