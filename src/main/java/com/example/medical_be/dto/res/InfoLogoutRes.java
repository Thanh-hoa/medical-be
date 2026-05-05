package com.example.medical_be.dto.res;
import lombok.Builder;

@Builder
public record InfoLogoutRes(
    boolean error,
    String message    
) {
}   
