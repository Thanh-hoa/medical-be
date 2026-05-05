package com.example.medical_be.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission , Long> {
    
}
