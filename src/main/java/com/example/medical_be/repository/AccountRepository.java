package com.example.medical_be.repository;
import com.example.medical_be.entity.Account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account , Long> {
    Optional<Account> findByEmailAndIsActive(String email , Boolean isActive);
    Optional<Account> findByEmailAndIsDelete(String email , Boolean isDelete);
    Optional<Account> findByEmail(String email);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.rfAccountRoles r LEFT JOIN FETCH r.role WHERE a.email = :value OR a.username = :value")
    Optional<Account> findByEmailOrUsernameWithRoles(@Param("value") String value);
    Optional<Account> findByIdAndIsDelete(Long id, Boolean isDelete);

    @Query(value = """
                SELECT a FROM Account a
                WHERE a.isDelete = :isDelete
                AND a.id != :excludeAccountId
                AND (
                    :search IS NULL OR :search = '' OR
                    LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                """, countQuery = """
                SELECT COUNT(DISTINCT a.id) FROM Account a
                WHERE a.isDelete = :isDelete
                AND a.id != :excludeAccountId
                AND (
                    :search IS NULL OR :search = '' OR
                    LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(a.email) LIKE LOWER(CONCAT('%', :search, '%'))
                )
                """)
    Page<Account> searchAccounts(
                @Param("isDelete") Boolean isDelete,
                @Param("excludeAccountId") Long excludeAccountId,
                @Param("search") String search,
                Pageable pageable);

    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN a.rfAccountRoles r LEFT JOIN r.role role WHERE LOWER(role.name) = LOWER(:roleName) AND a.isActive = true AND a.isDelete = false")
    List<Account> findByRole(@Param("roleName") String roleName);
}
