package com.example.medical_be.controller;
import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.CreateAccountReq;
import com.example.medical_be.dto.req.RegisterAccountReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.service.IAccountService;
import com.example.medical_be.validation.ActiveAccountValidate;
import com.example.medical_be.routes.APIRoutes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
    
    

}
