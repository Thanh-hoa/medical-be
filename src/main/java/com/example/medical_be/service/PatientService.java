package com.example.medical_be.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.req.patient.CreatePatientReq;
import com.example.medical_be.dto.req.patient.PatientSearchReq;
import com.example.medical_be.dto.req.patient.UpdatePatientReq;
import com.example.medical_be.dto.res.MedicalRecordSummaryPatient;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.dto.res.PatientRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.Patient;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.mapper.PatientMapper;
import com.example.medical_be.repository.MedicalRecordRepository;
import com.example.medical_be.repository.PatientRepository;
import com.example.medical_be.support.PaginationUtils;

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

   

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordSummaryPatient findByBhyt(String search) {
        Patient patient = findPatientByBhyt(search);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MedicalRecord> recordPage =
                medicalRecordRepository.findByPatientIdOrderByCreatedAtDesc(patient.getId(), pageable);

        List<MedicalRecordSummaryPatient.Item> records = recordPage.getContent().stream()
                .map(this::toSummaryItem)
                .toList();

        return MedicalRecordSummaryPatient.builder()
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

        List<PatientRes> filtered = patientRepository.findAll().stream()
                .filter(patient -> matchesSearch(patient, req.q()))
                .sorted(Comparator.comparing(
                        Patient::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(patientMapper::toRes)
                .toList();

        int from = Math.min(page * limit, filtered.size());
        int to = Math.min(from + limit, filtered.size());
        List<PatientRes> items = filtered.subList(from, to);

        return PagedResponse.<PatientRes>builder()
                .items(items)
                .currentPage(req.page())
                .limit(limit)
                .totalItems((long) filtered.size())
                .totalPage((int) Math.ceil((double) filtered.size() / limit))
                .build();
    }

    @Override
    @Transactional
    public PatientRes findOrCreate(CreatePatientReq req) {
        Patient existingPatient = findPatientByBhytOrNull(req.bhyt());
        return existingPatient != null
                ? patientMapper.toRes(existingPatient)
                : patientMapper.toRes(patientRepository.save(buildPatient(req)));
    }

    @Override
    @Transactional
    public PatientRes update(UpdatePatientReq req) {
        Patient patient = patientRepository.findById(req.id())
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("patient.not_found")));

        if (!Objects.equals(patient.getBhyt(), req.bhyt()) && existsByBhyt(req.bhyt())) {
            throw new ApplicationException(messageTranslator.getMessage("patient.bhyt.already_exists"));
        }

        patient.setBhyt(req.bhyt());
        patient.setName(req.name());
        patient.setDob(parseDob(req.dob()));
        patient.setGender(req.gender());
        patient.setAddress(req.address());
        patient.setPhone(req.phone());

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

    private Patient findPatientByBhyt(String bhyt) {
        Patient patient = findPatientByBhytOrNull(bhyt);
        if (patient == null) {
            throw new ApplicationException(messageTranslator.getMessage("patient.not_found"));
        }
        return patient;
    }

    private Patient findPatientByBhytOrNull(String bhyt) {
        if (bhyt == null || bhyt.isBlank()) {
            return null;
        }
        return patientRepository.findAll().stream()
                .filter(patient -> bhyt.equals(patient.getBhyt()))
                .findFirst()
                .orElse(null);
    }

    private boolean existsByBhyt(String bhyt) {
        return findPatientByBhytOrNull(bhyt) != null;
    }

    private boolean matchesSearch(Patient patient, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String keyword = q.toLowerCase(Locale.ROOT);
        return containsIgnoreCase(patient.getName(), keyword) || containsIgnoreCase(patient.getBhyt(), keyword);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }


    private MedicalRecordSummaryPatient.Item toSummaryItem(MedicalRecord m) {
        var ext = m.getExtractedData();
        return MedicalRecordSummaryPatient.Item.builder()
                .id(m.getId())
                .recordNumber(m.getRecordNumber())
                .status(m.getStatus() != null ? m.getStatus().getDbValue() : null)
                .department(m.getDepartment() != null ? m.getDepartment() : (ext != null ? ext.getDepartment() : null))
                .signerName(ext != null ? ext.getSignerName() : null)
                .diagnosis(ext != null ? ext.getDiagnosis() : null)
                .build();
    }
}
