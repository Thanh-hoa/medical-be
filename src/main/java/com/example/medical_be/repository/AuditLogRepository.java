package com.example.medical_be.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:resourceType IS NULL OR a.resourceType = :resourceType)
            AND (:actorId IS NULL OR a.actorId = :actorId)
            AND (:action IS NULL OR a.action = :action)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findAll(
            @Param("resourceType") String resourceType,
            @Param("actorId") Long actorId,
            @Param("action") String action,
            Pageable pageable);

    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.resourceType = :resourceType
            AND a.resourceId = :resourceId
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByResource(
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            Pageable pageable);
}
