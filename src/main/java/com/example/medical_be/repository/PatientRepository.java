package com.example.medical_be.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findFirstByBhytHash(String bhytHash);

    Optional<Patient> findFirstByCitizenIdHash(String citizenIdHash);

    Optional<Patient> findFirstByAccountId(Long accountId);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.createdAt >= :from AND p.createdAt < :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
