package com.example.medical_be.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.medical_be.entity.ManagerTokenAccount;

public interface ManagerTokenAccountRepository extends JpaRepository<ManagerTokenAccount , Long> {
    Optional<ManagerTokenAccount> findByTokenAndIsActive(String token , Boolean isActive);

    @Modifying
    @Query("UPDATE ManagerTokenAccount m SET m.isActive = false WHERE m.token = :token")
    void deactivateToken(String token);
}
