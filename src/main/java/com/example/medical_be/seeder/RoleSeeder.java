package com.example.medical_be.seeder;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.entity.Role;
import com.example.medical_be.repository.RoleRepository;
import com.example.medical_be.support.constant.RoleConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements ISeeder {
  private final RoleRepository roleRepository;


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void seed() {
        List<Role> roles = dataRole();
        for (Role role : roles) {
            updateOrCreate(role);
        }
        log.info("Seeded {} roles", roles.size());
    }

    private void updateOrCreate(Role role){
        roleRepository.findByCode(role.getCode())
            .ifPresentOrElse(
                roleExisted -> updateRole(roleExisted , role),
                ()->  createRole(role));
    }

    private void updateRole(Role existingRole, Role newRole) {
        existingRole.setName(newRole.getName());
        existingRole.setIsActive(newRole.getIsActive());
        roleRepository.save(existingRole);
        log.debug("Updated role: {}", existingRole.getCode());
    }

    private void createRole(Role role) {
        roleRepository.save(role);
        log.debug("Created new role: {}", role.getCode());
    }


    private List<Role> dataRole(){
            return Arrays.asList(
                Role.builder()
                .name("Quản Trị Viên")
                .code(RoleConstant.ROLE_ADMIN)
                .isActive(true)
                .isSuperAdmin(true)
                .build(),
                Role.builder()
                .name("Bác Sĩ")
                .code(RoleConstant.ROLE_DOCTOR)
                .isActive(true)
                .isSuperAdmin(false)
                .build(),
                Role.builder()
                .name("Nhân Viên")
                .code(RoleConstant.ROLE_EMPLOYEE)
                .isActive(true)
                .isSuperAdmin(false)
                .build()
            );
    }
    @Override
    public int getOrder(){
        return 1;
    }
}
