package com.example.medical_be.service;
import com.example.medical_be.dto.req.CreateAccountReq;
import com.example.medical_be.dto.req.RegisterAccountReq;
import com.example.medical_be.dto.res.InfoAccountRes;

public interface IAccountService {
      public InfoAccountRes  registerAccount(RegisterAccountReq req);
      public InfoAccountRes createAccount(CreateAccountReq req);
      public void activeAccount(String token);
}
