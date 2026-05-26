package com.example.medical_be.validation;

import org.springframework.stereotype.Component;

import com.example.medical_be.entity.Patient;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PatientValidate {
    private final PatientRepository patientRepository;
    private  final IMessageTranslator iMessageTranslator;

    public Patient validatePatientExist(Long patientId){
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ApplicationException(iMessageTranslator.getMessage("patient.not_found")));
        return patient;
    }

}
