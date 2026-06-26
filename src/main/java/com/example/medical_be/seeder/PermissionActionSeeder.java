package com.example.medical_be.seeder;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.medical_be.entity.PermissionAction;
import com.example.medical_be.repository.PermissionActionRepository;
import com.example.medical_be.support.constant.PermissionConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionActionSeeder implements ISeeder {
 private final PermissionActionRepository permissionRepository;

    @Override
    public void seed() {
        List<PermissionAction> permissions = dataPermissions();
        permissions.forEach(this::updateOrCreatePermission);
    }

    private void updateOrCreatePermission(PermissionAction permission) {
        permissionRepository.findByCode(permission.getCode())
                .ifPresentOrElse(
                        existingPermission -> updatePermission(existingPermission, permission),
                        () -> createPermission(permission)
                );
    }

    private void updatePermission(PermissionAction existingPermission, PermissionAction newPermission) {
        existingPermission.setCode(newPermission.getCode());
        permissionRepository.save(existingPermission);
        log.debug("Updated permission: {}", existingPermission.getCode());
    }

    private void createPermission(PermissionAction permission) {
        permissionRepository.save(permission);
        log.debug("Created new role: {}", permission.getCode());
    }

    private List<PermissionAction> dataPermissions() {
        return Arrays.asList(
                PermissionAction.builder()
                        .code(PermissionConstant.CREATE_PERMISSION)
                        .build(),
                PermissionAction.builder()
                        .code(PermissionConstant.EDIT_PERMISSION)
                        .build(),
                PermissionAction.builder()
                        .code(PermissionConstant.DELETE_PERMISSION)
                        .build(),
                PermissionAction.builder()
                        .code(PermissionConstant.VIEW_PERMISSION)
                        .build(),
                PermissionAction.builder()
                        .code(PermissionConstant.CANCEL_PERMISSION)
                        .build(),
                PermissionAction.builder()
                        .code(PermissionConstant.ROLLBACK_PERMISSION)
                        .build()
        );
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
