package com.example.medical_be.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.req.patient.CreatePatientReq;
import com.example.medical_be.dto.req.patient.PatientSearchReq;
import com.example.medical_be.dto.req.patient.UpdatePatiientReq;
import com.example.medical_be.dto.res.MedicalRecordSummaryRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.dto.res.PatientRes;
import com.example.medical_be.dto.res.PatientWithRecordsRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.Patient;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.mapper.PatientMapper;
import com.example.medical_be.repository.MedicalRecordRepository;
import com.example.medical_be.repository.PatientRepository;
import com.example.medical_be.support.PaginationUtils;
import com.example.medical_be.validation.PatientValidate;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PatientService implements IPatientService {

    // static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    final PatientRepository patientRepository;
    final MedicalRecordRepository medicalRecordRepository;
    final IMessageTranslator messageTranslator;
    final PatientMapper patientMapper;
    final PatientValidate patientValidate;

   

    @Override
    @Transactional(readOnly = true)
    public PatientWithRecordsRes findByBhyt(String search) {
        Patient patient = patientRepository.findByBhyt(search)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("patient.not_found")));

        
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MedicalRecord> recordPage =
                medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId(), pageable);

        List<MedicalRecordSummaryRes> records = recordPage.getContent().stream()
                .map(this::toSummary)
                .toList();

        return PatientWithRecordsRes.builder()
                .patient(patientMapper.toRes(patient))
                .records(records)
                .totalRecords(recordPage.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PatientRes> search(PatientSearchReq req) {
        int page = PaginationUtils.normalizePage(req.page(), messageTranslator);
        int limit = PaginationUtils.normalizeLimit(req.limit(), messageTranslator);
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "name"));

        Page<Patient> pageRes = patientRepository.search(req.q(), pageable);
        List<PatientRes> items = pageRes.getContent().stream().map(patientMapper::toRes).toList();

        return PaginationUtils.buildPagedResponse(items, pageRes, req.page(), limit);
    }

    @Override
    @Transactional
    public PatientRes findOrCreate(CreatePatientReq req) {
        return patientRepository.findByBhyt(req.bhyt())
                .map(patientMapper::toRes)
                .orElseGet(() -> patientMapper.toRes(patientRepository.save(buildPatient(req))));
    }

    @Override
    @Transactional
    public PatientRes update(UpdatePatiientReq req) {
        Patient patient = patientRepository.findById(req.id())
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("patient.not_found")));

        if (!patient.getBhyt().equals(req.bhyt()) && patientRepository.existsByBhyt(req.bhyt())) {
            throw new ApplicationException(messageTranslator.getMessage("patient.bhyt.already_exists"));
        }

        patient.setBhyt(req.bhyt());
        patient.setName(req.name());
        patient.setDob(parseDob(req.dob()));
        patient.setGender(req.gender());
        patient.setAddress(req.address());
        patient.setPhone(req.phone());
        patient.setUpdatedAt(LocalDateTime.now());

        return patientMapper.toRes(patientRepository.save(patient));
    }

    private Patient buildPatient(CreatePatientReq req) {
        return Patient.builder()
                .bhyt(req.bhyt())
                .name(req.name())
                .dob(parseDob(req.dob()))
                .gender(req.gender())
                .address(req.address())
                .phone(req.phone())
                .build();
    }

    private LocalDate parseDob(String dob) {
        if (dob == null || dob.isBlank()) return null;
        try {
            return LocalDate.parse(dob, DOB_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ApplicationException(messageTranslator.getMessage("patient.dob.invalid_format"));
        }
    }


    private MedicalRecordSummaryRes toSummary(MedicalRecord m) {
        return MedicalRecordSummaryRes.builder()
                .id(m.getId())
                .recordNumber(m.getRecordNumber())
                .status(m.getStatus() != null ? m.getStatus().getDbValue() : null)
                .department(m.getDepartment())
                .recordType(m.getRecordType())
                .fileName(m.getFileName())
                .fileType(m.getFileType())
                .uploadedBy(m.getUploadedBy())
                .patient(patientMapper.toRes(patientValidate.validatePatientExist(m.getPatientId())))
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
