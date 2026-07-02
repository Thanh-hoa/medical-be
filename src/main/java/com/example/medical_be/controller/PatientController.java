package com.example.medical_be.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.patient.CreatePatientReq;
import com.example.medical_be.dto.req.patient.PatientSearchReq;
import com.example.medical_be.dto.req.patient.UpdatePatientReq;
import com.example.medical_be.dto.res.MedicalRecordSummaryPatient;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.dto.res.PatientRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.IPatientService;
import com.example.medical_be.support.AccountSupport;
import com.example.medical_be.swagger.GroupAPIConstant;
import com.example.medical_be.swagger.PatientApiExamples;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.PATIENT, description = "Tra cứu và quản lý thông tin bệnh nhân")
public class PatientController {

    private final IPatientService patientService;
    private final IMessageTranslator messageTranslator;
    private final AccountSupport accountSupport;

    @Operation(summary = "Tra cứu chi tiết thông tin bệnh nhân theo BHYT ",
               description = "Tìm bệnh nhân theo số thẻ BHYT, trả về thông tin bệnh nhân và danh sách bệnh án (mới nhất trước)")
    @ApiResponse(responseCode = "200", description = "Tìm thấy bệnh nhân",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PatientApiExamples.SEARCH_BY_BHYT_SUCCESS)))
    @PreAuthorize("hasAuthority('patient-search:view')")
    @GetMapping(APIRoutes.PATIENT_SEARCH)
    public ResponseEntity<JSONResponse<?>> searchByBhyt(
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String bhyt) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordSummaryPatient>builder()
                .isError(false)
                .message(messageTranslator.getMessage("patient.detail_success"))
                .data(patientService.findByIdentifier(firstNonBlank(identifier, bhyt)))
                .build());
    }

    @Operation(summary = "Tự tra cứu bệnh án của chính mình",
               description = "User tự nhập BHYT hoặc CCCD của mình để liên kết tài khoản với hồ sơ bệnh nhân lần đầu tra cứu; các lần sau gọi lại không cần truyền identifier")
    @ApiResponse(responseCode = "200", description = "Thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PatientApiExamples.SEARCH_BY_BHYT_SUCCESS)))
    @PreAuthorize("hasAuthority('patient-self:view')")
    @GetMapping(APIRoutes.PATIENT_ME)
    public ResponseEntity<JSONResponse<?>> me(@RequestParam(required = false) String identifier) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordSummaryPatient>builder()
                .isError(false)
                .message(messageTranslator.getMessage("patient.detail_success"))
                .data(patientService.findOrLinkSelf(accountSupport.getCurrentAccountId(), identifier))
                .build());
    }

    @Operation(summary = "Danh sách bệnh nhân",
               description = "Tìm kiếm danh sách bệnh nhân theo tên hoặc BHYT, có phân trang")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PatientApiExamples.LIST_PATIENT_SUCCESS)))
    @PreAuthorize("hasAuthority('patient-search:view')")
    @GetMapping(APIRoutes.PATIENT_LIST)
    public ResponseEntity<JSONResponse<?>> list(@ModelAttribute PatientSearchReq req) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<PatientRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("patient.list_success"))
                .data(patientService.search(req))
                .build());
    }

    @Operation(summary = "Tạo bệnh nhân mới",
               description = "Tạo hồ sơ bệnh nhân mới với số thẻ BHYT duy nhất")
    @ApiResponse(responseCode = "200", description = "Tạo thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PatientApiExamples.CREATE_PATIENT_SUCCESS)))
    @PreAuthorize("hasAuthority('patient-search:create')")
    @PostMapping(APIRoutes.PATIENT_CREATE)
    public ResponseEntity<JSONResponse<?>> create(@Valid @RequestBody CreatePatientReq req) {
        return ResponseEntity.ok(JSONResponse.<PatientRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("patient.create_success"))
                .data(patientService.findOrCreate(req))
                .build());
    }

    @Operation(summary = "Cập nhật thông tin bệnh nhân",
               description = "Cập nhật thông tin cá nhân của bệnh nhân")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = PatientApiExamples.UPDATE_PATIENT_SUCCESS)))
    @PreAuthorize("hasAuthority('patient-search:edit')")
    @PutMapping(APIRoutes.PATIENT_UPDATE)
    public ResponseEntity<JSONResponse<?>> update(
            @Valid @RequestBody UpdatePatientReq req) {
        return ResponseEntity.ok(JSONResponse.<PatientRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("patient.update_success"))
                .data(patientService.update(req))
                .build());
    }

    private String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
