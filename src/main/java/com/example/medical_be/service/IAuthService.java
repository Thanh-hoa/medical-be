package com.example.medical_be.service;
import com.example.medical_be.dto.req.auth.LoginReq;
import com.example.medical_be.dto.req.auth.RefreshTokenReq;
import com.example.medical_be.dto.res.InfoLoginRes;
import com.example.medical_be.dto.res.InfoLogoutRes;

public interface IAuthService {

    public InfoLoginRes login(LoginReq req);

    public InfoLoginRes refreshToken(RefreshTokenReq refreshToken);

    public InfoLogoutRes logout(String token);
    
}
