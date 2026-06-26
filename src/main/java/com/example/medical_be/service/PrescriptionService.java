package com.example.medical_be.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.req.prescription.PrescriptionItemReq;
import com.example.medical_be.dto.req.prescription.UpdatePrescriptionReq;
import com.example.medical_be.event.PrescriptionCreatedEvent;
import com.example.medical_be.event.PrescriptionIssuedEvent;
import com.example.medical_be.event.PrescriptionPrintedEvent;
import com.example.medical_be.event.PrescriptionSavedDraftEvent;
import com.example.medical_be.dto.res.PrescriptionItemRes;
import com.example.medical_be.dto.res.PrescriptionPrintRes;
import com.example.medical_be.dto.res.PrescriptionRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.enums.MedicalRecordStatus;
import com.example.medical_be.entity.Prescription;
import com.example.medical_be.entity.enums.PrescriptionDurationOption;
import com.example.medical_be.entity.PrescriptionItem;
import com.example.medical_be.entity.enums.PrescriptionStatus;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.MedicalRecordRepository;
import com.example.medical_be.repository.PrescriptionRepository;
import com.example.medical_be.support.AccountSupport;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrescriptionService implements IPrescriptionService {

    PrescriptionRepository prescriptionRepository;
    MedicalRecordRepository medicalRecordRepository;
    AccountRepository accountRepository;
    AccountSupport accountSupport;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PrescriptionRes createByMedicalRecord(Long recordId) {
        return prescriptionRepository.findByMedicalRecordId(recordId)
                .map(this::toRes)
                .orElseGet(() -> {
                    MedicalRecord record = findApprovedRecord(recordId);
                    if (record.getPatientId() == null) {
                        throw new ApplicationException("Bệnh án chưa có thông tin bệnh nhân");
                    }

                    Long doctorId = accountSupport.getCurrentAccountId();
                    String doctorName = accountRepository.findById(doctorId)
                            .map(a -> a.getName())
                            .orElse("");

                    Prescription prescription = Prescription.builder()
                            .prescriptionNumber(generatePrescriptionNumber())
                            .medicalRecordId(recordId)
                            .patientId(record.getPatientId())
                            .doctorId(doctorId)
                            .status(PrescriptionStatus.DRAFT)
                            .hospitalName(record.getExtractedData() != null ? record.getExtractedData().getFacility() : null)
                            .receiverName(record.getPatient() != null ? record.getPatient().getName() : null)
                            .insuranceCode(record.getPatient() != null ? record.getPatient().getBhyt() : null)
                            .receiverAddress(record.getPatient() != null ? record.getPatient().getAddress() : null)
                            .diagnosis(record.getExtractedData() != null ? record.getExtractedData().getDiagnosis() : null)
                            .items(new ArrayList<>())
                            .build();

                    Prescription saved = prescriptionRepository.save(prescription);
                    eventPublisher.publishEvent(
                            new PrescriptionCreatedEvent(this, saved, doctorId));
                    PrescriptionRes res = toRes(saved);
                    res.setDoctorName(doctorName);
                    return res;
                });
    }

    @Override
    public PrescriptionRes getByMedicalRecord(Long recordId) {
        return prescriptionRepository.findByMedicalRecordId(recordId)
                .map(this::toRes)
                .orElse(null);
    }

    @Override
    @Transactional
    public PrescriptionRes update(Long id, UpdatePrescriptionReq req) {
        Prescription prescription = findDraftPrescription(id);

        prescription.setHospitalName(req.getHospitalName());
        prescription.setReceiverName(req.getReceiverName());
        prescription.setInsuranceCode(req.getInsuranceCode());
        prescription.setReceiverAddress(req.getReceiverAddress());
        prescription.setDiagnosis(req.getDiagnosis());
        prescription.setAdvice(req.getAdvice());

        if (req.getDurationOption() != null) {
            prescription.setDurationOption(req.getDurationOption());
            if (req.getDurationOption() == PrescriptionDurationOption.CUSTOM) {
                if (req.getDurationDays() == null || req.getDurationDays() < 1 || req.getDurationDays() > 365) {
                    throw new ApplicationException("Số ngày dùng thuốc không hợp lệ");
                }
                prescription.setDurationDays(req.getDurationDays());
            } else {
                prescription.setDurationDays(req.getDurationOption().getDefaultDays());
            }
        }

        if (req.getItems() != null) {
            prescription.getItems().clear();
            List<PrescriptionItem> newItems = buildItems(req.getItems(), prescription);
            prescription.getItems().addAll(newItems);
        }

        Prescription saved = prescriptionRepository.save(prescription);
        eventPublisher.publishEvent(
                new PrescriptionSavedDraftEvent(this, saved, accountSupport.getCurrentAccountId()));
        return toRes(saved);
    }

    @Override
    @Transactional
    public PrescriptionRes issue(Long id) {
        Prescription prescription = findDraftPrescription(id);

        if (prescription.getHospitalName() == null || prescription.getHospitalName().isBlank()) {
            throw new ApplicationException("Vui lòng nhập tên bệnh viện");
        }
        if (prescription.getReceiverName() == null || prescription.getReceiverName().isBlank()) {
            throw new ApplicationException("Vui lòng nhập tên người nhận");
        }
        if (prescription.getDiagnosis() == null || prescription.getDiagnosis().isBlank()) {
            throw new ApplicationException("Vui lòng nhập chẩn đoán");
        }
        if (prescription.getDurationDays() == null) {
            throw new ApplicationException("Vui lòng chọn thời gian dùng thuốc");
        }
        if (prescription.getItems() == null || prescription.getItems().isEmpty()) {
            throw new ApplicationException("Toa thuốc phải có ít nhất một thuốc trước khi phát hành");
        }

        for (PrescriptionItem item : prescription.getItems()) {
            if (item.getMedicineName() == null || item.getMedicineName().isBlank()) {
                throw new ApplicationException("Tên thuốc không được để trống");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ApplicationException("Số lượng thuốc phải lớn hơn 0");
            }
            boolean hasDose = isNotBlank(item.getMorningDose())
                    || isNotBlank(item.getNoonDose())
                    || isNotBlank(item.getAfternoonDose())
                    || isNotBlank(item.getEveningDose());
            if (!hasDose) {
                throw new ApplicationException("Vui lòng chọn ít nhất một thời điểm dùng thuốc");
            }
        }

        prescription.setStatus(PrescriptionStatus.ISSUED);
        prescription.setIssuedAt(LocalDateTime.now());

        Prescription issued = prescriptionRepository.save(prescription);
        eventPublisher.publishEvent(
                new PrescriptionIssuedEvent(this, issued, accountSupport.getCurrentAccountId()));
        return toRes(issued);
    }

    @Override
    public PrescriptionPrintRes getPrintData(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Không tìm thấy toa thuốc"));

        if (prescription.getStatus() != PrescriptionStatus.ISSUED) {
            throw new ApplicationException("Toa thuốc chưa được phát hành");
        }

        eventPublisher.publishEvent(
                new PrescriptionPrintedEvent(this, prescription, accountSupport.getCurrentAccountId()));

        String doctorName = accountRepository.findById(prescription.getDoctorId())
                .map(a -> a.getName())
                .orElse("");

        return PrescriptionPrintRes.builder()
                .prescriptionNumber(prescription.getPrescriptionNumber())
                .hospitalName(prescription.getHospitalName())
                .receiverName(prescription.getReceiverName())
                .insuranceCode(prescription.getInsuranceCode())
                .receiverAddress(prescription.getReceiverAddress())
                .diagnosis(prescription.getDiagnosis())
                .doctorName(doctorName)
                .durationDays(prescription.getDurationDays())
                .advice(prescription.getAdvice())
                .issuedAt(prescription.getIssuedAt())
                .items(toItemResList(prescription.getItems()))
                .build();
    }

    private MedicalRecord findApprovedRecord(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new ApplicationException("Không tìm thấy bệnh án"));
        if (record.getStatus() != MedicalRecordStatus.APPROVED) {
            throw new ApplicationException("Bệnh án chưa được duyệt nên không thể tạo toa thuốc");
        }
        return record;
    }

    private Prescription findDraftPrescription(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ApplicationException("Không tìm thấy toa thuốc"));
        if (prescription.getStatus() != PrescriptionStatus.DRAFT) {
            throw new ApplicationException("Toa thuốc đã được phát hành, không thể chỉnh sửa");
        }
        return prescription;
    }

    private List<PrescriptionItem> buildItems(List<PrescriptionItemReq> reqs, Prescription prescription) {
        List<PrescriptionItem> items = new ArrayList<>();
        for (int i = 0; i < reqs.size(); i++) {
            PrescriptionItemReq req = reqs.get(i);
            items.add(PrescriptionItem.builder()
                    .prescriptionId(prescription.getId())
                    .medicineId(req.getMedicineId())
                    .medicineName(req.getMedicineName())
                    .strength(req.getStrength())
                    .unit(req.getUnit())
                    .quantity(req.getQuantity())
                    .morningDose(req.getMorningDose())
                    .noonDose(req.getNoonDose())
                    .afternoonDose(req.getAfternoonDose())
                    .eveningDose(req.getEveningDose())
                    .instruction(req.getInstruction())
                    .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : i)
                    .build());
        }
        return items;
    }

    private String generatePrescriptionNumber() {
        int year = Year.now().getValue();
        long count = prescriptionRepository.count() + 1;
        return String.format("PRE-%d-%06d", year, count);
    }

    private PrescriptionRes toRes(Prescription p) {
        String doctorName = accountRepository.findById(p.getDoctorId())
                .map(a -> a.getName())
                .orElse("");
        return PrescriptionRes.builder()
                .id(p.getId())
                .prescriptionNumber(p.getPrescriptionNumber())
                .medicalRecordId(p.getMedicalRecordId())
                .patientId(p.getPatientId())
                .doctorId(p.getDoctorId())
                .doctorName(doctorName)
                .status(p.getStatus())
                .hospitalName(p.getHospitalName())
                .receiverName(p.getReceiverName())
                .insuranceCode(p.getInsuranceCode())
                .receiverAddress(p.getReceiverAddress())
                .diagnosis(p.getDiagnosis())
                .durationOption(p.getDurationOption())
                .durationDays(p.getDurationDays())
                .advice(p.getAdvice())
                .issuedAt(p.getIssuedAt())
                .createdAt(p.getCreatedAt())
                .items(toItemResList(p.getItems()))
                .build();
    }

    private List<PrescriptionItemRes> toItemResList(List<PrescriptionItem> items) {
        if (items == null) return new ArrayList<>();
        return items.stream().map(item -> PrescriptionItemRes.builder()
                .id(item.getId())
                .medicineId(item.getMedicineId())
                .medicineName(item.getMedicineName())
                .strength(item.getStrength())
                .unit(item.getUnit())
                .quantity(item.getQuantity())
                .morningDose(item.getMorningDose())
                .noonDose(item.getNoonDose())
                .afternoonDose(item.getAfternoonDose())
                .eveningDose(item.getEveningDose())
                .instruction(item.getInstruction())
                .sortOrder(item.getSortOrder())
                .build()).toList();
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
