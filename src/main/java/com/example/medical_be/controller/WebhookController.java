package com.example.medical_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.medical_be.dto.req.webhook.CreateWebhookReq;
import com.example.medical_be.dto.req.webhook.UpdateWebhookReq;
import com.example.medical_be.dto.res.WebhookRes;
import com.example.medical_be.routes.APIRoutes;
import com.example.medical_be.service.WebhookService;

import java.util.List;

@RestController
@RequestMapping(APIRoutes.API_V1 + "/" + APIRoutes.WEBHOOK_ROUTE)
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "Webhook Management (Admin only)")
@PreAuthorize("hasAuthority('admin')")
public class WebhookController {

    private final WebhookService webhookService;

    @PostMapping
    @Operation(summary = "Create webhook")
    public ResponseEntity<WebhookRes> create(@Valid @RequestBody CreateWebhookReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(webhookService.create(req));
    }

    @GetMapping
    @Operation(summary = "List all webhooks")
    public ResponseEntity<List<WebhookRes>> list() {
        return ResponseEntity.ok(webhookService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get webhook detail")
    public ResponseEntity<WebhookRes> detail(@PathVariable Long id) {
        return ResponseEntity.ok(webhookService.detail(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update webhook")
    public ResponseEntity<WebhookRes> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWebhookReq req) {
        return ResponseEntity.ok(webhookService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete webhook")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        webhookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
