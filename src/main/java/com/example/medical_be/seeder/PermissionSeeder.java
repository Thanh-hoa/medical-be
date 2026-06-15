package com.example.medical_be.seeder;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.entity.Permission;
import com.example.medical_be.repository.PermissionRepository;
import com.example.medical_be.support.constant.AccountConstant;
import com.example.medical_be.support.constant.AdminConstant;
import com.example.medical_be.support.constant.MedicalRecordConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionSeeder implements ISeeder {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void seed() {
        List<Permission> permissions = dataPermissions();
        permissions.forEach(this::updateOrCreate);
        log.info("Seeded {} permissions", permissions.size());
    }

    private void updateOrCreate(Permission permission) {
        permissionRepository.findBySlug(permission.getSlug())
                .ifPresentOrElse(
                        existing -> {
                            existing.setName(permission.getName());
                            existing.setSort(permission.getSort());
                            existing.setIsHidden(permission.getIsHidden());
                            permissionRepository.save(existing);
                            log.debug("Updated permission: {}", existing.getSlug());
                        },
                        () -> {
                            permissionRepository.save(permission);
                            log.debug("Created permission: {}", permission.getSlug());
                        }
                );
    }

    private List<Permission> dataPermissions() {
        return Arrays.asList(
                Permission.builder()
                        .slug(AccountConstant.ACCOUNT_MANAGEMENT)
                        .name("Quản lý Tài khoản")
                        .sort("1")
                        .isHidden(false)
                        .build(),
                Permission.builder()
                        .slug(MedicalRecordConstant.MEDICAL_RECORD)
                        .name("Quản lý Bệnh án")
                        .sort("2")
                        .isHidden(false)
                        .build(),
                Permission.builder()
                        .slug(MedicalRecordConstant.MEDICAL_RECORD_APPROVAL)
                        .name("Phê duyệt Bệnh án")
                        .sort("3")
                        .isHidden(false)
                        .build(),
                Permission.builder()
                        .slug(MedicalRecordConstant.PATIENT_SEARCH)
                        .name("Quản lý Bệnh nhân")
                        .sort("4")
                        .isHidden(false)
                        .build(),
                Permission.builder()
                        .slug(AdminConstant.AUDIT_LOGS)
                        .name("Nhật ký Hệ thống")
                        .sort("5")
                        .isHidden(false)
                        .build(),
                Permission.builder()
                        .slug(AdminConstant.DASHBOARD)
                        .name("Thống kê")
                        .sort("6")
                        .isHidden(false)
                        .build()
        );
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
