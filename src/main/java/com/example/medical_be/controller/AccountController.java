package com.example.medical_be.controller;
import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.AccountListReq;
import com.example.medical_be.dto.req.CreateAccountReq;
import com.example.medical_be.dto.req.DeleteAccountReq;
import com.example.medical_be.dto.req.RegisterAccountReq;
import com.example.medical_be.dto.req.UpdateAccountReq;
import com.example.medical_be.dto.req.UpdateProfileReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.service.IAccountService;
import com.example.medical_be.swagger.AccountApiExamples;
import com.example.medical_be.swagger.GroupAPIConstant;
import com.example.medical_be.validation.ActiveAccountValidate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.medical_be.routes.APIRoutes;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;


@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.ACCOUNT_MANAGEMENT, description = "Các API liên quan đến quản lý tài khoản người dùng")
public class AccountController {

    private final ActiveAccountValidate activeAccountValidate;
    private final IAccountService accountService;
    private  final IMessageTranslator iMessageTranslator;

    @Operation(
            summary = "Register new account",
            description = "Create a new user account with email verification"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Account created successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.REGISTER_SUCCESS)
            )
    )
    @PostMapping(APIRoutes.REGISTER)
    public ResponseEntity<JSONResponse<?>> registerAccount(@RequestBody RegisterAccountReq req){
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.create_success"))
                        .data(accountService.registerAccount(req))
                        .build());
    }


    @Operation(summary = "Create account", description = "Admin tạo tài khoản mới và gửi thông tin qua email")
    @ApiResponse(responseCode = "200", description = "Tạo tài khoản thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.CREATE_ACCOUNT_SUCCESS)))
    @PreAuthorize("hasAuthority('accounts:create')")
    @PostMapping(APIRoutes.CREATE_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> createAccount(@Valid @RequestBody CreateAccountReq req) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.create_success"))
                        .data(accountService.createAccount(req))
                        .build());
    }

    @Operation(summary = "Activate account", description = "Kích hoạt tài khoản qua token gửi về email")
    @ApiResponse(responseCode = "200", description = "Kích hoạt thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.VALIDATE_TOKEN_SUCCESS)))
    @GetMapping(APIRoutes.VALIDATE_TOKEN)
    public ResponseEntity<JSONResponse<?>> validateToken(@RequestParam String token) {
        activeAccountValidate.validateToken(token);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.activation_success"))
                        .build());
    }

    @Operation(summary = "Get my profile", description = "Lấy thông tin cá nhân của tài khoản đang đăng nhập")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.INFO_ACCOUNT_SUCCESS)))
    @PreAuthorize("isAuthenticated()")
    @GetMapping(APIRoutes.PROFILE)
    public ResponseEntity<JSONResponse<?>> getInfoProfile() {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.get_profile_success"))
                        .data(accountService.getInfoProfile())
                        .build());
    }

    @Operation(summary = "Update my profile", description = "Cập nhật thông tin cá nhân của tài khoản đang đăng nhập")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.UPDATE_SUCCESS)))
    @PreAuthorize("isAuthenticated()")
    @PutMapping(APIRoutes.PROFILE)
    public ResponseEntity<JSONResponse<?>> updateProfile(@Valid @RequestBody UpdateProfileReq req) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.update_profile_success"))
                        .data(accountService.updateProfile(req))
                        .build());
    }

    @Operation(summary = "Get account detail", description = "Lấy thông tin chi tiết của một tài khoản theo ID")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.INFO_ACCOUNT_SUCCESS)))
    @PreAuthorize("hasAuthority('accounts:view')")
    @GetMapping(APIRoutes.DETAIL_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> detailAccount(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.detail_success"))
                        .data(accountService.detailAccount(id))
                        .build());
    }

    @Operation(summary = "Update account", description = "Admin cập nhật thông tin tài khoản của người dùng khác")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.UPDATE_SUCCESS)))
    @PreAuthorize("hasAuthority('accounts:edit')")
    @PutMapping(APIRoutes.UPDATE_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> updateAccount(@Valid @RequestBody UpdateAccountReq req) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.update_success"))
                        .data(accountService.updateAccount(req))
                        .build());
    }

    @Operation(summary = "List accounts", description = "Lấy danh sách tài khoản với phân trang và tìm kiếm (loại trừ tài khoản đang đăng nhập)")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.LIST_ACCOUNT_SUCCESS)))
    @PreAuthorize("hasAuthority('accounts:view')")
    @GetMapping(APIRoutes.LIST_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> listAccount(@ModelAttribute AccountListReq filter) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<InfoAccountRes>>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.list_success"))
                        .data(accountService.listAccount(filter))
                        .build());
    }

    @Operation(summary = "Delete accounts", description = "Xóa mềm một hoặc nhiều tài khoản (không thể xóa tài khoản đang đăng nhập)")
    @ApiResponse(responseCode = "200", description = "Xóa thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = AccountApiExamples.DELETE_SUCCESS)))
    @PreAuthorize("hasAuthority('accounts:delete')")
    @DeleteMapping(APIRoutes.DELETE_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> deleteAccount(@Valid @RequestBody DeleteAccountReq req) {
        accountService.deleteAccount(req);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.delete_success"))
                        .build());
    }

}
