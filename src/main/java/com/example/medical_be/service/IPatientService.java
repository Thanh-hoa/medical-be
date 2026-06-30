package com.example.medical_be.service;

import com.example.medical_be.dto.req.patient.CreatePatientReq;
import com.example.medical_be.dto.req.patient.PatientSearchReq;
import com.example.medical_be.dto.req.patient.UpdatePatientReq;
import com.example.medical_be.dto.res.MedicalRecordSummaryPatient;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.dto.res.PatientRes;

public interface IPatientService {
    
    MedicalRecordSummaryPatient findByBhyt(String search);
    MedicalRecordSummaryPatient findByIdentifier(String search);
    PagedResponse<PatientRes> search(PatientSearchReq req);
    PatientRes findOrCreate(CreatePatientReq req);
    PatientRes update(UpdatePatientReq req);
    
}
