package com.example.medical_be.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.res.MedicineRes;
import com.example.medical_be.dto.res.PagedResponse;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.IMedicineService;
import com.example.medical_be.swagger.GroupAPIConstant;
import com.example.medical_be.swagger.MedicineApiExamples;

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
@Tag(name = GroupAPIConstant.MEDICINE, description = "Tra cứu danh mục thuốc")
public class MedicineController {

    private final IMedicineService medicineService;
    private final IMessageTranslator messageTranslator;

    @Operation(summary = "Tìm kiếm thuốc",
               description = "Tìm kiếm thuốc theo tên, mã hoặc hàm lượng. Chỉ trả thuốc đang hoạt động (isActive = true)")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                 content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = JSONResponse.class),
                                    examples = @ExampleObject(value = MedicineApiExamples.MEDICINE_LIST_SUCCESS)))
    @PreAuthorize("hasAuthority('medical-records:view')")
    @GetMapping(APIRoutes.MEDICINE_LIST)
    public ResponseEntity<JSONResponse<?>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(JSONResponse.<PagedResponse<MedicineRes>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("medicine.list_success"))
                .data(medicineService.search(q, page, limit))
                .build());
    }
}
