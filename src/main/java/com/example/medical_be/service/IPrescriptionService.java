package com.example.medical_be.service;

import com.example.medical_be.dto.req.prescription.UpdatePrescriptionReq;
import com.example.medical_be.dto.res.PrescriptionPrintRes;
import com.example.medical_be.dto.res.PrescriptionRes;

public interface IPrescriptionService {
    PrescriptionRes createByMedicalRecord(Long recordId);
    PrescriptionRes getByMedicalRecord(Long recordId);
    PrescriptionRes update(Long id, UpdatePrescriptionReq req);
    PrescriptionRes issue(Long id);
    PrescriptionPrintRes getPrintData(Long id);
    PrescriptionPrintRes getOwnPrintData(Long accountId, Long medicalRecordId);
}
