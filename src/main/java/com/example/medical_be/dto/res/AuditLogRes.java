package com.example.medical_be.dto.res;

import java.time.LocalDateTime;

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
public class AuditLogRes {
    Long id;
    Long actorId;
    String actorName;
    String action;
    String actionLabel;
    String resourceType;
    Long resourceId;
    String oldValue;
    String newValue;
    String ipAddress;
    String userAgent;
    LocalDateTime createdAt;
}
