package com.example.medical_be.seeder;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.RfAccountRole;
import com.example.medical_be.entity.Role;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.RfAccountRoleRepository;
import com.example.medical_be.repository.RoleRepository;
import com.example.medical_be.support.constant.RoleConstant;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSeeder implements ISeeder {
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final RfAccountRoleRepository rfAccounrRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void seed() {
        log.info("Seeding account...");
        List<Account> accounts = dataAccount();
        accounts.forEach(this::updateOrCreateAccount);
        log.info("Seeded {} account", accounts.size());
    }

    private List<Account> dataAccount() {
        return Collections.singletonList(
                Account.builder()
                        .name("Thanh Hoa")
                        .email("hoa1312004@gmail.com")
                        .username("hoa1312004@gmail.com")
                        .password(passwordEncoder.encode("Aa123456"))
                        .isActive(true)
                        .isDelete(false)
                        .emailVerifyAt(LocalDateTime.now())
                        .build()
        );
    }

    private void updateOrCreateAccount(Account account) {
        accountRepository.findByEmail(account.getEmail())
                .ifPresentOrElse(
                        existingAccount -> updateAccount(existingAccount, account),
                        () -> createAccount(account)
                );
    }

    private void updateAccount(Account existingAccount, Account newAccount) {
        existingAccount.setName(newAccount.getName());
        existingAccount.setEmail(newAccount.getEmail());
        existingAccount.setPassword(newAccount.getPassword());
        existingAccount.setIsActive(newAccount.getIsActive());
        existingAccount.setIsDelete(newAccount.getIsDelete());
        existingAccount.setCreatedAt(newAccount.getEmailVerifyAt());
        Account account = accountRepository.save(existingAccount);
        this.assignRole(account);
    }

    private void createAccount(Account account) {
        Account saveAccount = accountRepository.save(account);
        this.assignRole(saveAccount);
    }

    private void assignRole(Account account) {
        Optional<Role> role = roleRepository.findByCode(RoleConstant.ROLE_ADMIN);
        rfAccounrRoleRepository.findByAccountIdAndRoleId(account.getId(), role.orElseThrow().getId())
                .ifPresentOrElse(
                        existingAccount -> {
                        },
                        () -> {
                            rfAccounrRoleRepository.save(RfAccountRole
                                    .builder()
                                    .accountId(account.getId())
                                    .roleId(role.orElseThrow().getId())
                                    .build());
                        }
                );
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
