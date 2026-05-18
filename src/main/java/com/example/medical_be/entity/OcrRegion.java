package com.example.medical_be.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "ocr_regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OcrRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "record_id", nullable = false)
    Long recordId;

    // 5 loại: hospital_header, patient_info, diagnosis_block, test_table, footer_signature
    @Column(name = "region_type", length = 50, nullable = false)
    String regionType;

    @Column(name = "count")
    @Builder.Default
    Integer count = 1;

    // Confidence trung bình (0.000000 – 1.000000)
    @Column(name = "confidence_avg", precision = 8, scale = 6)
    BigDecimal confidenceAvg;

    @Column(name = "raw_text", columnDefinition = "text")
    String rawText;

    // Lưu dạng JSON string: [[x1,y1,x2,y2], ...]
    @Column(name = "bounding_boxes", columnDefinition = "text")
    String boundingBoxes;

    @Column(name = "created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    MedicalRecord medicalRecord;
}
