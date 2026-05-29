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

    private final IMessageTranslator messageTranslator;

    public Long getCurrentAccountId() {
        return currentUser().id();
    }

    public AccountUserDetails currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountUserDetails u)
            return u;
        throw new ApplicationException(messageTranslator.getMessage("account.not_found"));
    }

    public boolean hasRole(String role) {
        return currentUser().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean isAdmin() { return hasRole("admin"); }
    public boolean isDoctor() { return hasRole("doctor"); }
    public boolean isEmployee() { return hasRole("employee"); }
}
