package com.example.medical_be.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.res.AuditLogRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.AuditLogService;
import com.example.medical_be.swagger.GroupAPIConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.AUDIT_LOG, description = "Nhật ký thao tác hệ thống - chỉ admin")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final IMessageTranslator messageTranslator;

    @Operation(summary = "Danh sách audit log", description = "Admin xem toàn bộ log thao tác. Có thể filter theo resourceType, actorId, action")
    @PreAuthorize("hasAuthority('audit-logs:view')")
    @GetMapping(APIRoutes.AUDIT_LOG_LIST)
    public ResponseEntity<JSONResponse<?>> list(
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<AuditLogRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("audit_log.list_success"))
                .data(auditLogService.list(resourceType, actorId, action, page, limit))
                .build());
    }

    @Operation(summary = "Chi tiết audit log", description = "Admin xem chi tiết một log entry")
    @PreAuthorize("hasAuthority('audit-logs:view')")
    @GetMapping(APIRoutes.AUDIT_LOG_DETAIL)
    public ResponseEntity<JSONResponse<?>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<AuditLogRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("audit_log.detail_success"))
                .data(auditLogService.detail(id))
                .build());
    }

    @Operation(summary = "Audit log của bệnh án", description = "Admin/Doctor xem lịch sử thao tác trên một bệnh án cụ thể")
    @PreAuthorize("hasAuthority('medical-records:view')")
    @GetMapping(APIRoutes.AUDIT_LOG_BY_RECORD)
    public ResponseEntity<JSONResponse<?>> listByRecord(
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<AuditLogRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("audit_log.list_success"))
                .data(auditLogService.listByRecord(id, page, limit))
                .build());
    }
}
