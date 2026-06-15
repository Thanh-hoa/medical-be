package com.example.medical_be.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.res.DashboardOverviewRes;
import com.example.medical_be.dto.res.DashboardStatItemRes;
import com.example.medical_be.dto.res.DashboardUserPerformanceRes;
import com.example.medical_be.entity.MedicalRecordStatus;
import com.example.medical_be.repository.AccountRepository;
import com.example.medical_be.repository.MedicalRecordRepository;
import com.example.medical_be.repository.PatientRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level =  AccessLevel.PRIVATE)
public class DashboardService {

    final MedicalRecordRepository medicalRecordRepository;
    final PatientRepository patientRepository;
    final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public DashboardOverviewRes overview() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return DashboardOverviewRes.builder()
                .totalRecords(medicalRecordRepository.countActive())
                .totalPatients(patientRepository.count())
                .totalAccounts(accountRepository.count())
                .processingRecords(medicalRecordRepository.countByStatus(MedicalRecordStatus.PROCESSING))
                .extractedRecords(medicalRecordRepository.countByStatus(MedicalRecordStatus.EXTRACTED))
                .pendingReviewRecords(medicalRecordRepository.countByStatus(MedicalRecordStatus.PENDING_DOCTOR_REVIEW))
                .approvedRecords(medicalRecordRepository.countByStatus(MedicalRecordStatus.APPROVED))
                .rejectedRecords(medicalRecordRepository.countByStatus(MedicalRecordStatus.REJECTED))
                .todayUploads(medicalRecordRepository.countCreatedSince(startOfToday))
                .todayApprovals(medicalRecordRepository.countApprovedSince(startOfToday))
                .build();
    }

    @Transactional(readOnly = true)
    public List<DashboardStatItemRes> recordsByStatus() {
        List<Object[]> rows = medicalRecordRepository.countGroupByStatus();
        List<DashboardStatItemRes> result = new ArrayList<>();
        for (Object[] row : rows) {
            MedicalRecordStatus status = (MedicalRecordStatus) row[0];
            long count = ((Number) row[1]).longValue();
            result.add(DashboardStatItemRes.builder()
                    .label(status.getDbValue())
                    .count(count)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DashboardStatItemRes> recordsByDepartment() {
        List<Object[]> rows = medicalRecordRepository.countGroupByDepartment();
        List<DashboardStatItemRes> result = new ArrayList<>();
        for (Object[] row : rows) {
            String dept = (String) row[0];
            long count = ((Number) row[1]).longValue();
            result.add(DashboardStatItemRes.builder()
                    .label(dept)
                    .count(count)
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DashboardUserPerformanceRes> userPerformance() {
        Map<Long, DashboardUserPerformanceRes> map = new HashMap<>();

        for (Object[] row : medicalRecordRepository.countGroupByUploadedBy()) {
            Long accountId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            map.computeIfAbsent(accountId, id -> DashboardUserPerformanceRes.builder()
                    .accountId(id).accountName(resolveAccountName(id)).build())
                    .setUploaded(count);
        }
        for (Object[] row : medicalRecordRepository.countGroupByApprovedBy()) {
            Long accountId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            map.computeIfAbsent(accountId, id -> DashboardUserPerformanceRes.builder()
                    .accountId(id).accountName(resolveAccountName(id)).build())
                    .setApproved(count);
        }
        for (Object[] row : medicalRecordRepository.countGroupByRejectedBy()) {
            Long accountId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            map.computeIfAbsent(accountId, id -> DashboardUserPerformanceRes.builder()
                    .accountId(id).accountName(resolveAccountName(id)).build())
                    .setRejected(count);
        }

        return new ArrayList<>(map.values());
    }

    private String resolveAccountName(Long accountId) {
        return accountRepository.findById(accountId)
                .map(a -> a.getName())
                .orElse("Unknown");
    }
}
