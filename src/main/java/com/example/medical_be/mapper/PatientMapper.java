package com.example.medical_be.mapper;

import org.mapstruct.Mapper;

import com.example.medical_be.dto.res.PatientRes;
import com.example.medical_be.entity.Patient;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    PatientRes toRes(Patient patient);
}
