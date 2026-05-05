package com.example.medical_be.validation;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.support.AccountSupportCreateToken;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActiveAccountValidate {

    private final IMessageTranslator iMessageTranslator;
    private final AccountSupportCreateToken accountHelperService;

    public String validateToken(String token){

        String rawToken = new String(Base64.getDecoder().decode(token) , StandardCharsets.UTF_8);
        String[] parts = rawToken.split(":");
        if(parts.length != 2){
            throw new ApplicationException(iMessageTranslator.getMessage("token.invalid"));
        }
        String email = parts[0];
        String signature = parts[1];
        String expectedSignature = accountHelperService.sign(email);
        if(!checkSignatureAndExpectedSignature(signature, expectedSignature)){
            throw new ApplicationException(iMessageTranslator.getMessage("token.invalid"));
        }
        return email;
    }
    
    private boolean checkSignatureAndExpectedSignature(String signature , String expectedSignature){
        if(signature.length() != expectedSignature.length()){
            return false;
        }
        int isValidate = 0; 
        for(int i =0 ; i < signature.length() ; i++){
            isValidate |= signature.charAt(i) ^ expectedSignature.charAt(i);
        }
        return isValidate == 0;
    }
   
    
}
