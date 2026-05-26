package com.example.medical_be.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.medical_be.auth.AccountUserDetails;
import com.example.medical_be.dto.json.LabResultJson;
import com.example.medical_be.dto.req.medicalRecord.MedicalRecordListReq;
import com.example.medical_be.dto.req.medicalRecord.OcrResultReq;
import com.example.medical_be.dto.req.medicalRecord.RejectMedicalRecordReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateExtractedFieldReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateMedicalRecordDetailReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateMedicalRecordPatientReq;
import com.example.medical_be.dto.req.patient.CreatePatientReq;
import com.example.medical_be.dto.req.patient.UpdatePatiientReq;
import com.example.medical_be.dto.res.MedicalRecordDetailRes;
import com.example.medical_be.dto.res.MedicalRecordSummaryRes;
import com.example.medical_be.dto.res.OcrResponse;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.dto.res.PatientRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.MedicalRecordStatus;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.mapper.PatientMapper;
import com.example.medical_be.repository.MedicalRecordRepository;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.support.PaginationUtils;
import com.example.medical_be.validation.PatientValidate;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MedicalRecordService implements IMedicalRecordService {

    static final DateTimeFormatter PATIENT_DOB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final Map<String, String> SORT_MAP = Map.of(
            "created_at", "createdAt",
            "status", "status",
            "record_number", "recordNumber");

    final MedicalRecordRepository medicalRecordRepository;
    final PatientService patientService;
    final OcrService ocrService;
    final AccountSupport accountSupport;
    final IMessageTranslator messageTranslator;
    final PatientValidate patientValidate;
    final PatientMapper patientMapper;

    @Value("${app.upload-dir:uploads/photos}")
    String uploadDir;

    @Override
    public MedicalRecordDetailRes upload(MultipartFile file) {
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String savedName = UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir);
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve(savedName), file.getBytes());
        } catch (IOException e) {
            throw new ApplicationException(messageTranslator.getMessage("file.upload_failed"));
        }

        String savedPath = uploadDir + "/" + savedName;
        Long currentId = accountSupport.getCurrentAccountId();

        MedicalRecord record = MedicalRecord.builder()
                .recordNumber(generateRecordNumber())
                .uploadedBy(currentId)
                .fileName(original)
                .fileType(file.getContentType())
                .originalImagePath(savedPath)
                .patientId(null)
                .status(MedicalRecordStatus.PROCESSING)
                .build();
        record = medicalRecordRepository.save(record);

        OcrResponse ocrResponse;
        try {
            ocrResponse = ocrService.processImage(file);
        } catch (Exception e) {
            throw new ApplicationException(messageTranslator.getMessage("record.ocr.failed"));
        }

        if (ocrResponse != null && ocrResponse.parsedData() != null) {
            Map<String, String> extractedData = ocrResponse.parsedData().extractedData();
            List<LabResultJson> labData = ocrResponse.parsedData().labData();

            record.setExtractedData(extractedData != null ? extractedData : new HashMap<>());
            record.setLabData(labData != null ? labData : new ArrayList<>());

            if (extractedData != null) {
                if (record.getDepartment() == null) {
                    record.setDepartment(extractedData.get("department"));
                }
                String bhyt = extractedData.get("patient_bhyt");
                String name = extractedData.get("patient_name");
                if (bhyt != null && !bhyt.isBlank() && name != null && !name.isBlank()) {
                    String dob = extractedData.get("patient_dob");
                    PatientRes patient = patientService.findOrCreate(new CreatePatientReq(
                            bhyt,
                            name,
                            dob,
                            extractedData.get("patient_gender"),
                            extractedData.get("patient_address"),
                            null));
                    record.setPatientId(patient.getId());
                }
            }
        }

        record.setStatus(MedicalRecordStatus.EXTRACTED);
        record.setUpdatedAt(LocalDateTime.now());
        return buildDetail(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes processOcrResult(OcrResultReq req) {
        MedicalRecord record = medicalRecordRepository.findById(req.recordId())
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));

        if (record.getStatus() != MedicalRecordStatus.PROCESSING) {
            throw new ApplicationException(messageTranslator.getMessage("record.ocr.invalid_status"));
        }

        record.setExtractedData(req.extractedData());
        record.setLabData(req.labData());

        if (record.getDepartment() == null && req.extractedData() != null) {
            record.setDepartment(req.extractedData().get("department"));
        }

        if (req.extractedData() != null) {
            String bhyt = req.extractedData().get("patient_bhyt");
            String name = req.extractedData().get("patient_name");
            if (bhyt != null && !bhyt.isBlank() && name != null && !name.isBlank()) {
                PatientRes patient = patientService.findOrCreate(new CreatePatientReq(
                        bhyt,
                        name,
                        req.extractedData().get("patient_dob"),
                        req.extractedData().get("patient_gender"),
                        req.extractedData().get("patient_address"),
                        null));
                record.setPatientId(patient.getId());
            }
        }

        record.setStatus(MedicalRecordStatus.EXTRACTED);
        record.setUpdatedAt(LocalDateTime.now());

        return toSummary(medicalRecordRepository.save(record));
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
        Page<MedicalRecord> pageRes;

        if (isEmployee()) {
            pageRes = medicalRecordRepository.findByUploadedBy(currentId, status, req.q(), pageable);
        } else {
            pageRes = medicalRecordRepository.findAll(status, req.patientId(), req.q(), pageable);
        }

        List<MedicalRecordSummaryRes> items = pageRes.getContent().stream().map(this::toSummary).toList();
        return PaginationUtils.buildPagedResponse(items, pageRes, req.page(), limit);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordDetailRes detail(Long id) {
        MedicalRecord record = findAccessibleRecord(id);
        return buildDetail(record);
    }


    @Override
    @Transactional
    public MedicalRecordDetailRes updateDetail(UpdateMedicalRecordDetailReq req) {
        MedicalRecord record = findAccessibleRecord(req.id());
        if (req.department() != null)
            record.setDepartment(req.department());
        if (req.recordType() != null)
            record.setRecordType(req.recordType());
        if (req.notes() != null)
            record.setNotes(req.notes());
        if (req.patient() != null) {
            PatientRes patient = upsertPatient(record, req.patient());
            record.setPatientId(patient.getId());
        }
        if (req.extractedData() != null) {
            record.setExtractedData(new HashMap<>(req.extractedData()));
        }
        if (req.labData() != null) {
            record.setLabData(new ArrayList<>(req.labData()));
        }

        syncPatientIntoExtractedData(record, req.patient());
        record.setUpdatedAt(LocalDateTime.now());

        MedicalRecord savedRecord = medicalRecordRepository.save(record);
        return buildDetail(savedRecord);
    }

    @Override
    @Transactional
    public void updateExtractedField(UpdateExtractedFieldReq req) {
        MedicalRecord record = findAccessibleRecord(req.recordId());
        //checkEditPermission(record);

        Map<String, String> data = record.getExtractedData();
        if (data == null)
            data = new HashMap<>();
        data.put(req.fieldName(), req.fieldValue());
        record.setExtractedData(data);
        record.setUpdatedAt(LocalDateTime.now());

        medicalRecordRepository.save(record);
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes submitForReview(Long id) {
        MedicalRecord record = findAccessibleRecord(id);

        if (!isEmployee()) {
            throw new ApplicationException(messageTranslator.getMessage("record.submit.not_allowed"));
        }
        if (record.getStatus() != MedicalRecordStatus.EXTRACTED) {
            throw new ApplicationException(messageTranslator.getMessage("record.submit.invalid_status"));
        }

        record.setStatus(MedicalRecordStatus.PENDING_DOCTOR_REVIEW);
        record.setVerifiedBy(accountSupport.getCurrentAccountId());
        record.setVerifiedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return toSummary(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes approve(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));

        if (!isDoctor() && !isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.approve.not_allowed"));
        }
        if (record.getStatus() != MedicalRecordStatus.PENDING_DOCTOR_REVIEW) {
            throw new ApplicationException(messageTranslator.getMessage("record.approve.invalid_status"));
        }

        record.setStatus(MedicalRecordStatus.APPROVED);
        record.setApprovedBy(accountSupport.getCurrentAccountId());
        record.setApprovedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return toSummary(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public MedicalRecordSummaryRes reject(RejectMedicalRecordReq req) {
        MedicalRecord record = medicalRecordRepository.findById(req.id())
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));

        if (!isDoctor() && !isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.reject.not_allowed"));
        }
        if (record.getStatus() != MedicalRecordStatus.PENDING_DOCTOR_REVIEW) {
            throw new ApplicationException(messageTranslator.getMessage("record.reject.invalid_status"));
        }

        record.setStatus(MedicalRecordStatus.REJECTED);
        record.setRejectionReason(req.rejectionReason());
        record.setUpdatedAt(LocalDateTime.now());

        return toSummary(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!isAdmin()) {
            throw new ApplicationException(messageTranslator.getMessage("record.delete.not_allowed"));
        }
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));
        medicalRecordRepository.delete(record);
    }

    private MedicalRecord findAccessibleRecord(Long id) {
        if (isEmployee()) {
            return medicalRecordRepository.findByIdAndUploadedBy(id, accountSupport.getCurrentAccountId())
                    .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));
        }
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("record.not_found")));
    }

    // private void checkEditPermission(MedicalRecord record) {
    //     if (isAdmin())
    //         return;

    //     MedicalRecordStatus status = record.getStatus();
    //     if (isDoctor()) {
    //         if (status != MedicalRecordStatus.PENDING_DOCTOR_REVIEW) {
    //             throw new ApplicationException(messageTranslator.getMessage("record.edit.doctor_invalid_status"));
    //         }
    //         return;
    //     }

    //     if (status != MedicalRecordStatus.PROCESSING
    //             && status != MedicalRecordStatus.EXTRACTED
    //             && status != MedicalRecordStatus.REJECTED) {
    //         throw new ApplicationException(messageTranslator.getMessage("record.edit.employee_invalid_status"));
    //     }
    // }

    private AccountUserDetails currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AccountUserDetails u)
            return u;
        throw new ApplicationException(messageTranslator.getMessage("account.not_found"));
    }

    private boolean hasRole(String role) {
        return currentUser().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private boolean isAdmin() {
        return hasRole("admin");
    }

    private boolean isDoctor() {
        return hasRole("doctor");
    }

    private boolean isEmployee() {
        return hasRole("employee");
    }

    private MedicalRecordStatus parseStatus(String status) {
        if (status == null || status.isBlank())
            return null;
        try {
            return MedicalRecordStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(messageTranslator.getMessage("record.status.invalid"));
        }
    }

    private String generateRecordNumber() {
        String base = "REC-" + DateTimeFormatter.ofPattern("yyyy").format(LocalDateTime.now()) + "-";
        String candidate;
        do {
            candidate = base + String.format("%06d", (long) (Math.random() * 1_000_000));
        } while (medicalRecordRepository.existsByRecordNumber(candidate));
        return candidate;
    }

    private MedicalRecordDetailRes buildDetail(MedicalRecord record) {
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
                .rejectionReason(record.getRejectionReason())
                .uploadedBy(record.getUploadedBy())
                .verifiedBy(record.getVerifiedBy())
                .verifiedAt(record.getVerifiedAt())
                .approvedBy(record.getApprovedBy())
                .approvedAt(record.getApprovedAt())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .extractedData(record.getExtractedData())
                .labData(record.getLabData());

        if (record.getPatientId() != null) {
           builder.patient(patientMapper.toRes(patientValidate.validatePatientExist(record.getPatientId())));
        }

        return builder.build();
    }

    private PatientRes upsertPatient(MedicalRecord record, UpdateMedicalRecordPatientReq req) {
        if (req.bhyt() == null || req.bhyt().isBlank() || req.name() == null || req.name().isBlank()) {
            throw new ApplicationException(messageTranslator.getMessage("patient.not_found"));
        }
            return patientService.update(new UpdatePatiientReq(
                    record.getPatientId(),
                    req.bhyt(),
                    req.name(),
                    req.dob(),
                    req.gender(),
                    req.address(),
                    req.phone()));
        
    }

    private void syncPatientIntoExtractedData(MedicalRecord record, UpdateMedicalRecordPatientReq patientReq) {
        if (patientReq == null) {
            return;
        }

        Map<String, String> data = record.getExtractedData();
        if (data == null) {
            data = new HashMap<>();
            record.setExtractedData(data);
        }

        putIfPresent(data, "patient_bhyt", patientReq.bhyt());
        putIfPresent(data, "patient_name", patientReq.name());
        putIfPresent(data, "patient_dob", normalizePatientDob(patientReq.dob()));
        putIfPresent(data, "patient_gender", patientReq.gender());
        putIfPresent(data, "patient_address", patientReq.address());
        putIfPresent(data, "patient_phone", patientReq.phone());
    }

    private void putIfPresent(Map<String, String> data, String key, String value) {
        if (value != null) {
            data.put(key, value);
        }
    }

    private String normalizePatientDob(String dob) {
        if (dob == null || dob.isBlank()) {
            return dob;
        }
        try {
            LocalDate parsed = LocalDate.parse(dob, PATIENT_DOB_FORMATTER);
            return parsed.format(PATIENT_DOB_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ApplicationException(messageTranslator.getMessage("patient.dob.invalid_format"));
        }
    }

    public MedicalRecordSummaryRes toSummary(MedicalRecord m) {

        return MedicalRecordSummaryRes.builder()
                .id(m.getId())
                .recordNumber(m.getRecordNumber())
                .status(m.getStatus() != null ? m.getStatus().getDbValue() : null)
                .department(m.getDepartment())
                .recordType(m.getRecordType())
                .fileName(m.getFileName())
                .fileType(m.getFileType())
                .uploadedBy(m.getUploadedBy())
                .patient(
                        patientMapper.toRes(
                                patientValidate.validatePatientExist(m.getPatientId())))
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
