package com.example.medical_be.controller;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.res.MediaUploadRes;
import com.example.medical_be.i18n.IMessageTranslator;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.MediaUploadCommonService;
import com.example.medical_be.swagger.GroupAPIConstant;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.COMMON, description = "Các API chung liên quan đến upload media như hình ảnh, video, tài liệu,...")    
public class MediaUploadCommonController {

    private final MediaUploadCommonService mediaUploadCommonService;
    private final IMessageTranslator messageTranslator;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = APIRoutes.UPLOAD_MEDIA, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JSONResponse<?>> uploadMedia(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(JSONResponse.<MediaUploadRes>builder()
                .isError(false)
                .message(messageTranslator.getMessage("file.upload_success"))
                .data(mediaUploadCommonService.uploadFile(file))
                .build());
    }
}