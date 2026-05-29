package com.example.medical_be.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.MedicalRecordStatus;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    boolean existsByRecordNumber(String recordNumber);

    // Employee: chỉ thấy record của mình
    @Query("""
            SELECT m FROM MedicalRecord m
            WHERE m.uploadedBy = :uploadedBy
            AND (:status IS NULL OR m.status = :status)
            AND (
                :q IS NULL OR :q = '' OR
                LOWER(m.recordNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<MedicalRecord> findByUploadedBy(
            @Param("uploadedBy") Long uploadedBy,
            @Param("status") MedicalRecordStatus status,
            @Param("q") String q,
            Pageable pageable);

    // Admin/Doctor: thấy tất cả, filter theo status và patient
    @Query("""
            SELECT m FROM MedicalRecord m
            WHERE (:status IS NULL OR m.status = :status)
            AND (:patientId IS NULL OR m.patientId = :patientId)
            AND (
                :q IS NULL OR :q = '' OR
                LOWER(m.recordNumber) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<MedicalRecord> findAll(
            @Param("status") MedicalRecordStatus status,
            @Param("patientId") Long patientId,
            @Param("q") String q,
            Pageable pageable);

    // Lấy tất cả record của 1 bệnh nhân, mới nhất trước
    Page<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(Long patientId, Pageable pageable);

    Optional<MedicalRecord> findByIdAndUploadedBy(Long id, Long uploadedBy);
}
