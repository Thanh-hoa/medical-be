package com.example.medical_be.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com.example.medical_be.dto.json.ExtractedDataDto;
import com.example.medical_be.entity.enums.MedicalRecordStatus;
import com.example.medical_be.entity.enums.MedicalRecordStatusConverter;
import com.example.medical_be.dto.json.LabResultJson;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uploaded_by",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_medical_records_uploaded_by"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Account uploader;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "file_type", length = 10)
    String fileType;

    @Column(name = "original_image_path", length = 500)
    String originalImagePath;

    @Column(name = "patient_id")
    Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "patient_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_medical_records_patient"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Patient patient;

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

    @Column(name = "approved_by")
    Long approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "approved_by",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_medical_records_approved_by"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Account approver;

    @Column(name = "approved_at")
    LocalDateTime approvedAt;


    @Column(name = "verified_by")
    Long verifiedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "verified_by",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_medical_records_verified_by"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Account verifier;

    @Column(name = "verified_at")
    LocalDateTime verifiedAt;

  
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extracted_data", columnDefinition = "jsonb")
    @Builder.Default
    ExtractedDataDto extractedData = new ExtractedDataDto();

   
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lab_data", columnDefinition = "jsonb")
    @Builder.Default
    List<LabResultJson> labData = new ArrayList<>();

    @Column(name = "rejected_by")
    Long rejectedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "rejected_by",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_medical_records_rejected_by"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Account rejecter;

    @Column(name = "rejected_at")
    LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", columnDefinition = "text")
    String rejectionReason;

    @Column(name = "is_delete")
    @Builder.Default
    Boolean isDelete = false;

    @Column(name = "deleted_at")
    LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
