package com.example.medical_be.auth;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.medical_be.entity.Account;
public record AccountUserDetails(Long id, String username, String password, boolean enabled,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {


    public static AccountUserDetails of(Account account) {
        List<GrantedAuthority> authorities = account.getRfAccountRoles() == null
                ? List.of()
                : account.getRfAccountRoles().stream()
                        .filter(r -> r.getRole() != null)
                .map(r -> r.getRole().getCode() != null ? r.getRole().getCode() : r.getRole().getName())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new AccountUserDetails(
                account.getId(),
                account.getUsername(),
                account.getPassword(),
                Boolean.TRUE.equals(account.getIsActive()),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        List<String> roles = authorities().stream().map(GrantedAuthority::getAuthority).toList();
        return "AccountUserDetails{id=" + id + ", username='" + username + "', roles=" + roles + "}";
    }

    
    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
  
}