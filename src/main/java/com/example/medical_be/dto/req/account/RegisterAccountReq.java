package com.example.medical_be.dto.req.account;
public record RegisterAccountReq (
        String email,
        String password,
        String repeatPassword
){
}
