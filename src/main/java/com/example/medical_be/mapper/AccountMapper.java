package com.example.medical_be.mapper;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.entity.Account;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    InfoAccountRes toInfoAccount(Account account);

}
