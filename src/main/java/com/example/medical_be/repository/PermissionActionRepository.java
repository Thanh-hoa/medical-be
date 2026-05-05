package com.example.medical_be.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.medical_be.entity.PermissionAction;

public interface PermissionActionRepository extends JpaRepository<PermissionAction , Long> {
    Optional<PermissionAction>findByCode(String code);
}
