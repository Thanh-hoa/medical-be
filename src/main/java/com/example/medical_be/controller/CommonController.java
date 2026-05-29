package com.example.medical_be.controller;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.res.InfoAccountRes;
import com.example.medical_be.dto.res.MediaUploadRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.CommonService;
import com.example.medical_be.swagger.GroupAPIConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.COMMON, description = "Các API chung liên quan đến upload media như hình ảnh, video, tài liệu,...")    
public class CommonController {

    private final CommonService mediaUploadCommonService;
    private final IMessageTranslator messageTranslator;

    @Operation(summary = "Upload media file", description = "Upload hình ảnh hoặc tài liệu, trả về URL để dùng cho các field `photo` / `photoUrl`")
    @ApiResponse(responseCode = "200", description = "Upload thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JSONResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "isError": false,
                              "message": "Tải file thành công",
                              "data": {
                                "path": "http://localhost:8080/uploads/avatar_20260101.png",
                                "fileName": "avatar_20260101.png",
                                "extensionFile": "png"
                              }
                            }
                            """)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = APIRoutes.UPLOAD_MEDIA, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JSONResponse<?>> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(JSONResponse.<MediaUploadRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("file.upload_success"))
                .data(mediaUploadCommonService.uploadFile(file))
                .build());
    }

    @Operation(summary = "Get all roles", description = "Lấy danh sách role để dùng khi tạo / cập nhật tài khoản")
    @PreAuthorize("isAuthenticated()")
    @GetMapping(APIRoutes.GET_ROLES)
    public ResponseEntity<JSONResponse<?>> getRoles() {
        return ResponseEntity.ok(JSONResponse.<List<InfoAccountRes.Role>>builder()
                .isError(false)
                .message(messageTranslator.getMessage("role.get_list_success"))
                .data(mediaUploadCommonService.getRoles())
                .build());
    }
}