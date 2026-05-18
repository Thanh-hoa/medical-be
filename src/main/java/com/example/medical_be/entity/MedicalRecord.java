package com.example.medical_be.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "record_number", length = 50, unique = true)
    String recordNumber;

    @Column(name = "uploaded_by", nullable = false)
    Long uploadedBy;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "file_type", length = 10)
    String fileType;

    @Column(name = "original_image_path", length = 500)
    String originalImagePath;

    // FK → patients.id (tìm qua BHYT, sau đó link record vào đây)
    @Column(name = "patient_id")
    Long patientId;

    // Khoa phòng — OCR extract, employee có thể chỉnh: "Nội Khoa", "Ngoại Khoa"...
    @Column(name = "department", length = 100)
    String department;

    // Loại bệnh án — "Nội trú", "Ngoại trú"
    @Column(name = "record_type", length = 50)
    String recordType;

    @Column(name = "status", length = 30)
    @Convert(converter = MedicalRecordStatusConverter.class)
    @Builder.Default
    MedicalRecordStatus status = MedicalRecordStatus.PROCESSING;

    @Column(name = "notes", columnDefinition = "text")
    String notes;

    @Column(name = "rejection_reason", columnDefinition = "text")
    String rejectionReason;

    @Column(name = "approved_by")
    Long approvedBy;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;

    @Column(name = "created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
