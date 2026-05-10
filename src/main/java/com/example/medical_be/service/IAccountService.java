package com.example.medical_be.service;
import com.example.medical_be.dto.req.CreateAccountReq;
import com.example.medical_be.dto.req.RegisterAccountReq;
import com.example.medical_be.dto.req.AccountListReq;
import com.example.medical_be.dto.req.DeleteAccountReq;
import com.example.medical_be.dto.req.UpdateAccountReq;
import com.example.medical_be.dto.req.UpdateProfileReq;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.PagedResponse;

public interface IAccountService {
      public InfoAccountRes  registerAccount(RegisterAccountReq req);
      public InfoAccountRes createAccount(CreateAccountReq req);
      public void activeAccount(String token);
      public InfoAccountRes getInfoProfile();
      public InfoAccountRes updateProfile(UpdateProfileReq req);
      public InfoAccountRes detailAccount(Long id);
      public InfoAccountRes updateAccount(UpdateAccountReq input);
      public PagedResponse<InfoAccountRes> listAccount(AccountListReq filter);
      public void deleteAccount(DeleteAccountReq input);
}
