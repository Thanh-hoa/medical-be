package com.example.medical_be.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.medical_be.config.SecurityConfig;
import com.example.medical_be.dto.req.account.ChangePasswordReq;
import com.example.medical_be.dto.req.account.CreateAccountReq;
import com.example.medical_be.dto.req.account.DeleteAccountReq;
import com.example.medical_be.dto.req.account.RegisterAccountReq;
import com.example.medical_be.dto.req.account.UpdateAccountReq;
import com.example.medical_be.dto.req.account.UpdateProfileReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.jwt.JwtAuthenticationFilter;
import com.example.medical_be.service.IAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;


@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    IAccountService accountService;

    @MockBean
    IMessageTranslator messageTranslator;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void bypassJwtFilter() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
        
    }


    @Test
    void register_shouldReturn200_whenBodyValid() throws Exception {
        RegisterAccountReq req = new RegisterAccountReq("new@example.com", "Password1", "Password1");
        when(accountService.registerAccount(any())).thenReturn(new InfoAccountRes());

        mockMvc.perform(post("/api/v1/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isError").value(false));
    }

    @Test
    @WithMockUser(authorities = "accounts:create")
    void createAccount_shouldReturn200_whenAuthorizedAndBodyValid() throws Exception {
        CreateAccountReq req = new CreateAccountReq("Nguyen Van A", "01/01/1990", "new@example.com",
                "male", "0900000000", List.of(2L), "photo.png");
        InfoAccountRes created = new InfoAccountRes();
        created.setId(10L);
        created.setName("Nguyen Van A");
        created.setEmail("new@example.com");
        when(accountService.createAccount(any())).thenReturn(created);

        mockMvc.perform(post("/api/v1/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.name").value("Nguyen Van A"))
                .andExpect(jsonPath("$.data.email").value("new@example.com"));
    }

    @Test
    @WithMockUser(authorities = "accounts:view")
    void createAccount_shouldReturn403_whenMissingAuthority() throws Exception {
        CreateAccountReq req = new CreateAccountReq("Nguyen Van A", "01/01/1990", "new@example.com",
                "male", "0900000000", List.of(2L), "photo.png");

        mockMvc.perform(post("/api/v1/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "accounts:create")
    void createAccount_shouldReturn400_whenNameMissing() throws Exception {
        CreateAccountReq req = new CreateAccountReq("", null, "new@example.com", null,
                "0900000000", List.of(2L), null);

        mockMvc.perform(post("/api/v1/account/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isError").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }


    @Test
    void validateToken_shouldReturn200_andCallService() throws Exception {
        mockMvc.perform(get("/api/v1/account/validate-token").param("token", "abc"))
                .andExpect(status().isOk());

        verify(accountService).activeAccount("abc");
    }

    @Test
    @WithMockUser
    void getInfoProfile_shouldReturn200_whenAuthenticated() throws Exception {
        InfoAccountRes profile = new InfoAccountRes();
        profile.setId(1L);
        profile.setEmail("me@example.com");
        when(accountService.getInfoProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/v1/account/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("me@example.com"));
    }

    @Test
    void getInfoProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/account/profile"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    void getMyPermissions_shouldReturn200() throws Exception {
        when(accountService.getMyPermissions()).thenReturn(List.of("accounts:view"));

        mockMvc.perform(get("/api/v1/account/my-permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("accounts:view"));
    }



    @Test
    @WithMockUser
    void changePassword_shouldReturn200_whenValid() throws Exception {
        ChangePasswordReq req = new ChangePasswordReq("oldPass", "newPass1", "newPass1");

        mockMvc.perform(put("/api/v1/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isError").value(false));
    }

    @Test
    @WithMockUser
    void changePassword_shouldReturn400_whenFieldMissing() throws Exception {
        ChangePasswordReq req = new ChangePasswordReq("", "newPass1", "newPass1");

        mockMvc.perform(put("/api/v1/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isError").value(true))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }



    @Test
    @WithMockUser
    void updateProfile_shouldReturn200_whenValid() throws Exception {
        UpdateProfileReq req = new UpdateProfileReq(null, "New Name", "0911111111", null, "a@a.com", null);
        InfoAccountRes updated = new InfoAccountRes();
        updated.setName("New Name");
        updated.setPhoneNumber("0911111111");
        when(accountService.updateProfile(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/account/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0911111111"));
    }


    @Test
    @WithMockUser(authorities = "accounts:view")
    void detailAccount_shouldReturn200_whenAuthorized() throws Exception {
        InfoAccountRes detail = new InfoAccountRes();
        detail.setId(5L);
        detail.setEmail("detail@example.com");
        when(accountService.detailAccount(5L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/account/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.email").value("detail@example.com"));
    }

    @Test
    @WithMockUser
    void detailAccount_shouldReturn403_whenMissingAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/account/5"))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(authorities = "accounts:edit")
    void updateAccount_shouldReturn200_whenAuthorized() throws Exception {
        UpdateAccountReq req = new UpdateAccountReq(2L, true, "Name", null, "a@a.com",
                null, "0900000000", null, null);
        InfoAccountRes updated = new InfoAccountRes();
        updated.setId(2L);
        updated.setName("Name");
        updated.setIsActive(true);
        when(accountService.updateAccount(any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/account/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.name").value("Name"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @WithMockUser
    void updateAccount_shouldReturn403_whenMissingAuthority() throws Exception {
        UpdateAccountReq req = new UpdateAccountReq(2L, true, "Name", null, "a@a.com",
                null, "0900000000", null, null);

        mockMvc.perform(put("/api/v1/account/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "accounts:view")
    void listAccount_shouldReturn200() throws Exception {
        InfoAccountRes item = new InfoAccountRes();
        item.setId(7L);
        item.setEmail("listed@example.com");
        when(accountService.listAccount(any())).thenReturn(PagedResponse.<InfoAccountRes>builder()
                .items(List.of(item))
                .currentPage(1)
                .limit(10)
                .totalItems(1L)
                .totalPage(1)
                .build());

        mockMvc.perform(get("/api/v1/account/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(7))
                .andExpect(jsonPath("$.data.items[0].email").value("listed@example.com"))
                .andExpect(jsonPath("$.data.totalItems").value(1));
    }


    @Test
    @WithMockUser(authorities = "accounts:delete")
    void deleteAccount_shouldReturn200_whenAuthorized() throws Exception {
        DeleteAccountReq req = new DeleteAccountReq();
        req.setAccountIds(List.of(2L, 3L));

        mockMvc.perform(delete("/api/v1/account/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isError").value(false));
    }

    @Test
    @WithMockUser
    void deleteAccount_shouldReturn403_whenMissingAuthority() throws Exception {
        DeleteAccountReq req = new DeleteAccountReq();
        req.setAccountIds(List.of(2L));

        mockMvc.perform(delete("/api/v1/account/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }
}
