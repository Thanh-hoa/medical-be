package com.example.medical_be.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.prescription.UpdatePrescriptionReq;
import com.example.medical_be.dto.res.PrescriptionPrintRes;
import com.example.medical_be.dto.res.PrescriptionRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.IPrescriptionService;
import com.example.medical_be.swagger.GroupAPIConstant;
import com.example.medical_be.swagger.PrescriptionApiExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.PRESCRIPTION, description = "Quản lý toa thuốc sau khi bệnh án được duyệt")
public class PrescriptionController {

    private final IPrescriptionService prescriptionService;
    private final IMessageTranslator messageTranslator;

    @Operation(summary = "Tạo toa thuốc từ bệnh án",
               description = "Tạo toa thuốc DRAFT cho bệnh án đã APPROVED. Nếu toa đã tồn tại, trả về toa hiện có")
    @ApiResponse(responseCode = "200", description = "Tạo hoặc trả về toa thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PrescriptionApiExamples.CREATE_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records-approval:create')")
    @PostMapping(APIRoutes.PRESCRIPTION_CREATE_BY_RECORD)
    public ResponseEntity<JSONResponse<?>> create(@PathVariable Long recordId) {
        return ResponseEntity.ok(JSONResponse.<PrescriptionRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("prescription.create_success"))
                .data(prescriptionService.createByMedicalRecord(recordId))
                .build());
    }

    @Operation(summary = "Lấy toa thuốc theo bệnh án",
               description = "Trả về toa thuốc của bệnh án. Nếu chưa có toa, data = null (không dùng HTTP 404)")
    @ApiResponse(responseCode = "200", description = "Lấy toa thành công (data có thể là null nếu chưa tạo toa)",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = {
                                        @ExampleObject(name = "Đã có toa", value = PrescriptionApiExamples.GET_BY_RECORD_SUCCESS),
                                        @ExampleObject(name = "Chưa có toa", value = PrescriptionApiExamples.GET_BY_RECORD_NULL)
                                    }))
    @PreAuthorize("hasAuthority('medical-records:view')")
    @GetMapping(APIRoutes.PRESCRIPTION_GET_BY_RECORD)
    public ResponseEntity<JSONResponse<?>> getByRecord(@PathVariable Long recordId) {
        return ResponseEntity.ok(JSONResponse.<PrescriptionRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("prescription.get_success"))
                .data(prescriptionService.getByMedicalRecord(recordId))
                .build());
    }

    @Operation(summary = "Cập nhật toa thuốc nháp",
               description = "Cập nhật thông tin toa thuốc đang ở trạng thái DRAFT. Gửi toàn bộ danh sách thuốc, hệ thống sẽ thay thế")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PrescriptionApiExamples.UPDATE_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records-approval:create')")
    @PutMapping(APIRoutes.PRESCRIPTION_UPDATE)
    public ResponseEntity<JSONResponse<?>> update(
            @PathVariable Long id,
            @RequestBody UpdatePrescriptionReq req) {
        return ResponseEntity.ok(JSONResponse.<PrescriptionRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("prescription.update_success"))
                .data(prescriptionService.update(id, req))
                .build());
    }

    @Operation(summary = "Phát hành toa thuốc",
               description = "Chuyển toa từ DRAFT sang ISSUED. Toa đã phát hành không thể sửa. Yêu cầu: tên bệnh viện, tên người nhận, chẩn đoán, thời gian dùng thuốc, ít nhất 1 thuốc")
    @ApiResponse(responseCode = "200", description = "Phát hành thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PrescriptionApiExamples.ISSUE_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records-approval:create')")
    @PutMapping(APIRoutes.PRESCRIPTION_ISSUE)
    public ResponseEntity<JSONResponse<?>> issue(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<PrescriptionRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("prescription.issue_success"))
                .data(prescriptionService.issue(id))
                .build());
    }

    @Operation(summary = "Lấy dữ liệu in toa",
               description = "Lấy đầy đủ thông tin để in toa thuốc. Chỉ hoạt động với toa đã ISSUED")
    @ApiResponse(responseCode = "200", description = "Lấy dữ liệu in thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PrescriptionApiExamples.PRINT_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:view')")
    @GetMapping(APIRoutes.PRESCRIPTION_PRINT)
    public ResponseEntity<JSONResponse<?>> print(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<PrescriptionPrintRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("prescription.print_success"))
                .data(prescriptionService.getPrintData(id))
                .build());
    }
}
