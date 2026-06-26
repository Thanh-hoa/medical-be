package com.example.medical_be.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.RfAccountRole;

@Repository
public interface RfAccountRoleRepository extends JpaRepository<RfAccountRole , Long> {

    Optional<RfAccountRole> findByAccountIdAndRoleId(Long accountId ,long roleId);
    List<RfAccountRole> findByAccountId(Long accountId);
} 
