package com.example.medical_be.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.req.medicalRecord.MedicalRecordListReq;
import com.example.medical_be.dto.req.medicalRecord.OcrResultReq;
import com.example.medical_be.dto.req.medicalRecord.RejectMedicalRecordReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateExtractedFieldReq;
import com.example.medical_be.dto.req.medicalRecord.UpdateMedicalRecordDetailReq;
import com.example.medical_be.dto.res.MedicalRecordDetailRes;
import com.example.medical_be.dto.res.MedicalRecordSummaryRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.IMedicalRecordService;
import com.example.medical_be.swagger.GroupAPIConstant;
import com.example.medical_be.swagger.MedicalRecordApiExamples;

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
@Tag(name = GroupAPIConstant.MEDICAL_RECORD, description = "Quản lý bệnh án: tạo, xem, cập nhật, duyệt/từ chối")
public class MedicalRecordController {

    private final IMedicalRecordService medicalRecordService;
    private final IMessageTranslator messageTranslator;


    @Operation(summary = "Upload ảnh/PDF và xử lý OCR",
               description = "Upload file lên server, gọi OCR API xử lý, tự động tạo/tìm bệnh nhân, trả về bệnh án ở trạng thái Extracted")
    @ApiResponse(responseCode = "200", description = "Upload và xử lý OCR thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.UPLOAD_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:create')")
    @PostMapping(value = APIRoutes.MEDICAL_RECORD_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JSONResponse<?>> upload(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordDetailRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.upload_success"))
                .data(medicalRecordService.upload(file))
                .build());
    }

    @Operation(summary = "Nhận kết quả OCR từ AI",
               description = "Python AI service gọi về sau khi xử lý ảnh: lưu extractedData + labData, tự động tạo/tìm bệnh nhân theo BHYT, chuyển status → Extracted")
    @ApiResponse(responseCode = "200", description = "Lưu kết quả OCR thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.OCR_RESULT_SUCCESS)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping(APIRoutes.MEDICAL_RECORD_OCR_RESULT)
    public ResponseEntity<JSONResponse<?>> ocrResult(@Valid @RequestBody OcrResultReq req) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordSummaryRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.ocr_result_success"))
                .data(medicalRecordService.processOcrResult(req))
                .build());
    }

    @Operation(summary = "Danh sách bệnh án",
               description = "Employee chỉ thấy bệnh án của mình. Admin/Doctor thấy tất cả. Có thể filter theo status, patientId")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.LIST_RECORD_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:view')")
    @GetMapping(APIRoutes.MEDICAL_RECORD_LIST)
    public ResponseEntity<JSONResponse<?>> list(@ModelAttribute MedicalRecordListReq req) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<MedicalRecordSummaryRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.list_success"))
                .data(medicalRecordService.list(req))
                .build());
    }

    @Operation(summary = "Chi tiết bệnh án",
               description = "Xem chi tiết bệnh án: thông tin bệnh nhân, extracted fields, lab results")
    @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.DETAIL_RECORD_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:view')")
    @GetMapping(APIRoutes.MEDICAL_RECORD_DETAIL)
    public ResponseEntity<JSONResponse<?>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordDetailRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.detail_success"))
                .data(medicalRecordService.detail(id))
                .build());
    }

   
    
    @Operation(summary = "Cập nhật chi tiết bệnh án",
               description = "Nhân viên cập nhật lại toàn bộ thông tin đang hiển thị ở màn hình chi tiết sau OCR")
    @ApiResponse(responseCode = "200", description = "Cập nhật chi tiết thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.UPDATE_DETAIL_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:edit')")
    @PutMapping(APIRoutes.MEDICAL_RECORD_UPDATE_DETAIL)
    public ResponseEntity<JSONResponse<?>> updateDetail(@Valid @RequestBody UpdateMedicalRecordDetailReq req) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordDetailRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.update_success"))
                .data(medicalRecordService.updateDetail(req))
                .build());
    }

    @Operation(summary = "Cập nhật extracted field",
               description = "Nhân viên chỉnh sửa giá trị một trường OCR sau khi kiểm tra")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.UPDATE_FIELD_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:edit')")
    @PutMapping(APIRoutes.MEDICAL_RECORD_FIELD_UPDATE)
    public ResponseEntity<JSONResponse<?>> updateField(@Valid @RequestBody UpdateExtractedFieldReq req) {
        medicalRecordService.updateExtractedField(req);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                .isError(false)
                .message(messageTranslator.getMessage("extracted_field.update_success"))
                .build());
    }

    @Operation(summary = "Gửi bệnh án để bác sĩ duyệt",
               description = "Nhân viên submit bệnh án: Extracted → Pending Doctor Review. Tự động đánh dấu tất cả fields là verified")
    @ApiResponse(responseCode = "200", description = "Submit thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class)))
                                   // examples = @ExampleObject(value = MedicalRecordApiExamples.SUBMIT_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:edit')")
    @PutMapping(APIRoutes.MEDICAL_RECORD_SUBMIT)
    public ResponseEntity<JSONResponse<?>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordSummaryRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.submit_success"))
                .data(medicalRecordService.submitForReview(id))
                .build());
    }

    @Operation(summary = "Duyệt bệnh án",
               description = "Bác sĩ/Admin duyệt bệnh án: Pending Doctor Review → Approved")
    @ApiResponse(responseCode = "200", description = "Duyệt thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class)))
                                   // examples = @ExampleObject(value = MedicalRecordApiExamples.APPROVE_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records-approval:create')")
    @PutMapping(APIRoutes.MEDICAL_RECORD_APPROVE)
    public ResponseEntity<JSONResponse<?>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordSummaryRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.approve_success"))
                .data(medicalRecordService.approve(id))
                .build());
    }

    @Operation(summary = "Từ chối bệnh án",
               description = "Bác sĩ/Admin từ chối bệnh án: Pending Doctor Review → Rejected. Bắt buộc điền lý do")
    @ApiResponse(responseCode = "200", description = "Từ chối thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class)))
                               //     examples = @ExampleObject(value = MedicalRecordApiExamples.REJECT_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records-approval:cancel')")
    @PutMapping(APIRoutes.MEDICAL_RECORD_REJECT)
    public ResponseEntity<JSONResponse<?>> reject(@Valid @RequestBody RejectMedicalRecordReq req) {
        return ResponseEntity.ok(JSONResponse.<MedicalRecordSummaryRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.reject_success"))
                .data(medicalRecordService.reject(req))
                .build());
    }

    @Operation(summary = "Xóa bệnh án",
               description = "Admin xóa vĩnh viễn bệnh án và toàn bộ dữ liệu liên quan (OCR regions, extracted fields, lab results)")
    @ApiResponse(responseCode = "200", description = "Xóa thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicalRecordApiExamples.DELETE_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:delete')")
    @DeleteMapping(APIRoutes.MEDICAL_RECORD_DELETE)
    public ResponseEntity<JSONResponse<?>> delete(@PathVariable Long id) {
        medicalRecordService.delete(id);
        return ResponseEntity.ok(JSONResponse.<Void>builder()
                .isError(false)
                .message(messageTranslator.getMessage("record.delete_success"))
                .build());
    }
}
