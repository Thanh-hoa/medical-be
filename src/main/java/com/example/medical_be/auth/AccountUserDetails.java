package com.example.medical_be.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.medical_be.entity.Account;

public record AccountUserDetails(Long id, String username, String password, boolean enabled,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    public static AccountUserDetails of(Account account, List<String> permissionStrings) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // ROLE_<code> — dùng cho hasRole()
        if (account.getRfAccountRoles() != null) {
            account.getRfAccountRoles().stream()
                    .filter(r -> r.getRole() != null)
                    .map(r -> r.getRole().getCode() != null ? r.getRole().getCode() : r.getRole().getName())
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
                    .distinct()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        // slug:action — dùng cho hasAuthority(), ví dụ "accounts:view"
        if (permissionStrings != null) {
            permissionStrings.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> !s.isBlank())
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

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