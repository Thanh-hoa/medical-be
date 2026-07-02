package com.example.medical_be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    Optional<Prescription> findByMedicalRecordId(Long medicalRecordId);

    boolean existsByMedicalRecordId(Long medicalRecordId);

    Optional<Prescription> findByMedicalRecordIdAndPatientId(Long medicalRecordId, Long patientId);
}
