package com.example.medical_be.service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.auth.AccountUserDetails;
import com.example.medical_be.auth.AccountUserDetailsService;
import com.example.medical_be.dto.req.account.ForgotPasswordReq;
import com.example.medical_be.dto.req.account.ResetPasswordReq;
import com.example.medical_be.dto.req.auth.LoginReq;
import com.example.medical_be.dto.req.auth.RefreshTokenReq;
import com.example.medical_be.dto.res.InfoLoginRes;
import com.example.medical_be.dto.res.InfoLogoutRes;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.jwt.JwtService;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.support.AccountSentMailHelperService;
import com.example.medical_be.support.AccountSupportCreateToken;
import com.example.medical_be.validation.AccountValidate;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IMessageTranslator messageTranslator;
    private final AccountUserDetailsService accountUserDetailsService;
    private final TokenValidationService managerTokenAccountService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountSupportCreateToken accountSupportCreateToken;
    private final AccountSentMailHelperService accountSentMailHelperService;
    private final AccountValidate accountValidate;

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
            throw new ApplicationException(messageTranslator.getMessage("login.account_inactive"));
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

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordReq req) {
        accountRepository.findByEmail(req.email()).ifPresent(account -> {
            if (!account.getIsDelete() && account.getIsActive()) {
                String token = accountSupportCreateToken.generatePasswordResetToken(req.email());
                accountSentMailHelperService.sendPasswordResetEmail(account, token);
            }
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReq req) {
        String email = accountSupportCreateToken.validatePasswordResetToken(req.token());
        if (email == null) {
            throw new ApplicationException(messageTranslator.getMessage("token.invalid"));
        }
        accountValidate.validatePasswordMatch(req.password(), req.repeatPassword());
        accountRepository.findByEmail(email).ifPresent(account -> {
            if (account.getIsDelete() || !account.getIsActive()) {
                throw new ApplicationException(messageTranslator.getMessage("account.not_found"));
            }
            account.setPassword(passwordEncoder.encode(req.password()));
            accountRepository.save(account);
        });
    }

}
