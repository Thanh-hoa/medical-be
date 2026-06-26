package com.example.medical_be.mapper;

import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.entity.Account;
import com.example.medical_be.entity.RfAccountRole;
import com.example.medical_be.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "roles", source = "rfAccountRoles")
    InfoAccountRes toInfoAccount(Account account);

    default InfoAccountRes.Role rfAccountRoleToRole(RfAccountRole rfAccountRole) {
        if (rfAccountRole == null || rfAccountRole.getRole() == null) return null;
        Role role = rfAccountRole.getRole();
        return new InfoAccountRes.Role(role.getId(), role.getName());
    }
}
