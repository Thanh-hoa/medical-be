package com.example.medical_be.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByBhyt(String bhyt);

    boolean existsByBhyt(String bhyt);

    @Query("""
            SELECT p FROM Patient p
            WHERE (
                :q IS NULL OR :q = '' OR
                LOWER(p.bhyt) LIKE LOWER(CONCAT('%', :q, '%')) OR
                LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            )
            """)
    Page<Patient> search(@Param("q") String q, Pageable pageable);
}
