package com.example.medical_be.validation;

import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountValidate {
    private final AccountRepository accountRepository;
    private  final IMessageTranslator iMessageTranslator;

    public void validatePasswordMatch(String password , String repeatPassword){
        if(!password.equals(repeatPassword)){
            throw new ApplicationException(iMessageTranslator.getMessage("account.password_not_match"));
        }
    }

    public void validateEmailNoExistsForRegister(String email ){
        if(accountRepository.findByEmailAndIsActive(email , true).isPresent()){
            throw  new ApplicationException(iMessageTranslator.getMessage("account.email_existed"));
        }
    }
    
}
