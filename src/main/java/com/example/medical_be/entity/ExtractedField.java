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
@Table(name = "extracted_fields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtractedField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "record_id", nullable = false)
    Long recordId;

    // FK → ocr_regions.id (nullable: region có thể bị xóa)
    @Column(name = "region_id")
    Long regionId;

    // Ví dụ: patient_name, patient_bhyt, diagnosis, hospital_name, ...
    @Column(name = "field_name", length = 100, nullable = false)
    String fieldName;

    @Column(name = "field_value", columnDefinition = "text")
    String fieldValue;

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
