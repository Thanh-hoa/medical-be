package com.example.medical_be.controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.LoginReq;
import com.example.medical_be.dto.req.RefreshTokenReq;
import com.example.medical_be.dto.res.InfoLoginRes;
import com.example.medical_be.dto.res.InfoLogoutRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.IAuthService;

import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
public class AuthenController {
    private final IAuthService authService;
    private final IMessageTranslator messageTranslator;

    @PostMapping(APIRoutes.LOGIN)
    public ResponseEntity<JSONResponse<?>> login(@RequestBody LoginReq request) {
        return ResponseEntity.ok(JSONResponse.<InfoLoginRes>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("login.success"))
                        .data(authService.login(request))
                        .build());
    }

    @PostMapping(APIRoutes.REFRESH_TOKEN)
    public ResponseEntity<JSONResponse<?>> refreshToken(@RequestBody RefreshTokenReq refreshToken) {
        return ResponseEntity.ok(JSONResponse.<InfoLoginRes>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("refresh.token.success"))
                        .data(authService.refreshToken(refreshToken))
                        .build());
    }

    @PostMapping(APIRoutes.LOGOUT)
    public ResponseEntity<JSONResponse<?>> logout(@RequestBody String token) {
        return ResponseEntity.ok(JSONResponse.<InfoLogoutRes>builder()
                        .isError(false)
                        .message(messageTranslator.getMessage("logout.successful"))
                        .data(authService.logout(token))
                        .build());
    }

}