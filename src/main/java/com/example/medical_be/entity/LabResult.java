package com.example.medical_be.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "lab_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LabResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "record_id", nullable = false)
    Long recordId;

    // FK → ocr_regions.id (test_table region)
    @Column(name = "region_id")
    Long regionId;

    // Ví dụ: NEU#, EOS#, MCV, MCH, MCHC, ...
    @Column(name = "test_name", length = 100, nullable = false)
    String testName;

    // Ví dụ: "4.6", "0.4", "88.5"
    @Column(name = "test_value", length = 50)
    String testValue;

    // Ví dụ: K/uL, fL, pg, g/dL, %
    @Column(name = "unit", length = 30)
    String unit;

    // Ví dụ: "(2 - 7.5)", "(85 - 95)"
    @Column(name = "reference_range", length = 50)
    String referenceRange;

    @Column(name = "is_abnormal")
    Boolean isAbnormal;

    @Column(name = "is_verified")
    @Builder.Default
    Boolean isVerified = false;

    // FK → account.id (employee xác nhận)
    @Column(name = "verified_by")
    Long verifiedBy;

    @Column(name = "verified_at")
    LocalDateTime verifiedAt;

    @Column(name = "created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
