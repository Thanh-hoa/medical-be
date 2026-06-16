package com.example.medical_be.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.account.ForgotPasswordReq;
import com.example.medical_be.dto.req.account.ResetPasswordReq;
import com.example.medical_be.dto.req.auth.LoginReq;
import com.example.medical_be.dto.req.auth.RefreshTokenReq;
import com.example.medical_be.dto.res.InfoLoginRes;
import com.example.medical_be.dto.res.InfoLogoutRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.IAuthService;
import com.example.medical_be.swagger.AuthApiExamples;
import com.example.medical_be.swagger.GroupAPIConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.AUTHENTICATION , description = "Các API liên quan đến xác thực người dùng như đăng nhập, làm mới token và đăng xuất")
public class AuthenController {
    private final IAuthService authService;
    private final IMessageTranslator messageTranslator;

    @Operation(summary = "Login", description = "Đăng nhập bằng email và mật khẩu, trả về JWT token và refresh token")
    @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.LOGIN_SUCCESS)))
    @PostMapping(APIRoutes.LOGIN)
    public ResponseEntity<JSONResponse<?>> login(@RequestBody LoginReq request) {
        return ResponseEntity.ok(JSONResponse.<InfoLoginRes>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("login.success"))
                        .data(authService.login(request))
                        .build());
    }

    @Operation(summary = "Refresh token", description = "Dùng refresh token để lấy JWT token mới khi token cũ hết hạn")
    @ApiResponse(responseCode = "200", description = "Làm mới token thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.REFRESH_TOKEN_SUCCESS)))
    @PostMapping(APIRoutes.REFRESH_TOKEN)
    public ResponseEntity<JSONResponse<?>> refreshToken(@RequestBody RefreshTokenReq refreshToken) {
        return ResponseEntity.ok(JSONResponse.<InfoLoginRes>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("refresh.token.success"))
                        .data(authService.refreshToken(refreshToken))
                        .build());
    }

    @Operation(summary = "Logout", description = "Đăng xuất và vô hiệu hóa token hiện tại")
    @ApiResponse(responseCode = "200", description = "Đăng xuất thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AuthApiExamples.LOGOUT_SUCCESS)))
    @PostMapping(APIRoutes.LOGOUT)
    public ResponseEntity<JSONResponse<?>> logout(@RequestBody String token) {
        return ResponseEntity.ok(JSONResponse.<InfoLogoutRes>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("logout.successful"))
                        .data(authService.logout(token))
                        .build());
    }

    @Operation(summary = "Forgot password", description = "Gửi email chứa link đặt lại mật khẩu (link hết hạn sau 15 phút)")
    @PostMapping(APIRoutes.FORGOT_PASSWORD)
    public ResponseEntity<JSONResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordReq req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("account.forgot_password_success"))
                        .build());
    }

    @Operation(summary = "Reset password", description = "Đặt lại mật khẩu mới bằng token nhận từ email")
    @PostMapping(APIRoutes.RESET_PASSWORD)
    public ResponseEntity<JSONResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("account.reset_password_success"))
                        .build());
    }

}