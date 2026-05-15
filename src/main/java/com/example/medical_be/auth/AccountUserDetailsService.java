package com.example.medical_be.auth;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.PermissionRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PermissionRoleRepository permissionRoleRepository;
    private final IMessageTranslator messageTranslator;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var acc = accountRepository.findByEmailOrUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException(messageTranslator.getMessage("account.not_found")));

        List<String> permissions = permissionRoleRepository.findPermissionStringsByAccountId(acc.getId());
        return AccountUserDetails.of(acc, permissions);
    }
}
