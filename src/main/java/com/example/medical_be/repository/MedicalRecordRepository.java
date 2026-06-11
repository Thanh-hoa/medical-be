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

    @Query("""
            SELECT m FROM MedicalRecord m
            WHERE m.uploadedBy = :uploadedBy
            AND (m.isDelete IS NULL OR m.isDelete = false)
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

    
    @Query("""
            SELECT m FROM MedicalRecord m
            WHERE (m.isDelete IS NULL OR m.isDelete = false)
            AND (:status IS NULL OR m.status = :status)
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

    @Query("""
            SELECT m FROM MedicalRecord m
            WHERE m.patientId = :patientId
            AND (m.isDelete IS NULL OR m.isDelete = false)
            ORDER BY m.createdAt DESC
            """)
    Page<MedicalRecord> findByPatientIdOrderByCreatedAtDesc(
            @Param("patientId") Long patientId,
            Pageable pageable);

    
    @Query("""
            SELECT m FROM MedicalRecord m
            WHERE m.id = :id
            AND m.uploadedBy = :uploadedBy
            AND (m.isDelete IS NULL OR m.isDelete = false)
            """)
    Optional<MedicalRecord> findByIdAndUploadedBy(
            @Param("id") Long id,
            @Param("uploadedBy") Long uploadedBy);
}
