package com.example.medical_be.service;


import com.example.medical_be.dto.req.CreateAccountReq;
import com.example.medical_be.dto.req.RegisterAccountReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.Gender;
import com.example.medical_be.entity.RfAccountRole;
import com.example.medical_be.entity.Role;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.mapper.AccountMapper;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.RfAccounrRoleRepository;
import com.example.medical_be.repository.RoleRepository;
import com.example.medical_be.support.AccountSupportCreateToken;
import com.example.medical_be.support.AccountSentMailHelperService;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.validation.AccountValidate;
import com.example.medical_be.validation.ActiveAccountValidate;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class AccountService implements IAccountService {
    final AccountRepository accountRepository;
    final AccountValidate commonAccountValidate;
    final ActiveAccountValidate activeAccountValidate;
    final PasswordEncoder passwordEncoder;
    final AccountSupportCreateToken accountHelperService;
    final AccountSentMailHelperService accountSentMailHelperService;
    final AccountMapper accountMapper;
    final RoleRepository roleRepository;
    final RfAccounrRoleRepository rfAccounrRoleRepository;
    final IMessageTranslator messageTranslator;
    final AccountSupport currentAccountProvider;
    @Transactional
    public InfoAccountRes  registerAccount(RegisterAccountReq req){
        commonAccountValidate.validatePasswordMatch(req.password() , req.repeatPassword());
        commonAccountValidate.validateEmailNoExistsForRegister(req.email());
        Account account = saveAccount(
                "",
                "",
                "",
                req.email(),
                req.password(),
                false,
                "",
                null
        );
         String token = accountHelperService.generateTokenActiveAccount(req.email());
        accountSentMailHelperService.sendMailActiveAccount(account, token);        
        return  accountMapper.toInfoAccount(account);
    }

    @Transactional
    public InfoAccountRes createAccount(CreateAccountReq req){
        commonAccountValidate.validateEmailNoExistsForRegister(req.email());
        String defaultPassword = RandomStringUtils.randomAlphanumeric(8);
        String photoUrl = "";
        if (req.photo() != null && !req.photo().isBlank()) {
            photoUrl = req.photo();
        }
        Account account = saveAccount(
                req.name(),
                req.birthday(),
                req.phoneNumber(),
                req.email(),
                defaultPassword,
                true ,
                photoUrl,
                currentAccountProvider.getCurrentAccountId()
        );
        if (req.gender() != null && !req.gender().isBlank()) {
            account.setGender(Gender.valueOf(req.gender()));
        }
        if (req.roles() != null && !req.roles().isEmpty()) {
            assignRolesToAccount(account.getId(), req.roles());
        }
        accountSentMailHelperService.sendInfoAccountEmailSafely(account, defaultPassword);
        return accountMapper.toInfoAccount(accountRepository.save(account));
    }

    private Account saveAccount(
            String name ,
            String birthday ,
            String phoneNumber ,
            String email,
            String password,
            Boolean isActive,
            String photoUrl,
            Long createBy
    ){
        LocalDate parsedBirthday = null;
        if (birthday != null && !birthday.isBlank()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                parsedBirthday = LocalDate.parse(birthday, formatter);
            } catch (DateTimeParseException e) {
                throw new ApplicationException("account.birthday.invalid_format");
            }
        }
        Account account = Account.builder()
                .name(name)
                .birthday(parsedBirthday)
                .phoneNumber(phoneNumber)
                .email(email)
                .username(email)
                .password(passwordEncoder.encode(password))
                .isActive(isActive)
                .photoUrl(photoUrl)
                .isDelete(false)
                .createdAt(LocalDateTime.now())
                .createdBy(createBy)
                .build();

        return  accountRepository.save(account);
    }

     private void assignRolesToAccount(Long accountId, List<Long> roleIds) {
        List<RfAccountRole> existingAccountRoles = rfAccounrRoleRepository.findByAccountId(accountId);
        List<Role> roles = roleRepository.findAllById(roleIds);

        if (roles.size() != roleIds.size()) {

            throw new ApplicationException(
                    messageTranslator.getMessage("role.exist_invalid"));
        }

        List<Long> newRoleIds = roles.stream().map(Role::getId).toList();
        List<Long> existingRoleIds = existingAccountRoles.stream()
                .map(RfAccountRole::getRoleId)
                .toList();
        List<RfAccountRole> rolesToDelete = existingAccountRoles.stream()
                .filter(ar -> !newRoleIds.contains(ar.getRoleId()))
                .toList();
        List<RfAccountRole> newAccountRoles = roles.stream()
                .filter(role -> !existingRoleIds.contains(role.getId()))
                .map(role -> RfAccountRole.builder()
                        .accountId(accountId)
                        .roleId(role.getId())
                        .role(role)
                        .build())
                .toList();

        if (!rolesToDelete.isEmpty()) {
            rfAccounrRoleRepository.deleteAll(rolesToDelete);
        }

        if (!newAccountRoles.isEmpty()) {
            rfAccounrRoleRepository.saveAll(newAccountRoles);
        }

        rfAccounrRoleRepository.findByAccountId(accountId);
    }


    @Override
    public void activeAccount(String token) {
        String email  = activeAccountValidate.validateToken(token);
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new ApplicationException("account.not_found"));
        if(account.getIsActive()){
            throw new ApplicationException("account.already_active");
        }
        account.setIsActive(true);
        account.setEmailVerifyAt(LocalDateTime.now());
        accountRepository.save(account);
    }
    
}
