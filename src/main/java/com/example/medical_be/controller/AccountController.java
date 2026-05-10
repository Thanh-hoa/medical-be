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
import com.example.medical_be.validation.ActiveAccountValidate;
import com.example.medical_be.routes.APIRoutes;
import lombok.RequiredArgsConstructor;
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
public class AccountController {

    private final ActiveAccountValidate activeAccountValidate;
    private final IAccountService accountService;
    private  final IMessageTranslator iMessageTranslator;


    @PostMapping(APIRoutes.REGISTER)
    public ResponseEntity<JSONResponse<?>> registerAccount(@RequestBody RegisterAccountReq req){
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.create_success"))
                        .data(accountService.registerAccount(req))
                        .build());
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping(APIRoutes.CREATE_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> createAccount(@Valid @RequestBody CreateAccountReq req) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.create_success"))
                        .data(accountService.createAccount(req))
                        .build());
    }

    @GetMapping(APIRoutes.VALIDATE_TOKEN)
    public ResponseEntity<JSONResponse<?>> validateToken(@RequestParam String token) {
        activeAccountValidate.validateToken(token);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.activation_success"))
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(APIRoutes.PROFILE)
    public ResponseEntity<JSONResponse<?>> getInfoProfile() {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.get_profile_success"))
                        .data(accountService.getInfoProfile())
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(APIRoutes.PROFILE)
    public ResponseEntity<JSONResponse<?>> updateProfile(@Valid @RequestBody UpdateProfileReq req) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.update_profile_success"))
                        .data(accountService.updateProfile(req))
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(APIRoutes.DETAIL_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> detailAccount(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.detail_success"))
                        .data(accountService.detailAccount(id))
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping(APIRoutes.UPDATE_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> updateAccount(@Valid @RequestBody UpdateAccountReq req) {
        return ResponseEntity.ok(JSONResponse.<InfoAccountRes>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.update_success"))
                        .data(accountService.updateAccount(req))
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(APIRoutes.LIST_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> listAccount(@ModelAttribute AccountListReq filter) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<InfoAccountRes>>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.list_success"))
                        .data(accountService.listAccount(filter))
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping(APIRoutes.DELETE_ACCOUNT)
    public ResponseEntity<JSONResponse<?>> deleteAccount(@Valid @RequestBody DeleteAccountReq req) {
        accountService.deleteAccount(req);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                        .isError(false)
                        .message(iMessageTranslator.getMessage("account.delete_success"))
                        .build());
    }

}
