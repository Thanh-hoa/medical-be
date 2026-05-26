package com.example.medical_be.service;

import org.springframework.web.multipart.MultipartFile;

import com.example.medical_be.dto.req.medicalRecord.MedicalRecordListReq;
import com.example.medical_be.dto.req.medicalRecord.OcrResultReq;
import com.example.medical_be.dto.req.medicalRecord.RejectMedicalRecordReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateExtractedFieldReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateMedicalRecordDetailReq;
import com.example.medical_be.dto.res.MedicalRecordDetailRes;
import com.example.medical_be.dto.res.MedicalRecordSummaryRes;
import com.example.medical_be.dto.res.PagedResponse;

public interface IMedicalRecordService {
    
    MedicalRecordDetailRes upload(MultipartFile file);

    MedicalRecordSummaryRes processOcrResult(OcrResultReq req);

    PagedResponse<MedicalRecordSummaryRes> list(MedicalRecordListReq req);

    MedicalRecordDetailRes detail(Long id);

    MedicalRecordDetailRes updateDetail(UpdateMedicalRecordDetailReq req);

    void updateExtractedField(UpdateExtractedFieldReq req);

    MedicalRecordSummaryRes submitForReview(Long id);

    MedicalRecordSummaryRes approve(Long id);

    MedicalRecordSummaryRes reject(RejectMedicalRecordReq req);

    void delete(Long id);
}
