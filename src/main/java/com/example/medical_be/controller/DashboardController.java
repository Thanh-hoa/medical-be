package com.example.medical_be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.res.DashboardOverviewRes;
import com.example.medical_be.dto.res.DashboardStatItemRes;
import com.example.medical_be.dto.res.DashboardTimelineRes;
import com.example.medical_be.dto.res.DashboardUserPerformanceRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.DashboardService;
import com.example.medical_be.swagger.GroupAPIConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.DASHBOARD, description = "Thống kê tổng quan hệ thống")
public class DashboardController {

    private final DashboardService dashboardService;
    private final IMessageTranslator messageTranslator;

    @Operation(summary = "Tổng quan dashboard",
               description = "Thống kê tổng số bệnh án, bệnh nhân, tài khoản, số bệnh án theo trạng thái và số upload/duyệt hôm nay")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping(APIRoutes.DASHBOARD_OVERVIEW)
    public ResponseEntity<JSONResponse<?>> overview(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(JSONResponse.<DashboardOverviewRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("dashboard.overview_success"))
                .data(dashboardService.overview(period, date, fromDate, toDate))
                .build());
    }

    @Operation(summary = "Bệnh án theo trạng thái",
               description = "Thống kê số lượng bệnh án cho từng trạng thái (Processing, Extracted, Pending, Approved, Rejected)")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping(APIRoutes.DASHBOARD_RECORDS_BY_STATUS)
    public ResponseEntity<JSONResponse<?>> recordsByStatus(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(JSONResponse.<List<DashboardStatItemRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("dashboard.records_by_status_success"))
                .data(dashboardService.recordsByStatus(scope, period, date, fromDate, toDate))
                .build());
    }

    @Operation(summary = "Bệnh án theo khoa/phòng",
               description = "Thống kê số lượng bệnh án theo từng khoa/phòng, sắp xếp giảm dần")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping(APIRoutes.DASHBOARD_RECORDS_BY_DEPT)
    public ResponseEntity<JSONResponse<?>> recordsByDepartment(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(JSONResponse.<List<DashboardStatItemRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("dashboard.records_by_department_success"))
                .data(dashboardService.recordsByDepartment(period, date, fromDate, toDate))
                .build());
    }

    @Operation(summary = "Hiệu suất người dùng",
               description = "Thống kê số bệnh án đã upload, đã duyệt và đã từ chối theo từng tài khoản")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping(APIRoutes.DASHBOARD_USER_PERFORMANCE)
    public ResponseEntity<JSONResponse<?>> userPerformance(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(JSONResponse.<List<DashboardUserPerformanceRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("dashboard.user_performance_success"))
                .data(dashboardService.userPerformance(period, date, fromDate, toDate))
                .build());
    }

    @Operation(summary = "Dữ liệu biểu đồ dashboard",
               description = "Trả dữ liệu timeline để FE vẽ chart theo ngày/tháng cho uploads, approvals, rejections")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @GetMapping(APIRoutes.DASHBOARD_TIMELINE)
    public ResponseEntity<JSONResponse<?>> timeline(
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        return ResponseEntity.ok(JSONResponse.<DashboardTimelineRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("dashboard.timeline_success"))
                .data(dashboardService.timeline(metric, period, date, fromDate, toDate))
                .build());
    }
}
