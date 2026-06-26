package com.example.medical_be.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.medical_be.dto.json.ExtractedDataDto;
import com.example.medical_be.dto.json.LabResultJson;
import com.example.medical_be.dto.res.FileUploadInfo;
import com.example.medical_be.dto.req.medicalRecord.MedicalRecordListReq;
import com.example.medical_be.dto.req.medicalRecord.RejectMedicalRecordReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateExtractedFieldReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateMedicalRecordDetailReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateMedicalRecordPatientReq;
import com.example.medical_be.dto.req.patient.CreatePatientReq;
import com.example.medical_be.dto.req.patient.UpdatePatientReq;
import com.example.medical_be.dto.res.MedicalRecordDetailRes;
import com.example.medical_be.dto.res.MedicalRecordSummaryRes;
import com.example.medical_be.dto.res.OcrResponse;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.dto.res.PatientRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.enums.MedicalRecordStatus;
import com.example.medical_be.event.MedicalRecordUploadedEvent;
import com.example.medical_be.event.MedicalRecordSubmittedEvent;
import com.example.medical_be.event.MedicalRecordApprovedEvent;
import com.example.medical_be.event.MedicalRecordRejectedEvent;
import com.example.medical_be.event.MedicalRecordResubmittedEvent;
import com.example.medical_be.event.MedicalRecordDeletedEvent;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.mapper.MedicalRecordMapper;
import com.example.medical_be.repository.MedicalRecordRepository;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.support.PaginationUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MedicalRecordService implements IMedicalRecordService {

    static final Map<String, String> SORT_MAP = Map.of(
            "created_at", "createdAt",
            "status", "status",
            "record_number", "recordNumber");
    static final Set<String> KNOWN_OCR_KEYS = Set.of(
            "patient_name", "patient_bhyt", "patient_dob", "patient_gender", "patient_address",
            "facility", "department", "signer_name", "diagnosis");

    final MedicalRecordRepository medicalRecordRepository;
    final PatientService patientService;
    final OcrService ocrService;
    final FileStorageService fileStorageService;
    final AccountSupport accountSupport;
    final IMessageTranslator messageTranslator;
    final MedicalRecordMapper medicalRecordMapper;
    final ApplicationEventPublisher eventPublisher;
    final NotificationService notificationService;

    @Override
    @Transactional
    public MedicalRecordDetailRes upload(MultipartFile file) {
        FileUploadInfo uploadInfo = fileStorageService.store(file);

        MedicalRecord record = MedicalRecord.builder()
                .recordNumber(generateRecordNumber())
                .uploadedBy(accountSupport.getCurrentAccountId())
                .fileName(uploadInfo.fileName())
                .fileType(file.getContentType())
                .originalImagePath(uploadInfo.path())
                .patientId(null)
                .status(MedicalRecordStatus.EXTRACTED)
                .build();
        record = medicalRecordRepository.save(record);

        OcrResponse ocrResponse;
        try {
            ocrResponse = ocrService.processImage(file);
        } catch (Exception e) {
            throw new ApplicationException(messageTranslator.getMessage("record.ocr.failed"));
        }

        if (ocrResponse != null && ocrResponse.parsedData() != null) {
            Map<String, String> rawData = ocrResponse.parsedData().extractedData();
            ExtractedDataDto extracted = mapRawToExtractedData(rawData);
            List<LabResultJson> labData = ocrResponse.parsedData().labData();
            record.setExtractedData(extracted);
            record.setLabData(labData != null ? labData : new ArrayList<>());
            record.setDepartment(extracted.getDepartment());
            String bhyt = rawData != null ? rawData.get("patient_bhyt") : null;
            String name = rawData != null ? rawData.get("patient_name") : null;
            if (bhyt != null && !bhyt.isBlank() && name != null && !name.isBlank()) {
                PatientRes patient = patientService.findOrCreate(new CreatePatientReq(
                        bhyt, name,
                        rawData.get("patient_dob"),
                        rawData.get("patient_gender"),
                        rawData.get("patient_address"),
                        null));
                record.setPatientId(patient.getId());
            }
        }

        record = medicalRecordRepository.save(record);
        MedicalRecordDetailRes result = medicalRecordMapper.toDetail(record);
        eventPublisher.publishEvent(
                new MedicalRecordUploadedEvent(this, record, accountSupport.getCurrentAccountId())
        );
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MedicalRecordSummaryRes> list(MedicalRecordListReq req) {
        int page = PaginationUtils.normalizePage(req.page(), messageTranslator);
        int limit = PaginationUtils.normalizeLimit(req.limit(), messageTranslator);
        Sort sort = PaginationUtils.buildSort(req.sortBy(), req.orderBy(), SORT_MAP, "createdAt");
        Pageable pageable = PageRequest.of(page, limit, sort);

        MedicalRecordStatus status = parseStatus(req.status());
        Long currentId = accountSupport.getCurrentAccountId();
        Page<MedicalRecord> pageRes = accountSupport.isEmployee()
                ? medicalRecordRepository.findByUploadedBy(currentId, status, req.q(), pageable)
                : medicalRecordRepository.findAll(status, req.patientId(), req.q(), pageable);

        List<MedicalRecordSummaryRes> items = pageRes.getContent().stream()
                .map(medicalRecordMapper::toSummary).toList();
        return PaginationUtils.buildPagedResponse(items, pageRes, req.page(), limit);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MedicalRecordSummaryRes> listPendingReview(MedicalRecordListReq req) {
        int page = PaginationUtils.normalizePage(req.page(), messageTranslator);
        int limit = PaginationUtils.normalizeLimit(req.limit(), messageTranslator);
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<MedicalRecord> pageRes = medicalRecordRepository.findAll(
                MedicalRecordStatus.PENDING_DOCTOR_REVIEW, req.patientId(), req.q(), pageable);

        List<MedicalRecordSummaryRes> items = pageRes.getContent().stream()
                .map(medicalRecordMapper::toSummary).toList();
        return PaginationUtils.buildPagedResponse(items, pageRes, req.page(), limit);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDetailRes detail(Long id) {
        return medicalRecordMapper.toDetail(findAccessibleRecord(id));
    }

    @Override
    @Transactional
    public MedicalRecordDetailRes updateDetail(UpdateMedicalRecordDetailReq req) {
        MedicalRecord record = findAccessibleRecord(req.id());

        if (req.department() != null) record.setDepartment(req.department());
        if (req.recordType() != null) record.setRecordType(req.recordType());
        if (req.notes() != null) record.setNotes(req.notes());
        if (req.extractedData() != null) record.setExtractedData(req.extractedData());
        if (req.labData() != null) record.setLabData(new ArrayList<>(req.labData()));

        if (req.patient() != null) {
            record.setPatientId(upsertPatient(record, req.patient()).getId());
        }

        return medicalRecordMapper.toDetail(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public void updateExtractedField(UpdateExtractedFieldReq req) {
        MedicalRecord record = findAccessibleRecord(req.recordId());
        ExtractedDataDto data = record.getExtractedData() != null
                ? record.getExtractedData() : new ExtractedDataDto();
        setExtractedField(data, req.fieldName(), req.fieldValue());
        record.setExtractedData(data);
        medicalRecordRepository.save(record);
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes submitForReview(Long id) {
        if (!accountSupport.isEmployee() && !accountSupport.isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.submit.not_allowed"));
        }
        MedicalRecord record = findAccessibleRecord(id);
        if (record.getStatus() != MedicalRecordStatus.EXTRACTED) {
            throw new ApplicationException(messageTranslator.getMessage("record.submit.invalid_status"));
        }
        record.setStatus(MedicalRecordStatus.PENDING_DOCTOR_REVIEW);
        record.setVerifiedBy(accountSupport.getCurrentAccountId());
        record.setVerifiedAt(LocalDateTime.now());
        record = medicalRecordRepository.save(record);
        eventPublisher.publishEvent(
                new MedicalRecordSubmittedEvent(this, record, accountSupport.getCurrentAccountId())
        );
        return medicalRecordMapper.toSummary(record);
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes approve(Long id) {
        if (!accountSupport.isDoctor() && !accountSupport.isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.approve.not_allowed"));
        }
        MedicalRecord record = findById(id);
        if (record.getStatus() != MedicalRecordStatus.PENDING_DOCTOR_REVIEW) {
            throw new ApplicationException(messageTranslator.getMessage("record.approve.invalid_status"));
        }
        record.setStatus(MedicalRecordStatus.APPROVED);
        record.setApprovedBy(accountSupport.getCurrentAccountId());
        record.setApprovedAt(LocalDateTime.now());
        record = medicalRecordRepository.save(record);
        notificationService.markResourceAsReadForCurrentUser("MedicalRecord", record.getId());
        eventPublisher.publishEvent(
                new MedicalRecordApprovedEvent(this, record, accountSupport.getCurrentAccountId())
        );
        return medicalRecordMapper.toSummary(record);
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes reject(RejectMedicalRecordReq req) {
        if (!accountSupport.isDoctor() && !accountSupport.isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.reject.not_allowed"));
        }
        MedicalRecord record = findById(req.id());
        if (record.getStatus() != MedicalRecordStatus.PENDING_DOCTOR_REVIEW) {
            throw new ApplicationException(messageTranslator.getMessage("record.reject.invalid_status"));
        }
        record.setStatus(MedicalRecordStatus.REJECTED);
        record.setRejectedBy(accountSupport.getCurrentAccountId());
        record.setRejectedAt(LocalDateTime.now());
        record.setRejectionReason(req.rejectionReason());
        record = medicalRecordRepository.save(record);
        notificationService.markResourceAsReadForCurrentUser("MedicalRecord", record.getId());
        eventPublisher.publishEvent(
                new MedicalRecordRejectedEvent(this, record, accountSupport.getCurrentAccountId(), req.rejectionReason())
        );
        return medicalRecordMapper.toSummary(record);
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes resubmit(Long id) {
        if (!accountSupport.isEmployee() && !accountSupport.isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.resubmit.not_allowed"));
        }
        MedicalRecord record = findAccessibleRecord(id);
        if (record.getStatus() != MedicalRecordStatus.REJECTED) {
            throw new ApplicationException(messageTranslator.getMessage("record.resubmit.invalid_status"));
        }
        record.setStatus(MedicalRecordStatus.PENDING_DOCTOR_REVIEW);
        record.setRejectedBy(null);
        record.setRejectedAt(null);
        record.setRejectionReason(null);
        record.setVerifiedBy(accountSupport.getCurrentAccountId());
        record.setVerifiedAt(LocalDateTime.now());
        record = medicalRecordRepository.save(record);
        notificationService.markResourceAsReadForCurrentUser("MedicalRecord", record.getId());
        eventPublisher.publishEvent(
                new MedicalRecordResubmittedEvent(this, record, accountSupport.getCurrentAccountId())
        );
        return medicalRecordMapper.toSummary(record);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!accountSupport.isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.delete.not_allowed"));
        }
        MedicalRecord record = findById(id);
        record.setIsDelete(true);
        record.setDeletedAt(LocalDateTime.now());
        record = medicalRecordRepository.save(record);
        eventPublisher.publishEvent(
                new MedicalRecordDeletedEvent(this, record, accountSupport.getCurrentAccountId())
        );
    }

    private MedicalRecord findById(Long id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));
    }

    private MedicalRecord findAccessibleRecord(Long id) {
        if (accountSupport.isEmployee()) {
            return medicalRecordRepository.findByIdAndUploadedBy(id, accountSupport.getCurrentAccountId())
                    .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));
        }
        return findById(id);
    }

    private PatientRes upsertPatient(MedicalRecord record, UpdateMedicalRecordPatientReq req) {
        if (req.bhyt() == null || req.bhyt().isBlank() || req.name() == null || req.name().isBlank()) {
            throw new ApplicationException(messageTranslator.getMessage("patient.not_found"));
        }
        return patientService.update(new UpdatePatientReq(
                record.getPatientId(), req.bhyt(), req.name(),
                req.dob(), req.gender(), req.address(), req.phone()));
    }

    private ExtractedDataDto mapRawToExtractedData(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) return new ExtractedDataDto();
        Map<String, String> extra = new HashMap<>();
        raw.forEach((k, v) -> { if (!KNOWN_OCR_KEYS.contains(k)) extra.put(k, v); });
        return ExtractedDataDto.builder()
                .facility(raw.get("facility"))
                .department(normalizeDepartment(raw.get("department")))
                .signerName(raw.get("signer_name"))
                .diagnosis(raw.get("diagnosis"))
                .extra(extra.isEmpty() ? null : extra)
                .build();
    }

    private void setExtractedField(ExtractedDataDto dto, String fieldName, String value) {
        switch (fieldName) {
            case "facility"                      -> dto.setFacility(value);
            case "department"                    -> dto.setDepartment(value);
            case "signer_name", "signerName"     -> dto.setSignerName(value);
            case "diagnosis"                     -> dto.setDiagnosis(value);
            default -> {
                Map<String, String> extra = dto.getExtra() != null ? dto.getExtra() : new HashMap<>();
                extra.put(fieldName, value);
                dto.setExtra(extra);
            }
        }
    }

    private MedicalRecordStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return MedicalRecordStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(messageTranslator.getMessage("record.status.invalid"));
        }
    }

    private String normalizeDepartment(String value) {
        if (value == null || value.isBlank()) return value;
        return value.replaceAll("(?i)^(phòng|khoa)\\s*:\\s*", "").trim();
    }

    private String generateRecordNumber() {
        String base = "REC-" + DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now()) + "-";
        String candidate;
        do {
            candidate = base + String.format("%06d", (long) (Math.random() * 1_000_000));
        } while (medicalRecordRepository.existsByRecordNumber(candidate));
        return candidate;
    }
}
