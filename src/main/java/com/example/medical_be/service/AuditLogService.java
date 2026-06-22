package com.example.medical_be.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.medical_be.dto.res.AuditLogRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.entity.AuditLog;
import com.example.medical_be.exception.ApplicationException;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.repository.AuditLogRepository;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.support.PaginationUtils;

import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogService {

    public static final String RESOURCE_MEDICAL_RECORD = "MEDICAL_RECORD";
    public static final String RESOURCE_PRESCRIPTION   = "PRESCRIPTION";

    public static final String ACTION_UPLOAD   = "UPLOAD";
    public static final String ACTION_UPDATE   = "UPDATE";
    public static final String ACTION_SUBMIT   = "SUBMIT";
    public static final String ACTION_APPROVE  = "APPROVE";
    public static final String ACTION_REJECT   = "REJECT";
    public static final String ACTION_RESUBMIT = "RESUBMIT";
    public static final String ACTION_DELETE   = "DELETE";

    public static final String ACTION_PRESCRIPTION_CREATE     = "PRESCRIPTION_CREATE";
    public static final String ACTION_PRESCRIPTION_SAVE_DRAFT = "PRESCRIPTION_SAVE_DRAFT";
    public static final String ACTION_PRESCRIPTION_ISSUE      = "PRESCRIPTION_ISSUE";
    public static final String ACTION_PRESCRIPTION_PRINT      = "PRESCRIPTION_PRINT";

    final AuditLogRepository auditLogRepository;
    final AccountSupport accountSupport;
    final IMessageTranslator messageTranslator;

    public void log(String action, String resourceType, Long resourceId,
                    String oldValue, String newValue) {
        Long actorId = null;
        try { actorId = accountSupport.getCurrentAccountId(); } catch (Exception ignored) {}
        log(action, resourceType, resourceId, oldValue, newValue, actorId);
    }

    public void log(String action, String resourceType, Long resourceId,
                    String oldValue, String newValue, Long actorId) {
        AuditLog entry = AuditLog.builder()
                .actorId(actorId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(getClientIp())
                .userAgent(getUserAgent())
                .build();
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogRes> list(String resourceType, Long actorId,
                                           String action, String period, String date,
                                           String fromDate, String toDate,
                                           Integer page, Integer limit) {
        int p = PaginationUtils.normalizePage(page, messageTranslator);
        int l = PaginationUtils.normalizeLimit(limit, messageTranslator);
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "createdAt"));
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        Page<AuditLog> pageRes = auditLogRepository.findAll(
                buildAuditLogSpecification(resourceType, actorId, action, null, null, range),
                pageable);
        return PaginationUtils.buildPagedResponse(
                pageRes.getContent().stream().map(this::toRes).toList(),
                pageRes, page, l);
    }

    @Transactional(readOnly = true)
    public AuditLogRes detail(Long id) {
        return auditLogRepository.findById(id)
                .map(this::toRes)
                .orElseThrow(() -> new ApplicationException(messageTranslator.getMessage("audit_log.not_found")));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogRes> listByRecord(Long recordId, String period, String date,
                                                   String fromDate, String toDate,
                                                   Integer page, Integer limit) {
        int p = PaginationUtils.normalizePage(page, messageTranslator);
        int l = PaginationUtils.normalizeLimit(limit, messageTranslator);
        Pageable pageable = PageRequest.of(p, l, Sort.by(Sort.Direction.DESC, "createdAt"));
        DateRange range = resolveDateRange(period, date, fromDate, toDate);
        Page<AuditLog> pageRes = auditLogRepository.findAll(
                buildAuditLogSpecification(null, null, null, RESOURCE_MEDICAL_RECORD, recordId, range),
                pageable);
        return PaginationUtils.buildPagedResponse(
                pageRes.getContent().stream().map(this::toRes).toList(),
                pageRes, page, l);
    }

    private AuditLogRes toRes(AuditLog log) {
        String actorName = log.getActor() != null ? log.getActor().getName() : null;
        return AuditLogRes.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .actorName(actorName)
                .action(log.getAction())
                .actionLabel(mapActionLabel(log.getAction()))
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String mapActionLabel(String action) {
        if (action == null) return null;
        return switch (action) {
            case ACTION_UPLOAD   -> "Upload bệnh án";
            case ACTION_SUBMIT   -> "Gửi để bác sĩ duyệt";
            case ACTION_APPROVE  -> "Phê duyệt bệnh án";
            case ACTION_REJECT   -> "Từ chối bệnh án";
            case ACTION_RESUBMIT -> "Nộp lại sau từ chối";
            case ACTION_UPDATE   -> "Cập nhật bệnh án";
            case ACTION_DELETE                   -> "Xóa bệnh án";
            case ACTION_PRESCRIPTION_CREATE     -> "Tạo toa thuốc";
            case ACTION_PRESCRIPTION_SAVE_DRAFT -> "Lưu nháp toa thuốc";
            case ACTION_PRESCRIPTION_ISSUE      -> "Phát hành toa thuốc";
            case ACTION_PRESCRIPTION_PRINT      -> "In toa thuốc";
            default                             -> action;
        };
    }

    private Specification<AuditLog> buildAuditLogSpecification(String resourceType, Long actorId, String action,
                                                               String exactResourceType, Long resourceId,
                                                               DateRange range) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (resourceType != null && !resourceType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("resourceType"), resourceType));
            }
            if (actorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorId"), actorId));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }
            if (exactResourceType != null && !exactResourceType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("resourceType"), exactResourceType));
            }
            if (resourceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("resourceId"), resourceId));
            }
            if (range.fromDateTime() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), range.fromDateTime()));
            }
            if (range.toDateTime() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), range.toDateTime()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private DateRange resolveDateRange(String period, String date, String fromDate, String toDate) {
        if (period != null && !period.isBlank()) {
            LocalDate baseDate = parseDateOrToday(date);
            return switch (period.toLowerCase()) {
                case "day", "date", "today" -> new DateRange(baseDate.atStartOfDay(), baseDate.plusDays(1).atStartOfDay());
                case "week" -> {
                    LocalDate start = baseDate.with(DayOfWeek.MONDAY);
                    yield new DateRange(start.atStartOfDay(), start.plusWeeks(1).atStartOfDay());
                }
                case "month" -> {
                    LocalDate start = baseDate.withDayOfMonth(1);
                    yield new DateRange(start.atStartOfDay(), start.plusMonths(1).atStartOfDay());
                }
                case "year" -> {
                    LocalDate start = baseDate.withDayOfYear(1);
                    yield new DateRange(start.atStartOfDay(), start.plusYears(1).atStartOfDay());
                }
                default -> throw new ApplicationException(messageTranslator.getMessage("audit_log.period.invalid"));
            };
        }

        LocalDateTime fromDateTime = parseDateOrNull(fromDate, "audit_log.from_date.invalid")
                .map(LocalDate::atStartOfDay)
                .orElse(null);
        LocalDateTime toDateTime = parseDateOrNull(toDate, "audit_log.to_date.invalid")
                .map(d -> d.plusDays(1).atStartOfDay())
                .orElse(null);

        if (fromDateTime != null && toDateTime != null && !fromDateTime.isBefore(toDateTime)) {
            throw new ApplicationException(messageTranslator.getMessage("audit_log.date_range.invalid"));
        }
        return new DateRange(fromDateTime, toDateTime);
    }

    private LocalDate parseDateOrToday(String value) {
        return parseDateOrNull(value, "audit_log.date.invalid").orElse(LocalDate.now());
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

    private String getClientIp() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            var request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return forwarded != null ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    private String getUserAgent() {
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return null;
        }
    }

    private record DateRange(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    }
}
