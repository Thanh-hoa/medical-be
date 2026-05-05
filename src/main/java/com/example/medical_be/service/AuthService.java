package com.example.medical_be.service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.medical_be.auth.AccountUserDetails;
import com.example.medical_be.auth.AccountUserDetailsService;
import com.example.medical_be.dto.req.LoginReq;
import com.example.medical_be.dto.req.RefreshTokenReq;
import com.example.medical_be.dto.res.InfoLoginRes;
import com.example.medical_be.dto.res.InfoLogoutRes;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.jwt.JwtService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IMessageTranslator messageTranslator;
    private final AccountUserDetailsService accountUserDetailsService;
    // private final PasswordEncoder passwordEncoder;
    // private final AccountRepository accountRepository;
    private final TokenValidationService managerTokenAccountService;

    @Override

    public InfoLoginRes login(LoginReq req) {
        Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        AccountUserDetails account = (AccountUserDetails) authentication.getPrincipal();
        String token = jwtService.generationToken(account.username(), account.id());
        String refreshToken = jwtService.generationRefreshToken(account.username(), account.id());

        managerTokenAccountService.saveToken(account.id(), token, jwtService.getExpirationToken());
        managerTokenAccountService.saveToken(account.id(), refreshToken, jwtService.getExpirationTokenRefresh());

        return InfoLoginRes.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(account.id())
                .username(account.username())
                .build();

    }

    @Override
    public InfoLoginRes refreshToken(RefreshTokenReq refreshToken) {
        if (!managerTokenAccountService.isValidToken(refreshToken.refreshToken())) {
            throw new ApplicationException(messageTranslator.getMessage("token.invalid"));
        }
        var jwt = jwtService.parseRefreshToken(refreshToken.refreshToken());
        Claims claims = jwt.getPayload();
        String username = claims.getSubject();
        Long userId = claims.get("uid", Long.class);

        AccountUserDetails userDetails = (AccountUserDetails) accountUserDetailsService.loadUserByUsername(username);
        if (!userDetails.isEnabled()) {
            throw new ApplicationException("login.account_inactive");
        }
        
        String newToken = jwtService.generationToken(username, userId);
        String newRefreshToken = jwtService.generationRefreshToken(username, userId);

        managerTokenAccountService.deactivateToken(refreshToken.refreshToken());
        managerTokenAccountService.saveToken(userId, newToken, jwtService.getExpirationToken());
        managerTokenAccountService.saveToken(userId, newRefreshToken, jwtService.getExpirationTokenRefresh());

        return InfoLoginRes.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .userId(userId)
                .username(username)
                .build();
    }

    @Override
    public InfoLogoutRes logout(String token) {

        managerTokenAccountService.deactivateToken(token);
        return InfoLogoutRes.builder()
                .error(false)
                .message(messageTranslator.getMessage("logout.successful"))
                .build();
    }

}
