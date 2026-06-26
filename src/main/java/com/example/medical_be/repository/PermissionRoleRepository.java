package com.example.medical_be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.medical_be.entity.PermissionRole;

@Repository
public interface PermissionRoleRepository extends JpaRepository<PermissionRole , Long> {

    boolean existsByPermissionIdAndPermissionActionIdAndRoleId(Long permissionId, Long permissionActionId, Long roleId);

    @Query("""
            SELECT CONCAT(p.slug, ':', pa.code)
            FROM PermissionRole pr
            JOIN pr.permission p
            JOIN pr.permissionAction pa
            WHERE pr.roleId IN (
                SELECT rar.roleId FROM RfAccountRole rar WHERE rar.accountId = :accountId
            )
            """)
    List<String> findPermissionStringsByAccountId(@Param("accountId") Long accountId);
}
