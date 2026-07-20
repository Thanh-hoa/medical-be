package com.example.medical_be.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.RfAccountRole;
import com.example.medical_be.entity.Role;

/**
 * Dùng Testcontainers thay vì H2 vì migration V1__create_account.sql dùng BIGSERIAL
 * (cú pháp riêng Postgres, H2 không hiểu). Replace.NONE để @DataJpaTest không tự thay
 * DataSource bằng DB nhúng, giữ nguyên Postgres thật từ container.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RfAccountRoleRepository rfAccountRoleRepository;

    @Autowired
    TestEntityManager entityManager;

    private Account newAccount(String email, boolean isActive, boolean isDelete) {
        Account account = Account.builder()
                .email(email)
                .username(email)
                .password("encoded-password")
                .isActive(isActive)
                .isDelete(isDelete)
                .build();
        return accountRepository.save(account);
    }


    @Test
    void findByEmail_shouldReturnAccount_whenExists() {
        Account created = newAccount("found@example.com", true, false);

        Optional<Account> result = accountRepository.findByEmail("found@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(created.getId());
        assertThat(result.get().getEmail()).isEqualTo("found@example.com");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotExists() {
        Optional<Account> result = accountRepository.findByEmail("missing@example.com");

        assertThat(result).isEmpty();
    }


    @Test
    void findByEmailAndIsActive_shouldReturnEmpty_whenActiveFlagDoesNotMatch() {
        newAccount("inactive@example.com", false, false);

        Optional<Account> result = accountRepository.findByEmailAndIsActive("inactive@example.com", true);

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailAndIsDelete_shouldReturnEmpty_whenDeleteFlagDoesNotMatch() {
        newAccount("deleted@example.com", true, true);

        Optional<Account> result = accountRepository.findByEmailAndIsDelete("deleted@example.com", false);

        assertThat(result).isEmpty();
    }


    @Test
    void findByEmailOrUsernameWithRoles_shouldReturnAccountWithRoles_whenAccountHasRole() {
        Account account = newAccount("withrole@example.com", true, false);
        Role role = roleRepository.save(Role.builder().name("Doctor").code("doctor").isActive(true).build());
        rfAccountRoleRepository.save(RfAccountRole.builder()
                .accountId(account.getId())
                .roleId(role.getId())
                .build());
        // account đang được cache trong persistence context từ lúc save() ở trên với
        // rfAccountRoles=null; phải flush+clear để buộc JOIN FETCH bên dưới load lại từ DB
        entityManager.flush();
        entityManager.clear();

        Optional<Account> result = accountRepository.findByEmailOrUsernameWithRoles("withrole@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRfAccountRoles()).hasSize(1);
        assertThat(result.get().getRfAccountRoles().get(0).getRole().getCode()).isEqualTo("doctor");
    }

    @Test
    void findByEmailOrUsernameWithRoles_shouldReturnAccountWithEmptyRoles_whenAccountHasNoRole() {
        newAccount("norole@example.com", true, false);
        entityManager.flush();
        entityManager.clear();

        Optional<Account> result = accountRepository.findByEmailOrUsernameWithRoles("norole@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getRfAccountRoles()).isEmpty();
    }

    @Test
    void findByEmailOrUsernameWithRoles_shouldMatchByUsername() {
        Account created = newAccount("byusername@example.com", true, false);

        Optional<Account> result = accountRepository.findByEmailOrUsernameWithRoles("byusername@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(created.getId());
        assertThat(result.get().getUsername()).isEqualTo("byusername@example.com");
    }


    @Test
    void findByIdAndIsDelete_shouldReturnAccount_whenDeleteFlagMatches() {
        Account account = newAccount("bydelete@example.com", true, false);

        Optional<Account> result = accountRepository.findByIdAndIsDelete(account.getId(), false);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("bydelete@example.com");
    }

    @Test
    void findByIdAndIsDelete_shouldReturnEmpty_whenDeleteFlagDoesNotMatch() {
        Account account = newAccount("bydelete2@example.com", true, true);

        Optional<Account> result = accountRepository.findByIdAndIsDelete(account.getId(), false);

        assertThat(result).isEmpty();
    }

    @Test
    void searchAccounts_shouldExcludeGivenAccountId() {
        Account excluded = newAccount("exclude@example.com", true, false);
        newAccount("included@example.com", true, false);

        Page<Account> result = accountRepository.searchAccounts(false, excluded.getId(), null,
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Account::getId)
                .doesNotContain(excluded.getId());
        assertThat(result.getContent())
                .extracting(Account::getEmail)
                .contains("included@example.com");
    }

    @Test
    void searchAccounts_shouldFilterBySearchKeyword() {
        Account excluded = newAccount("unused@example.com", true, false);
        newAccount("keyword-match@example.com", true, false);
        newAccount("other@example.com", true, false);

        Page<Account> result = accountRepository.searchAccounts(false, excluded.getId(), "keyword-match",
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Account::getEmail)
                .containsExactly("keyword-match@example.com");
    }

    @Test
    void searchAccounts_shouldOnlyReturnAccountsMatchingIsDeleteFlag() {
        Account excluded = newAccount("exclude2@example.com", true, false);
        newAccount("active-account@example.com", true, false);
        newAccount("deleted-account@example.com", true, true);

        Page<Account> result = accountRepository.searchAccounts(false, excluded.getId(), null,
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .extracting(Account::getEmail)
                .containsExactly("active-account@example.com");
    }


    @Test
    void findByRole_shouldReturnOnlyActiveNonDeletedAccountsWithMatchingRole() {
        Role role = roleRepository.save(Role.builder().name("Employee").code("employee").isActive(true).build());

        Account activeAccount = newAccount("active-employee@example.com", true, false);
        rfAccountRoleRepository.save(RfAccountRole.builder()
                .accountId(activeAccount.getId())
                .roleId(role.getId())
                .build());

        Account inactiveAccount = newAccount("inactive-employee@example.com", false, false);
        rfAccountRoleRepository.save(RfAccountRole.builder()
                .accountId(inactiveAccount.getId())
                .roleId(role.getId())
                .build());

        List<Account> result = accountRepository.findByRole("Employee");

        assertThat(result)
                .extracting(Account::getEmail)
                .containsExactly("active-employee@example.com");
    }
}
