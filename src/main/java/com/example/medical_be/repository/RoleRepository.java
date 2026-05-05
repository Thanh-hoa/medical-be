package com.example.medical_be.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import  com.example.medical_be.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role , Long> {
     
    Optional<Role> findByCode(String code);
    
}
