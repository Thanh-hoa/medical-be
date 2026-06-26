package com.example.medical_be.service.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.medical_be.dto.res.MenuItemRes;
import com.example.medical_be.repository.PermissionRoleRepository;
import com.example.medical_be.support.AccountSupport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuPermissionService implements IMenuPermissionService {

    private record MenuDef(Long id, String key, String label, String path) {}

    private static final List<MenuDef> MENU_DEFINITIONS = List.of(
            new MenuDef(1L, "dashboard",                "Dashboard",                  "/dashboard"),
            new MenuDef(2L, "accounts",                 "Account Management",         "/accounts"),
            new MenuDef(3L, "medical-records",          "Medical Records",            "/medical-records"),
            new MenuDef(4L, "medical-records-approval", "Medical Records Approval",   "/medical-records-approval"),
            new MenuDef(5L, "patient-search",           "Patient Management",         "/patient-search"),
            new MenuDef(6L, "audit-logs",               "Audit Logs",                 "/audit-logs")
    );

    private final PermissionRoleRepository permissionRoleRepository;
    private final AccountSupport accountSupport;

    @Override
    public List<MenuItemRes> menuPermissionOfUser() {
        Long accountId = accountSupport.getCurrentAccountId();
        List<String> permissions = permissionRoleRepository.findPermissionStringsByAccountId(accountId);

        Map<String, List<String>> permByModule = new HashMap<>();
        for (String perm : permissions) {
            String[] parts = perm.split(":", 2);
            if (parts.length == 2) {
                permByModule.computeIfAbsent(parts[0], k -> new ArrayList<>()).add(parts[1]);
            }
        }

        return MENU_DEFINITIONS.stream()
                .filter(def -> {
                    List<String> actions = permByModule.get(def.key());
                    return actions != null && actions.contains("view");
                })
                .map(def -> new MenuItemRes(def.id(), def.key(), def.label(), def.path(), permByModule.get(def.key())))
                .toList();
    }
}
