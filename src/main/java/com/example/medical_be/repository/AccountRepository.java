package com.example.medical_be.repository;
import com.example.medical_be.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account , Long> {
    Optional<Account> findByEmailAndIsActive(String email , Boolean isActive);
    Optional<Account> findByEmail(String email);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.rfAccountRoles r LEFT JOIN FETCH r.role WHERE a.email = :value OR a.username = :value")
    Optional<Account> findByEmailOrUsernameWithRoles(@Param("value") String value);
}
