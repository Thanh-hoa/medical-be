package com.example.medical_be.dto.req;
public record RegisterAccountReq (
        String email,
        String password,
        String repeatPassword
){
}
