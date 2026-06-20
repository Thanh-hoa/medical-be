package com.example.medical_be.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.enums.MedicalRecordStatus;

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

    // Dashboard queries
    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE (m.isDelete IS NULL OR m.isDelete = false)")
    long countActive();

    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE (m.isDelete IS NULL OR m.isDelete = false) AND m.status = :status")
    long countByStatus(@Param("status") MedicalRecordStatus status);

    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE (m.isDelete IS NULL OR m.isDelete = false) AND m.createdAt >= :from")
    long countCreatedSince(@Param("from") LocalDateTime from);

    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE (m.isDelete IS NULL OR m.isDelete = false) AND m.approvedAt >= :from")
    long countApprovedSince(@Param("from") LocalDateTime from);

    @Query("""
            SELECT m.status, COUNT(m) FROM MedicalRecord m
            WHERE (m.isDelete IS NULL OR m.isDelete = false)
            GROUP BY m.status
            """)
    List<Object[]> countGroupByStatus();

    @Query("""
            SELECT m.department, COUNT(m) FROM MedicalRecord m
            WHERE (m.isDelete IS NULL OR m.isDelete = false)
            AND m.department IS NOT NULL AND m.department != ''
            GROUP BY m.department
            ORDER BY COUNT(m) DESC
            """)
    List<Object[]> countGroupByDepartment();

    @Query("""
            SELECT m.uploadedBy, COUNT(m) FROM MedicalRecord m
            WHERE (m.isDelete IS NULL OR m.isDelete = false)
            GROUP BY m.uploadedBy
            """)
    List<Object[]> countGroupByUploadedBy();

    @Query("""
            SELECT m.approvedBy, COUNT(m) FROM MedicalRecord m
            WHERE (m.isDelete IS NULL OR m.isDelete = false) AND m.approvedBy IS NOT NULL
            GROUP BY m.approvedBy
            """)
    List<Object[]> countGroupByApprovedBy();

    @Query("""
            SELECT m.rejectedBy, COUNT(m) FROM MedicalRecord m
            WHERE (m.isDelete IS NULL OR m.isDelete = false) AND m.rejectedBy IS NOT NULL
            GROUP BY m.rejectedBy
            """)
    List<Object[]> countGroupByRejectedBy();
}
