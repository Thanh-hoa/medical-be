package com.example.medical_be.service;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.medical_be.entity.ManagerTokenAccount;
import com.example.medical_be.repository.ManagerTokenAccountRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenValidationService {
    private final ManagerTokenAccountRepository managerTokenAccountRepository;
    private final RedisTokenService redisTokenService;


    public boolean isValidToken(String token) {
        // 1. Kiểm tra Redis trước (nhanh hơn)
        try {
            if (redisTokenService.isRedisAvailable()) {
                boolean redisValid = redisTokenService.isValidToken(token);
                if (!redisValid) {
                    log.debug("Token not found or blacklisted in Redis");
                }
                return redisValid;
            }
        } catch (Exception e) {
            log.warn("Redis check failed, falling back to DB: {}", e.getMessage());
        }

        // 2. Fallback: kiểm tra DB
        Optional<ManagerTokenAccount> tokenAccount = managerTokenAccountRepository.findByTokenAndIsActive(token , true);
        return tokenAccount.map(managerTokenAccount -> 
                managerTokenAccount.getExpiredAt().isAfter(LocalDateTime.now())
        ).orElse(false);
    }

    @Transactional
    public void saveToken(Long accountId, String token , LocalDateTime expiredAt){ 
        // 1. Lưu vào Redis trước (nhanh, TTL tự động)
        try {
            redisTokenService.saveToken(accountId, token, expiredAt);
        } catch (Exception e) {
            log.warn("Failed to save token to Redis, falling back to DB: {}", e.getMessage());
        }

        // 2. Luôn lưu vào DB để backup
        ManagerTokenAccount managerTokenAccount = ManagerTokenAccount.builder()
                .accountId(accountId)
                .token(token)
                .expiredAt(expiredAt)
                .isActive(true)
                .build();
        managerTokenAccountRepository.save(managerTokenAccount);
    }
    
     @Transactional
    public void deactivateToken(String token) {
        // 1. Vô hiệu hóa trong Redis
        try {
            redisTokenService.deactivateToken(token);
        } catch (Exception e) {
            log.warn("Failed to deactivate token in Redis, falling back to DB: {}", e.getMessage());
        }

        // 2. Luôn vô hiệu hóa trong DB
        managerTokenAccountRepository.deactivateToken(token);
    }
}
