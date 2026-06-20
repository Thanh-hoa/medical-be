package com.example.medical_be.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
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
@Table(name = "prescription_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "prescription_id", nullable = false)
    Long prescriptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_prescription_items_prescription"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Prescription prescription;

    @Column(name = "medicine_id")
    Long medicineId;

    @Column(name = "medicine_name", nullable = false)
    String medicineName;

    @Column(name = "strength", length = 100)
    String strength;

    @Column(name = "unit", length = 50)
    String unit;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "morning_dose", length = 100)
    String morningDose;

    @Column(name = "noon_dose", length = 100)
    String noonDose;

    @Column(name = "afternoon_dose", length = 100)
    String afternoonDose;

    @Column(name = "evening_dose", length = 100)
    String eveningDose;

    @Column(name = "instruction", columnDefinition = "text")
    String instruction;

    @Column(name = "sort_order")
    Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
