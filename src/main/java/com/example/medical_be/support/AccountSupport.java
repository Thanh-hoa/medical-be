package com.example.medical_be.support;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.medical_be.auth.AccountUserDetails;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Component
public class AccountSupport {

    private final IMessageTranslator messageTranslator ;
    public Long getCurrentAccountId(){
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !(auth.getPrincipal() instanceof AccountUserDetails userDetails)){
            throw new ApplicationException(messageTranslator.getMessage("account.not_found"));
        }
        return userDetails.id();
    }
    
}
