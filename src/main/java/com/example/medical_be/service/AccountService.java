package com.example.medical_be.service;


import com.example.medical_be.dto.req.account.AccountListReq;
import com.example.medical_be.dto.req.account.CreateAccountReq;
import com.example.medical_be.dto.req.account.DeleteAccountReq;
import com.example.medical_be.dto.req.account.RegisterAccountReq;
import com.example.medical_be.dto.req.account.UpdateAccountReq;
import com.example.medical_be.dto.req.account.UpdateProfileReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.PagedResponse;
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
import com.example.medical_be.support.PaginationUtils;
import com.example.medical_be.support.AccountSentMailHelperService;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.validation.AccountValidate;
import com.example.medical_be.validation.ActiveAccountValidate;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Slf4j
public class AccountService implements IAccountService {

        private static final Map<String, String> SORT_MAP = Map.of(
            "name", "name",
            "email", "email",
            "created_at", "createdAt");
            
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

    @Override
    public InfoAccountRes getInfoProfile() {
        Account account = commonAccountValidate.validateAccountExist(currentAccountProvider.getCurrentAccountId());
        return accountMapper.toInfoAccount(account);
    }

    @Override
    @Transactional
    public InfoAccountRes updateProfile(UpdateProfileReq input) {
        Account account = commonAccountValidate.validateAccountExist(currentAccountProvider.getCurrentAccountId());
        updateCommonFields(
                account,
                input.email(),
                input.name(),
                input.birthday(),
                input.gender(),
                input.phoneNumber(),
                input.photo());
        return accountMapper.toInfoAccount(accountRepository.save(account));
    }
   @Override
    @Transactional(readOnly = true)
    public InfoAccountRes detailAccount(Long id) {
        Account account = accountRepository.findByIdAndIsDelete(id, false)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("account.not_found")));
        return accountMapper.toInfoAccount(account);
    }

    @Override
    @Transactional
    public InfoAccountRes updateAccount(UpdateAccountReq input) {
         Account account = accountRepository.findById(input.id())
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("account.not_found")));
        Long currentAccountId = currentAccountProvider.getCurrentAccountId();

        if (account.getId().equals(currentAccountId)) {
            throw new ApplicationException(messageTranslator.getMessage("account.update_self_not_allowed"));
        }
        updateCommonFields(
                account,
                input.email(),
                input.name(),
                input.birthday(),
                input.gender(),
                input.phoneNumber(),
                input.photo());
        if (input.isActive() != null) {
            account.setIsActive(input.isActive());
        }
        Account infoAccountUpdated = accountRepository.save(account);

        if (input.roles() != null && !input.roles().isEmpty()) {
            assignRolesToAccount(account.getId(), input.roles());
        }

        return accountMapper.toInfoAccount(infoAccountUpdated);
    }

    @Override
    @Transactional
    public void deleteAccount(DeleteAccountReq input) {
        Long currentAccountId = currentAccountProvider.getCurrentAccountId();
        input.getAccountIds().forEach(accountId -> {
            if (accountId.equals(currentAccountId)) {
                throw new ApplicationException(messageTranslator.getMessage("account.delete_self_not_allowed"));
            }
             Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("account.not_found")));
           
            if (account.getIsDelete()) {
                return;
            }
            account.setIsDelete(true);
            account.setIsActive(false);
            accountRepository.save(account);
        });
    }

    
    @Override
    public PagedResponse<InfoAccountRes> listAccount(AccountListReq filter) {
        int validatedPage = PaginationUtils.normalizePage(filter.page(), messageTranslator);
        int validatedLimit = PaginationUtils.normalizeLimit(filter.limit(), messageTranslator);

        Sort sort = PaginationUtils.buildSort(filter.sortBy(), filter.orderBy(), SORT_MAP, "createdAt");
        Pageable pageable = PageRequest.of(validatedPage, validatedLimit, sort);
        Long currentAccountId = currentAccountProvider.getCurrentAccountId();
        Page<Account> pageRes = accountRepository.searchAccounts(false, currentAccountId, filter.q(),
                pageable);

        List<InfoAccountRes> items = pageRes.getContent()
                .stream()
                .map(accountMapper::toInfoAccount)
                .toList();
        return PagedResponse.<InfoAccountRes>builder()
                .items(items)
                .totalPage(pageRes.getTotalPages())
                .currentPage(filter.page())
                .totalItems(pageRes.getTotalElements())
                .limit(filter.limit())
                .build();
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

     private void updateCommonFields(
            Account account,
            String email,
            String name,
            String birthday,
            String gender,
            String phoneNumber,
            String photoUrl) {
        if (email != null && !email.equals(account.getEmail())) {
            if (!commonAccountValidate.validateEmailBelongAccountId(email, account.getId())) {
                throw new ApplicationException(
                        messageTranslator.getMessage("account.email.already_exists"));
            }
            account.setEmail(email);
        }

        if (name != null)
            account.setName(name);

        if (photoUrl != null && !photoUrl.isBlank()) {
            account.setPhotoUrl(photoUrl);
        }

        if (birthday != null) {
            try {
                account.setBirthday(LocalDate.parse(birthday,
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } catch (DateTimeParseException e) {
                throw new ApplicationException( messageTranslator.getMessage("account.birthday.invalid_format"));
            }
        }

        if (gender != null) {
            account.setGender(Gender.valueOf(gender.toLowerCase()));
        }

        if (phoneNumber != null)
            account.setPhoneNumber(phoneNumber);

        account.setUpdatedAt(LocalDateTime.now());
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
