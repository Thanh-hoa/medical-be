package com.example.medical_be.service;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.example.medical_be.entity.ManagerTokenAccount;
import com.example.medical_be.repository.ManagerTokenAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenValidationService {
    private final ManagerTokenAccountRepository managerTokenAccountRepository;


    public boolean isValidToken(String token) {
        Optional<ManagerTokenAccount> tokenAccount = managerTokenAccountRepository.findByTokenAndIsActive(token , true);
              return tokenAccount.map(managerTokenAccount -> 
                managerTokenAccount.getExpiredAt().isAfter(LocalDateTime.now())
        ).orElse(false);
    }

    @Transactional
    public void saveToken(Long accountId, String token , LocalDateTime expiredAt){ 
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
        managerTokenAccountRepository.deactivateToken(token);
    }
}
