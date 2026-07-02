package com.example.medical_be.seeder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.entity.Permission;
import com.example.medical_be.entity.PermissionAction;
import com.example.medical_be.entity.PermissionRole;
import com.example.medical_be.entity.Role;
import com.example.medical_be.repository.PermissionActionRepository;
import com.example.medical_be.repository.PermissionRepository;
import com.example.medical_be.repository.PermissionRoleRepository;
import com.example.medical_be.repository.RoleRepository;
import com.example.medical_be.support.constant.AccountConstant;
import com.example.medical_be.support.constant.AdminConstant;
import com.example.medical_be.support.constant.MedicalRecordConstant;
import com.example.medical_be.support.constant.PermissionConstant;
import com.example.medical_be.support.constant.RoleConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionRoleSeeder implements ISeeder {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PermissionActionRepository permissionActionRepository;
    private final PermissionRoleRepository permissionRoleRepository;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void seed() {
        assignAdminPermissions();
        assignDoctorPermissions();
        assignEmployeePermissions();
        assignPatientPermissions();
        log.info("Seeded permission roles");
    }

    private void assignAdminPermissions() {
        Role admin = findRole(RoleConstant.ROLE_ADMIN);

        grant(admin, AccountConstant.ACCOUNT_MANAGEMENT,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.CREATE_PERMISSION,
                PermissionConstant.EDIT_PERMISSION, PermissionConstant.DELETE_PERMISSION);

        grant(admin, MedicalRecordConstant.MEDICAL_RECORD,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.CREATE_PERMISSION,
                PermissionConstant.EDIT_PERMISSION, PermissionConstant.DELETE_PERMISSION);

        grant(admin, MedicalRecordConstant.MEDICAL_RECORD_APPROVAL,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.CREATE_PERMISSION,
                PermissionConstant.CANCEL_PERMISSION , PermissionConstant.EDIT_PERMISSION, 
                PermissionConstant.DELETE_PERMISSION);

        grant(admin, MedicalRecordConstant.PATIENT_SEARCH,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.CREATE_PERMISSION,
                PermissionConstant.EDIT_PERMISSION, PermissionConstant.DELETE_PERMISSION);

        grant(admin, AdminConstant.AUDIT_LOGS,
                PermissionConstant.VIEW_PERMISSION);

        grant(admin, AdminConstant.DASHBOARD,
                PermissionConstant.VIEW_PERMISSION);
    }

 
    private void assignDoctorPermissions() {
        Role doctor = findRole(RoleConstant.ROLE_DOCTOR);

        grant(doctor, MedicalRecordConstant.MEDICAL_RECORD,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.EDIT_PERMISSION);

        grant(doctor, MedicalRecordConstant.MEDICAL_RECORD_APPROVAL,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.CREATE_PERMISSION,
                PermissionConstant.CANCEL_PERMISSION);

        grant(doctor, MedicalRecordConstant.PATIENT_SEARCH,
                PermissionConstant.VIEW_PERMISSION);

        // grant(doctor, AdminConstant.DASHBOARD,
        //         PermissionConstant.VIEW_PERMISSION);
    }

    private void assignEmployeePermissions() {
        Role employee = findRole(RoleConstant.ROLE_EMPLOYEE);

        grant(employee, MedicalRecordConstant.MEDICAL_RECORD,
                PermissionConstant.VIEW_PERMISSION, PermissionConstant.CREATE_PERMISSION,
                PermissionConstant.EDIT_PERMISSION);
    }


    private void assignPatientPermissions() {
        Role patient = findRole(RoleConstant.ROLE_PATIENT);

        grant(patient, MedicalRecordConstant.PATIENT_SELF,
                PermissionConstant.VIEW_PERMISSION);
    }

    private void grant(Role role, String permissionSlug, String... actionCodes) {
        Permission permission = permissionRepository.findBySlug(permissionSlug)
                .orElseThrow(() -> new IllegalStateException("Permission not found: " + permissionSlug));

        for (String actionCode : actionCodes) {
            PermissionAction action = permissionActionRepository.findByCode(actionCode)
                    .orElseThrow(() -> new IllegalStateException("Action not found: " + actionCode));

            if (!permissionRoleRepository.existsByPermissionIdAndPermissionActionIdAndRoleId(
                    permission.getId(), action.getId(), role.getId())) {
                permissionRoleRepository.save(PermissionRole.builder()
                        .permissionId(permission.getId())
                        .permissionActionId(action.getId())
                        .roleId(role.getId())
                        .build());
                log.debug("Granted {}:{} → {}", permissionSlug, actionCode, role.getCode());
            }
        }
    }

    private Role findRole(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + code));
    }

    @Override
    public int getOrder() {
        return 5;
    }

}
