package com.example.medical_be.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.medical_be.dto.req.account.AccountListReq;
import com.example.medical_be.dto.req.account.ChangePasswordReq;
import com.example.medical_be.dto.req.account.CreateAccountReq;
import com.example.medical_be.dto.req.account.DeleteAccountReq;
import com.example.medical_be.dto.req.account.RegisterAccountReq;
import com.example.medical_be.dto.req.account.UpdateAccountReq;
import com.example.medical_be.dto.req.account.UpdateProfileReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.Role;
import com.example.medical_be.entity.enums.Gender;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.mapper.AccountMapper;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.PermissionRoleRepository;
import com.example.medical_be.repository.RfAccountRoleRepository;
import com.example.medical_be.repository.RoleRepository;
import com.example.medical_be.support.AccountSentMailHelperService;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.support.AccountSupportCreateToken;
import com.example.medical_be.support.constant.RoleConstant;
import com.example.medical_be.validation.AccountValidate;
import com.example.medical_be.validation.ActiveAccountValidate;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock AccountValidate commonAccountValidate;
    @Mock ActiveAccountValidate activeAccountValidate;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AccountSupportCreateToken accountHelperService;
    @Mock AccountSentMailHelperService accountSentMailHelperService;
    @Mock AccountMapper accountMapper;
    @Mock RoleRepository roleRepository;
    @Mock RfAccountRoleRepository rfAccountRoleRepository;
    @Mock PermissionRoleRepository permissionRoleRepository;
    @Mock IMessageTranslator messageTranslator;
    @Mock AccountSupport currentAccountProvider;

    @InjectMocks
    AccountService accountService;

    @BeforeEach
    void setUp() {
        lenient().when(messageTranslator.getMessage(anyString())).thenReturn("error message");
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
    }

    private Account accountOf(Long id) {
        return accountOf(id, "account" + id + "@example.com");
    }

    private Account accountOf(Long id, String email) {
        return Account.builder()
                .id(id)
                .email(email)
                .username(email)
                .password("ENCODED")
                .isActive(true)
                .isDelete(false)
                .build();
    }

    @Test
    void registerAccount_shouldCreateAccountAndAssignPatientRole_whenValid() {
        RegisterAccountReq req = new RegisterAccountReq("new@example.com", "Password1", "Password1");
        Account savedAccount = accountOf(1L, req.email());
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        Role patientRole = Role.builder().id(9L).code(RoleConstant.ROLE_PATIENT).build();
        when(roleRepository.findByCode(RoleConstant.ROLE_PATIENT)).thenReturn(Optional.of(patientRole));
        when(rfAccountRoleRepository.findByAccountId(1L)).thenReturn(List.of());
        when(roleRepository.findAllById(List.of(9L))).thenReturn(List.of(patientRole));
        when(accountHelperService.generateTokenActiveAccount(req.email())).thenReturn("activation-token");
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(savedAccount)).thenReturn(mapped);

        InfoAccountRes result = accountService.registerAccount(req);

        assertThat(result).isSameAs(mapped);
        verify(commonAccountValidate).validatePasswordMatch(req.password(), req.repeatPassword());
        verify(commonAccountValidate).validateEmailNoExistsForRegister(req.email());
        verify(rfAccountRoleRepository).saveAll(anyList());
        verify(accountSentMailHelperService).sendMailActiveAccount(savedAccount, "activation-token");
    }

    @Test
    void registerAccount_shouldThrow_whenPasswordsDoNotMatch() {
        RegisterAccountReq req = new RegisterAccountReq("new@example.com", "Password1", "Different1");
        doThrow(new ApplicationException("password not match"))
                .when(commonAccountValidate).validatePasswordMatch(req.password(), req.repeatPassword());

        assertThrows(ApplicationException.class, () -> accountService.registerAccount(req));

        verifyNoInteractions(accountRepository);
    }

    @Test
    void registerAccount_shouldThrow_whenEmailAlreadyExists() {
        RegisterAccountReq req = new RegisterAccountReq("dup@example.com", "Password1", "Password1");
        doThrow(new ApplicationException("email existed"))
                .when(commonAccountValidate).validateEmailNoExistsForRegister(req.email());

        assertThrows(ApplicationException.class, () -> accountService.registerAccount(req));

        verifyNoInteractions(accountRepository);
    }

    @Test
    void registerAccount_shouldThrow_whenPatientRoleNotConfigured() {
        RegisterAccountReq req = new RegisterAccountReq("new@example.com", "Password1", "Password1");
        when(accountRepository.save(any(Account.class))).thenReturn(accountOf(1L, req.email()));
        when(roleRepository.findByCode(RoleConstant.ROLE_PATIENT)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> accountService.registerAccount(req));

        verifyNoInteractions(accountSentMailHelperService);
    }

    // ---------- createAccount ----------

    @Test
    void createAccount_shouldAssignRolesAndSendMail_whenValid() {
        CreateAccountReq req = new CreateAccountReq("Nguyen Van A", "01/01/1990", "new@example.com",
                "male", "0900000000", List.of(2L), "photo.png");
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(99L);
        Account savedAccount = accountOf(5L, req.email());
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountRepository.findById(5L)).thenReturn(Optional.of(savedAccount));
        Role role = Role.builder().id(2L).build();
        when(rfAccountRoleRepository.findByAccountId(5L)).thenReturn(List.of());
        when(roleRepository.findAllById(List.of(2L))).thenReturn(List.of(role));
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(savedAccount)).thenReturn(mapped);

        InfoAccountRes result = accountService.createAccount(req);

        assertThat(result).isSameAs(mapped);
        assertThat(savedAccount.getGender()).isEqualTo(Gender.male);
        verify(accountSentMailHelperService).sendInfoAccountEmailSafely(eq(savedAccount), anyString());
        verify(rfAccountRoleRepository).saveAll(anyList());
    }

    @Test
    void createAccount_shouldThrow_whenEmailAlreadyExists() {
        CreateAccountReq req = new CreateAccountReq("Nguyen Van A", null, "dup@example.com", null,
                "0900000000", null, null);
        doThrow(new ApplicationException("email existed"))
                .when(commonAccountValidate).validateEmailNoExistsForRegister(req.email());

        assertThrows(ApplicationException.class, () -> accountService.createAccount(req));

        verifyNoInteractions(accountRepository);
    }

    @Test
    void createAccount_shouldSkipRoleAssignment_whenRolesEmpty() {
        CreateAccountReq req = new CreateAccountReq("Nguyen Van A", null, "new2@example.com", null,
                "0900000000", List.of(), null);
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(99L);
        Account savedAccount = accountOf(6L, req.email());
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        when(accountRepository.findById(6L)).thenReturn(Optional.of(savedAccount));
        when(accountMapper.toInfoAccount(savedAccount)).thenReturn(new InfoAccountRes());

        accountService.createAccount(req);

        verifyNoInteractions(rfAccountRoleRepository);
    } 

    @Test
    void getInfoProfile_shouldReturnMappedAccount() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(1L);
        when(commonAccountValidate.validateAccountExist(1L)).thenReturn(account);
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(account)).thenReturn(mapped);

        InfoAccountRes result = accountService.getInfoProfile();

        assertThat(result).isSameAs(mapped);
    }

    @Test
    void getInfoProfile_shouldThrow_whenAccountNotFound() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        when(commonAccountValidate.validateAccountExist(1L))
                .thenThrow(new ApplicationException("not found"));

        assertThrows(ApplicationException.class, () -> accountService.getInfoProfile());
    }

    @Test
    void getMyPermissions_shouldReturnPermissionList() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        when(permissionRoleRepository.findPermissionStringsByAccountId(1L))
                .thenReturn(List.of("accounts:view"));

        List<String> result = accountService.getMyPermissions();

        assertThat(result).containsExactly("accounts:view");
    }

    @Test
    void updateProfile_shouldUpdateFields_whenValid() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(1L);
        when(commonAccountValidate.validateAccountExist(1L)).thenReturn(account);
        UpdateProfileReq req = new UpdateProfileReq("photo.png", "New Name", "0911111111",
                "15/03/1995", account.getEmail(), "female");
        when(accountRepository.save(account)).thenReturn(account);
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(account)).thenReturn(mapped);

        InfoAccountRes result = accountService.updateProfile(req);

        assertThat(result).isSameAs(mapped);
        assertThat(account.getName()).isEqualTo("New Name");
        assertThat(account.getGender()).isEqualTo(Gender.female);
    }

    @Test
    void updateProfile_shouldThrow_whenNewEmailBelongsToAnotherActiveAccount() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(1L);
        when(commonAccountValidate.validateAccountExist(1L)).thenReturn(account);
        UpdateProfileReq req = new UpdateProfileReq(null, "New Name", "0911111111", null,
                "taken@example.com", null);
        when(commonAccountValidate.validateEmailBelongAccountId("taken@example.com", 1L)).thenReturn(false);

        assertThrows(ApplicationException.class, () -> accountService.updateProfile(req));
    }

    @Test
    void updateProfile_shouldThrow_whenBirthdayFormatInvalid() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(1L);
        when(commonAccountValidate.validateAccountExist(1L)).thenReturn(account);
        // tháng 13 không tồn tại -> DateTimeParseException dù dùng resolver "smart" mặc định
        UpdateProfileReq req = new UpdateProfileReq(null, "New Name", "0911111111", "01/13/1995",
                account.getEmail(), null);

        assertThrows(ApplicationException.class, () -> accountService.updateProfile(req));
    }


    @Test
    void detailAccount_shouldReturnMappedAccount_whenFound() {
        Account account = accountOf(3L);
        when(accountRepository.findByIdAndIsDelete(3L, false)).thenReturn(Optional.of(account));
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(account)).thenReturn(mapped);

        InfoAccountRes result = accountService.detailAccount(3L);

        assertThat(result).isSameAs(mapped);
    }

    @Test
    void detailAccount_shouldThrow_whenNotFoundOrDeleted() {
        when(accountRepository.findByIdAndIsDelete(99L, false)).thenReturn(Optional.empty());

        assertThrows(ApplicationException.class, () -> accountService.detailAccount(99L));
    }

    @Test
    void updateAccount_shouldUpdateAnotherAccount_whenAdminIsNotTargetAccount() {
        Account target = accountOf(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        UpdateAccountReq req = new UpdateAccountReq(2L, true, "New Name", null, target.getEmail(),
                null, "0900000000", List.of(4L), null);
        when(rfAccountRoleRepository.findByAccountId(2L)).thenReturn(List.of());
        Role role = Role.builder().id(4L).build();
        when(roleRepository.findAllById(List.of(4L))).thenReturn(List.of(role));
        when(accountRepository.save(target)).thenReturn(target);
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(target)).thenReturn(mapped);

        InfoAccountRes result = accountService.updateAccount(req);

        assertThat(result).isSameAs(mapped);
        assertThat(target.getIsActive()).isTrue();
        verify(rfAccountRoleRepository).saveAll(anyList());
    }

    @Test
    void updateAccount_shouldThrow_whenAccountNotFound() {
        when(accountRepository.findById(100L)).thenReturn(Optional.empty());
        UpdateAccountReq req = new UpdateAccountReq(100L, null, "Name", null, "a@a.com",
                null, "0900000000", null, null);

        assertThrows(ApplicationException.class, () -> accountService.updateAccount(req));
    }

    @Test
    void updateAccount_shouldThrow_whenAdminTriesToUpdateOwnAccount() {
        Account self = accountOf(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(self));
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        UpdateAccountReq req = new UpdateAccountReq(1L, null, "Name", null, self.getEmail(),
                null, "0900000000", null, null);

        assertThrows(ApplicationException.class, () -> accountService.updateAccount(req));
    }

    @Test
    void deleteAccount_shouldSoftDeleteAccounts_whenNotCurrentUser() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        DeleteAccountReq req = new DeleteAccountReq();
        req.setAccountIds(List.of(2L, 3L));
        Account acc2 = accountOf(2L);
        Account acc3 = accountOf(3L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(acc2));
        when(accountRepository.findById(3L)).thenReturn(Optional.of(acc3));

        accountService.deleteAccount(req);

        assertThat(acc2.getIsDelete()).isTrue();
        assertThat(acc3.getIsDelete()).isTrue();
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void deleteAccount_shouldThrow_whenTryingToDeleteSelf() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        DeleteAccountReq req = new DeleteAccountReq();
        req.setAccountIds(List.of(1L));

        assertThrows(ApplicationException.class, () -> accountService.deleteAccount(req));
    }

    @Test
    void deleteAccount_shouldSkipAlreadyDeletedAccount() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        DeleteAccountReq req = new DeleteAccountReq();
        req.setAccountIds(List.of(4L));
        Account alreadyDeleted = accountOf(4L);
        alreadyDeleted.setIsDelete(true);
        when(accountRepository.findById(4L)).thenReturn(Optional.of(alreadyDeleted));

        accountService.deleteAccount(req);

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void listAccount_shouldReturnPagedResult() {
        AccountListReq filter = new AccountListReq(1, 10, null, null, null);
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(2L);
        Page<Account> page = new PageImpl<>(List.of(account), PageRequest.of(0, 10), 1);
        when(accountRepository.searchAccounts(eq(false), eq(1L), isNull(), any(Pageable.class)))
                .thenReturn(page);
        InfoAccountRes mapped = new InfoAccountRes();
        when(accountMapper.toInfoAccount(account)).thenReturn(mapped);

        PagedResponse<InfoAccountRes> result = accountService.listAccount(filter);

        assertThat(result.getItems()).containsExactly(mapped);
        assertThat(result.getTotalItems()).isEqualTo(1L);
    }

    @Test
    void listAccount_shouldThrow_whenPageIsInvalid() {
        AccountListReq filter = new AccountListReq(0, 10, null, null, null);

        assertThrows(ApplicationException.class, () -> accountService.listAccount(filter));
    }


    @Test
    void changePassword_shouldUpdatePassword_whenCurrentPasswordCorrect() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(1L);
        when(commonAccountValidate.validateAccountExist(1L)).thenReturn(account);
        when(passwordEncoder.matches("oldPass", account.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("ENCODED_NEW");
        ChangePasswordReq req = new ChangePasswordReq("oldPass", "newPass", "newPass");

        accountService.changePassword(req);

        assertThat(account.getPassword()).isEqualTo("ENCODED_NEW");
        verify(accountRepository).save(account);
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordsDoNotMatch() {
        ChangePasswordReq req = new ChangePasswordReq("oldPass", "newPass", "different");
        doThrow(new ApplicationException("not match"))
                .when(commonAccountValidate).validatePasswordMatch("newPass", "different");

        assertThrows(ApplicationException.class, () -> accountService.changePassword(req));
    }

    @Test
    void changePassword_shouldThrow_whenCurrentPasswordIncorrect() {
        when(currentAccountProvider.getCurrentAccountId()).thenReturn(1L);
        Account account = accountOf(1L);
        when(commonAccountValidate.validateAccountExist(1L)).thenReturn(account);
        when(passwordEncoder.matches("wrongPass", account.getPassword())).thenReturn(false);
        ChangePasswordReq req = new ChangePasswordReq("wrongPass", "newPass", "newPass");

        assertThrows(ApplicationException.class, () -> accountService.changePassword(req));

        verify(accountRepository, never()).save(any(Account.class));
    }


    @Test
    void activeAccount_shouldActivate_whenTokenValidAndAccountInactive() {
        Account account = accountOf(1L);
        account.setIsActive(false);
        when(activeAccountValidate.validateToken("token123")).thenReturn(account.getEmail());
        when(accountRepository.findByEmail(account.getEmail())).thenReturn(Optional.of(account));

        accountService.activeAccount("token123");

        assertThat(account.getIsActive()).isTrue();
        assertThat(account.getEmailVerifyAt()).isNotNull();
        verify(accountRepository).save(account);
    }

    @Test
    void activeAccount_shouldThrow_whenTokenInvalid() {
        when(activeAccountValidate.validateToken("bad-token"))
                .thenThrow(new ApplicationException("token invalid"));

        assertThrows(ApplicationException.class, () -> accountService.activeAccount("bad-token"));
    }

    @Test
    void activeAccount_shouldThrow_whenAccountAlreadyActive() {
        Account account = accountOf(1L);
        account.setIsActive(true);
        when(activeAccountValidate.validateToken("token123")).thenReturn(account.getEmail());
        when(accountRepository.findByEmail(account.getEmail())).thenReturn(Optional.of(account));

        assertThrows(ApplicationException.class, () -> accountService.activeAccount("token123"));
    }
}
