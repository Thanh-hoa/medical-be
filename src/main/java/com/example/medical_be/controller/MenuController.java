package com.example.medical_be.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.dto.res.MenuItemRes;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.configuration.IMenuPermissionService;
import com.example.medical_be.swagger.ConfigurationApiExamples;
import com.example.medical_be.swagger.GroupAPIConstant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(APIRoutes.API_V1)
@RequiredArgsConstructor
@Tag(name = GroupAPIConstant.MENU_PERMISSION, description = "APIs trả về menu hiển thị theo quyền của user hiện tại")
public class MenuController {

    private final IMenuPermissionService menuPermissionService;

    @Operation(
            summary = "Lấy menu theo quyền user",
            description = "Trả về danh sách menu items mà user hiện tại có quyền view, kèm danh sách actions được phép.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Lấy menu thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(value = ConfigurationApiExamples.MENU_RESPONSE)))
    @PreAuthorize("isAuthenticated()")
    @GetMapping(APIRoutes.CONFIG_PERMISSION_MENU)
    public ResponseEntity<JSONResponse<?>> getMenu() {
        return ResponseEntity.ok(JSONResponse.<List<MenuItemRes>>builder()
                .isError(false)
                .data(menuPermissionService.menuPermissionOfUser())
                .build());
    }
}
