package com.example.medical_be.dto.res;

import java.time.LocalDateTime;
import java.util.List;

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
public class PrescriptionPrintRes {
    String prescriptionNumber;
    String hospitalName;
    String receiverName;
    String insuranceCode;
    String receiverAddress;
    String diagnosis;
    String doctorName;
    Integer durationDays;
    String advice;
    LocalDateTime issuedAt;
    List<PrescriptionItemRes> items;
}
