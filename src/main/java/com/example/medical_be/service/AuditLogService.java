package com.example.medical_be.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLogService {

    public static final String RESOURCE_MEDICAL_RECORD = "MEDICAL_RECORD";

    public static final String ACTION_UPLOAD   = "UPLOAD";
    public static final String ACTION_UPDATE   = "UPDATE";
    public static final String ACTION_SUBMIT   = "SUBMIT";
    public static final String ACTION_APPROVE  = "APPROVE";
    public static final String ACTION_REJECT   = "REJECT";
    public static final String ACTION_RESUBMIT = "RESUBMIT";
    public static final String ACTION_DELETE   = "DELETE";

    final AuditLogRepository auditLogRepository;
    final AccountSupport accountSupport;
    final IMessageTranslator messageTranslator;

    public void log(String action, String resourceType, Long resourceId,
                    String oldValue, String newValue) {
        Long actorId = null;
        try { actorId = accountSupport.getCurrentAccountId(); } catch (Exception ignored) {}

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
                                           String action, Integer page, Integer limit) {
        int p = PaginationUtils.normalizePage(page, messageTranslator);
        int l = PaginationUtils.normalizeLimit(limit, messageTranslator);
        Pageable pageable = PageRequest.of(p, l);
        Page<AuditLog> pageRes = auditLogRepository.findAll(resourceType, actorId, action, pageable);
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
    public PagedResponse<AuditLogRes> listByRecord(Long recordId, Integer page, Integer limit) {
        int p = PaginationUtils.normalizePage(page, messageTranslator);
        int l = PaginationUtils.normalizeLimit(limit, messageTranslator);
        Pageable pageable = PageRequest.of(p, l);
        Page<AuditLog> pageRes = auditLogRepository.findByResource(RESOURCE_MEDICAL_RECORD, recordId, pageable);
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
            case ACTION_DELETE   -> "Xóa bệnh án";
            default              -> action;
        };
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
}
