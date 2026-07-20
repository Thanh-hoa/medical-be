package com.example.medical_be.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.medical_be.dto.req.account.ChangePasswordReq;
import com.example.medical_be.dto.req.account.RegisterAccountReq;
import com.example.medical_be.dto.req.auth.LoginReq;
import com.example.medical_be.service.mailapi.IEmailService;
import com.example.medical_be.support.AccountSupportCreateToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
  Test nguyên luồng nghiệp vụ thật (Controller -> Service -> Repository -> Postgres thật qua
  Testcontainers), không mock tầng nào của app. IEmailService bị mock vì đây là hệ thống
  ngoài (SMTP thật) - mock đúng boundary thật sự "bên ngoài", không phải mock nội bộ app.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "app.key=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MTI=",
        "encryption.secret-key=01234567890123456789012345678912",
        "ocr.api.url=http://localhost:9999",
        "app.server.url=http://localhost:8080",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test-password"
})
class AccountFlowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AccountSupportCreateToken accountSupportCreateToken;

    @MockBean
    IEmailService emailService;

    @Test
    void registerActivateLoginChangePassword_shouldWorkEndToEnd() throws Exception {
        String email = "integration-user@example.com";

        // 1. Đăng ký -> account được tạo, ở trạng thái chưa active
        RegisterAccountReq registerReq = new RegisterAccountReq(email, "Password1", "Password1");
        mockMvc.perform(post("/api/v1/account/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isOk());

        // 2. Đăng nhập trước khi kích hoạt -> bị từ chối
        LoginReq loginBeforeActivate = new LoginReq(email, "Password1");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBeforeActivate)))
                .andExpect(status().is4xxClientError());

        // 3. Kích hoạt bằng token tính lại y hệt app (không cần đọc email thật)
        String activationToken = accountSupportCreateToken.generateTokenActiveAccount(email);
        mockMvc.perform(get("/api/v1/account/validate-token").param("token", activationToken))
                .andExpect(status().isOk());

        // 4. Đăng nhập sau khi kích hoạt -> thành công, lấy JWT thật
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBeforeActivate)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("data").get("token").asText();
        assertThat(accessToken).isNotBlank();

        // 5. Lấy profile bằng JWT thật -> đúng email vừa đăng ký
        mockMvc.perform(get("/api/v1/account/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        // 6. Đổi mật khẩu
        ChangePasswordReq changePasswordReq = new ChangePasswordReq("Password1", "NewPassword1", "NewPassword1");
        mockMvc.perform(put("/api/v1/account/change-password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordReq)))
                .andExpect(status().isOk());

        // 7. Đăng nhập lại bằng mật khẩu cũ -> bị từ chối
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBeforeActivate)))
                .andExpect(status().is4xxClientError());

        // 8. Đăng nhập bằng mật khẩu mới -> thành công
        LoginReq loginWithNewPassword = new LoginReq(email, "NewPassword1");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginWithNewPassword)))
                .andExpect(status().isOk());
    }
}
