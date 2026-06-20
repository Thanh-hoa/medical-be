package com.example.medical_be.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.medical_be.entity.enums.PrescriptionDurationOption;
import com.example.medical_be.entity.enums.PrescriptionStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "prescription_number", length = 50, unique = true)
    String prescriptionNumber;

    @Column(name = "medical_record_id", nullable = false)
    Long medicalRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_prescriptions_medical_record"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    MedicalRecord medicalRecord;

    @Column(name = "patient_id")
    Long patientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_prescriptions_patient"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Patient patient;

    @Column(name = "doctor_id", nullable = false)
    Long doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_prescriptions_doctor"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Account doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    PrescriptionStatus status = PrescriptionStatus.DRAFT;

    @Column(name = "hospital_name")
    String hospitalName;

    @Column(name = "receiver_name")
    String receiverName;

    @Column(name = "insurance_code", length = 50)
    String insuranceCode;

    @Column(name = "receiver_address", columnDefinition = "text")
    String receiverAddress;

    @Column(name = "diagnosis", columnDefinition = "text")
    String diagnosis;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_option", length = 20)
    PrescriptionDurationOption durationOption;

    @Column(name = "duration_days")
    Integer durationDays;

    @Column(name = "advice", columnDefinition = "text")
    String advice;

    @Column(name = "issued_at")
    LocalDateTime issuedAt;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<PrescriptionItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
