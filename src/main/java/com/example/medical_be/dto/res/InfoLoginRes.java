package com.example.medical_be.dto.res;
import lombok.Builder;

@Builder
public record InfoLoginRes(
        String token,
        String refreshToken,
        Long userId,
        String username
) {
}