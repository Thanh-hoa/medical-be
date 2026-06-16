package com.example.medical_be.dto.req.webhook;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateWebhookReq {

    @NotBlank(message = "webhook.url.required")
    String url;

    @NotBlank(message = "webhook.event_types.required")
    String eventTypes;

    @NotBlank(message = "webhook.secret.required")
    String secret;

    @Builder.Default
    Boolean isActive = true;
}
