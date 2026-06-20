package com.example.medical_be.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.Medicine;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByCode(String code);

    Optional<Medicine> findByCode(String code);

    @Query("""
            SELECT m FROM Medicine m
            WHERE m.isActive = true
            AND (
                :q IS NULL OR :q = '' OR
                LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(m.code) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(m.strength) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<Medicine> search(@Param("q") String q, Pageable pageable);
}
