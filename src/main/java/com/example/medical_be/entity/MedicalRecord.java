package com.example.medical_be.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.medical_be.dto.json.LabResultJson;

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

    @Column(name = "patient_id")
    Long patientId;

    @Column(name = "department", length = 100)
    String department;

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


    @Column(name = "verified_by")
    Long verifiedBy;

    @Column(name = "verified_at")
    LocalDateTime verifiedAt;

  
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_data", columnDefinition = "jsonb")
    @Builder.Default
    Map<String, String> extractedData = new HashMap<>();

   
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lab_data", columnDefinition = "jsonb")
    @Builder.Default
    List<LabResultJson> labData = new ArrayList<>();

    @Column(name = "created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
