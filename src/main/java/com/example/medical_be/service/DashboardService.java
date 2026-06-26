package com.example.medical_be.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.medical_be.dto.res.DashboardMetricCardRes;
import com.example.medical_be.dto.res.DashboardOverviewRes;
import com.example.medical_be.dto.res.DashboardRangeRes;
import com.example.medical_be.dto.res.DashboardStatItemRes;
import com.example.medical_be.dto.res.DashboardTimelineRes;
import com.example.medical_be.dto.res.DashboardUserPerformanceRes;
import com.example.medical_be.entity.MedicalRecord;
import com.example.medical_be.entity.enums.MedicalRecordStatus;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
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

    static final String DEFAULT_PERIOD = "day";
    static final String SCOPE_CURRENT = "current";
    static final String SCOPE_CREATED = "created";

    final MedicalRecordRepository medicalRecordRepository;
    final PatientRepository patientRepository;
    final AccountRepository accountRepository;
    final IMessageTranslator messageTranslator;

    @Transactional(readOnly = true)
    public DashboardOverviewRes overview(String period, String date, String fromDate, String toDate) {
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        DateRange previousRange = range.previous();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long currentUploads = medicalRecordRepository.countCreatedBetween(range.fromDateTime(), range.toDateTime());
        long previousUploads = medicalRecordRepository.countCreatedBetween(previousRange.fromDateTime(), previousRange.toDateTime());
        long currentApprovals = medicalRecordRepository.countApprovedBetween(range.fromDateTime(), range.toDateTime());
        long previousApprovals = medicalRecordRepository.countApprovedBetween(previousRange.fromDateTime(), previousRange.toDateTime());
        long currentRejections = medicalRecordRepository.countRejectedBetween(range.fromDateTime(), range.toDateTime());
        long previousRejections = medicalRecordRepository.countRejectedBetween(previousRange.fromDateTime(), previousRange.toDateTime());
        long currentPatients = patientRepository.countCreatedBetween(range.fromDateTime(), range.toDateTime());
        long previousPatients = patientRepository.countCreatedBetween(previousRange.fromDateTime(), previousRange.toDateTime());

        return DashboardOverviewRes.builder()
                .range(toRangeRes(range, previousRange))
                .cards(List.of(
                        buildCard("newRecords", "Bệnh án mới", currentUploads, previousUploads),
                        buildCard("approvedRecords", "Bệnh án đã duyệt", currentApprovals, previousApprovals),
                        buildCard("rejectedRecords", "Bệnh án bị từ chối", currentRejections, previousRejections),
                        buildCard("newPatients", "Bệnh nhân mới", currentPatients, previousPatients),
                        buildCard("pendingRecords", "Đang chờ duyệt",
                                medicalRecordRepository.countByStatus(MedicalRecordStatus.PENDING_DOCTOR_REVIEW), 0)
                ))
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
    public List<DashboardStatItemRes> recordsByStatus(String scope, String period, String date,
                                                      String fromDate, String toDate) {
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        String normalizedScope = normalizeScope(scope);
        List<Object[]> rows = SCOPE_CREATED.equals(normalizedScope)
                ? medicalRecordRepository.countGroupByStatusCreatedBetween(range.fromDateTime(), range.toDateTime())
                : medicalRecordRepository.countGroupByStatus();
        long total = rows.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum();
        List<DashboardStatItemRes> result = new ArrayList<>();
        for (Object[] row : rows) {
            MedicalRecordStatus status = (MedicalRecordStatus) row[0];
            long count = ((Number) row[1]).longValue();
            result.add(DashboardStatItemRes.builder()
                    .key(status != null ? status.name() : null)
                    .label(status != null ? status.getDbValue() : null)
                    .count(count)
                    .percent(calculatePercent(count, total))
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DashboardStatItemRes> recordsByDepartment(String period, String date, String fromDate, String toDate) {
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        List<Object[]> rows = medicalRecordRepository.countGroupByDepartmentCreatedBetween(
                range.fromDateTime(), range.toDateTime());
        long total = rows.stream().mapToLong(row -> ((Number) row[1]).longValue()).sum();
        List<DashboardStatItemRes> result = new ArrayList<>();
        for (Object[] row : rows) {
            String dept = (String) row[0];
            long count = ((Number) row[1]).longValue();
            result.add(DashboardStatItemRes.builder()
                    .key(dept)
                    .label(dept)
                    .count(count)
                    .percent(calculatePercent(count, total))
                    .build());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<DashboardUserPerformanceRes> userPerformance(String period, String date, String fromDate, String toDate) {
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        Map<Long, DashboardUserPerformanceRes> map = new HashMap<>();

        for (Object[] row : medicalRecordRepository.countGroupByUploadedByCreatedBetween(range.fromDateTime(), range.toDateTime())) {
            Long accountId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            map.computeIfAbsent(accountId, id -> DashboardUserPerformanceRes.builder()
                    .accountId(id).accountName(resolveAccountName(id)).build())
                    .setUploaded(count);
        }
        for (Object[] row : medicalRecordRepository.countGroupByApprovedByBetween(range.fromDateTime(), range.toDateTime())) {
            Long accountId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            map.computeIfAbsent(accountId, id -> DashboardUserPerformanceRes.builder()
                    .accountId(id).accountName(resolveAccountName(id)).build())
                    .setApproved(count);
        }
        for (Object[] row : medicalRecordRepository.countGroupByRejectedByBetween(range.fromDateTime(), range.toDateTime())) {
            Long accountId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            map.computeIfAbsent(accountId, id -> DashboardUserPerformanceRes.builder()
                    .accountId(id).accountName(resolveAccountName(id)).build())
                    .setRejected(count);
        }

        map.values().forEach(item -> item.setTotalActions(item.getUploaded() + item.getApproved() + item.getRejected()));
        return map.values().stream()
                .sorted(Comparator.comparingLong(DashboardUserPerformanceRes::getTotalActions).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public DashboardTimelineRes timeline(String metric, String period, String date, String fromDate, String toDate) {
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        String normalizedMetric = normalizeMetric(metric);
        List<MedicalRecord> records = switch (normalizedMetric) {
            case "approvals" -> medicalRecordRepository.findApprovedBetween(range.fromDateTime(), range.toDateTime());
            case "rejections" -> medicalRecordRepository.findRejectedBetween(range.fromDateTime(), range.toDateTime());
            default -> medicalRecordRepository.findCreatedBetween(range.fromDateTime(), range.toDateTime());
        };
        Function<MedicalRecord, LocalDateTime> timeExtractor = switch (normalizedMetric) {
            case "approvals" -> MedicalRecord::getApprovedAt;
            case "rejections" -> MedicalRecord::getRejectedAt;
            default -> MedicalRecord::getCreatedAt;
        };

        return DashboardTimelineRes.builder()
                .metric(normalizedMetric)
                .period(range.period())
                .fromDate(range.fromDate())
                .toDate(range.toDateInclusive())
                .items(buildTimelineItems(range, records, timeExtractor))
                .build();
    }

    private DashboardMetricCardRes buildCard(String key, String label, long value, long previousValue) {
        long change = value - previousValue;
        return DashboardMetricCardRes.builder()
                .key(key)
                .label(label)
                .value(value)
                .previousValue(previousValue)
                .change(change)
                .changePercent(calculateChangePercent(value, previousValue))
                .trend(change > 0 ? "up" : change < 0 ? "down" : "flat")
                .build();
    }

    private List<DashboardStatItemRes> buildTimelineItems(DateRange range, List<MedicalRecord> records,
                                                          Function<MedicalRecord, LocalDateTime> timeExtractor) {
        boolean monthly = useMonthlyBuckets(range);
        Map<String, Long> buckets = new LinkedHashMap<>();
        LocalDate cursor = range.fromDate();
        while (cursor.isBefore(range.toDateExclusive())) {
            String label = monthly ? monthLabel(cursor) : cursor.toString();
            buckets.putIfAbsent(label, 0L);
            cursor = monthly ? cursor.plusMonths(1).withDayOfMonth(1) : cursor.plusDays(1);
        }

        for (MedicalRecord record : records) {
            LocalDateTime value = timeExtractor.apply(record);
            if (value == null) {
                continue;
            }
            String label = monthly ? monthLabel(value.toLocalDate()) : value.toLocalDate().toString();
            buckets.computeIfPresent(label, (key, count) -> count + 1);
        }

        return buckets.entrySet().stream()
                .map(entry -> DashboardStatItemRes.builder()
                        .key(entry.getKey())
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .toList();
    }

    private DateRange resolveDateRange(String period, String date, String fromDate, String toDate) {
        if ((fromDate != null && !fromDate.isBlank()) || (toDate != null && !toDate.isBlank())) {
            LocalDate from = parseDateOrNull(fromDate, "dashboard.from_date.invalid").orElse(LocalDate.now());
            LocalDate to = parseDateOrNull(toDate, "dashboard.to_date.invalid").orElse(from);
            if (from.isAfter(to)) {
                throw new ApplicationException(messageTranslator.getMessage("dashboard.date_range.invalid"));
            }
            return DateRange.custom(from, to.plusDays(1));
        }

        String normalizedPeriod = period == null || period.isBlank() ? DEFAULT_PERIOD : period.toLowerCase();
        LocalDate baseDate = parseDateOrNull(date, "dashboard.date.invalid").orElse(LocalDate.now());
        return switch (normalizedPeriod) {
            case "day", "today" -> DateRange.of("day", baseDate, baseDate.plusDays(1));
            case "week" -> {
                LocalDate start = baseDate.with(DayOfWeek.MONDAY);
                yield DateRange.of("week", start, start.plusWeeks(1));
            }
            case "month" -> {
                LocalDate start = baseDate.withDayOfMonth(1);
                yield DateRange.of("month", start, start.plusMonths(1));
            }
            case "year" -> {
                LocalDate start = baseDate.withDayOfYear(1);
                yield DateRange.of("year", start, start.plusYears(1));
            }
            default -> throw new ApplicationException(messageTranslator.getMessage("dashboard.period.invalid"));
        };
    }

    private Optional<LocalDate> parseDateOrNull(String value, String messageKey) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(value));
        } catch (DateTimeParseException e) {
            throw new ApplicationException(messageTranslator.getMessage(messageKey));
        }
    }

    private DashboardRangeRes toRangeRes(DateRange range, DateRange previousRange) {
        return DashboardRangeRes.builder()
                .period(range.period())
                .fromDate(range.fromDate())
                .toDate(range.toDateInclusive())
                .previousFromDate(previousRange.fromDate())
                .previousToDate(previousRange.toDateInclusive())
                .build();
    }

    private String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return SCOPE_CREATED;
        }
        String normalized = scope.toLowerCase();
        if (!SCOPE_CURRENT.equals(normalized) && !SCOPE_CREATED.equals(normalized)) {
            throw new ApplicationException(messageTranslator.getMessage("dashboard.scope.invalid"));
        }
        return normalized;
    }

    private String normalizeMetric(String metric) {
        if (metric == null || metric.isBlank()) {
            return "uploads";
        }
        String normalized = metric.toLowerCase();
        if (!List.of("uploads", "records", "approvals", "rejections").contains(normalized)) {
            throw new ApplicationException(messageTranslator.getMessage("dashboard.metric.invalid"));
        }
        return "records".equals(normalized) ? "uploads" : normalized;
    }

    private boolean useMonthlyBuckets(DateRange range) {
        long days = ChronoUnit.DAYS.between(range.fromDate(), range.toDateExclusive());
        return "year".equals(range.period()) || days > 62;
    }

    private String monthLabel(LocalDate date) {
        return date.withDayOfMonth(1).toString().substring(0, 7);
    }

    private double calculatePercent(long value, long total) {
        if (total == 0) {
            return 0;
        }
        return Math.round(value * 10000.0 / total) / 100.0;
    }

    private double calculateChangePercent(long value, long previousValue) {
        if (previousValue == 0) {
            return value == 0 ? 0 : 100;
        }
        return Math.round((value - previousValue) * 10000.0 / previousValue) / 100.0;
    }

    private String resolveAccountName(Long accountId) {
        return accountRepository.findById(accountId)
                .map(a -> a.getName())
                .orElse("Unknown");
    }

    private record DateRange(String period, LocalDate fromDate, LocalDate toDateExclusive) {
        static DateRange of(String period, LocalDate fromDate, LocalDate toDateExclusive) {
            return new DateRange(period, fromDate, toDateExclusive);
        }

        static DateRange custom(LocalDate fromDate, LocalDate toDateExclusive) {
            return new DateRange("custom", fromDate, toDateExclusive);
        }

        LocalDateTime fromDateTime() {
            return fromDate.atStartOfDay();
        }

        LocalDateTime toDateTime() {
            return toDateExclusive.atStartOfDay();
        }

        LocalDate toDateInclusive() {
            return toDateExclusive.minusDays(1);
        }

        DateRange previous() {
            long days = ChronoUnit.DAYS.between(fromDate, toDateExclusive);
            return new DateRange(period, fromDate.minusDays(days), fromDate);
        }
    }
}
