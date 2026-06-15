package com.example.medical_be.mapper;

import org.springframework.stereotype.Component;

import com.example.medical_be.dto.res.MedicalRecordDetailRes;
import com.example.medical_be.dto.res.MedicalRecordSummaryRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.validation.PatientValidate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedicalRecordMapper {

    private final PatientMapper patientMapper;
    private final PatientValidate patientValidate;

    public MedicalRecordDetailRes toDetail(MedicalRecord record) {
        MedicalRecordDetailRes.MedicalRecordDetailResBuilder builder = MedicalRecordDetailRes.builder()
                .id(record.getId())
                .recordNumber(record.getRecordNumber())
                .status(record.getStatus() != null ? record.getStatus().getDbValue() : null)
                .department(record.getDepartment())
                .recordType(record.getRecordType())
                .fileName(record.getFileName())
                .fileType(record.getFileType())
                .originalImagePath(record.getOriginalImagePath())
                .notes(record.getNotes())
                .uploadedBy(record.getUploadedBy())
                .verifiedBy(record.getVerifiedBy())
                .verifiedAt(record.getVerifiedAt())
                .approvedBy(record.getApprovedBy())
                .approvedAt(record.getApprovedAt())
                .rejectedBy(record.getRejectedBy())
                .rejectedAt(record.getRejectedAt())
                .rejectionReason(record.getRejectionReason())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .extractedData(record.getExtractedData())
                .labData(record.getLabData());

        if (record.getPatientId() != null) {
            builder.patient(patientMapper.toRes(patientValidate.validatePatientExist(record.getPatientId())));
        }

        return builder.build();
    }

    public MedicalRecordSummaryRes toSummary(MedicalRecord record) {
        return MedicalRecordSummaryRes.builder()
                .id(record.getId())
                .recordNumber(record.getRecordNumber())
                .status(record.getStatus() != null ? record.getStatus().getDbValue() : null)
                .department(record.getDepartment())
                .recordType(record.getRecordType())
                .fileName(record.getFileName())
                .fileType(record.getFileType())
                .uploadedBy(record.getUploadedBy())
                .patient(record.getPatientId() != null
                        ? patientMapper.toRes(patientValidate.validatePatientExist(record.getPatientId()))
                        : null)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
