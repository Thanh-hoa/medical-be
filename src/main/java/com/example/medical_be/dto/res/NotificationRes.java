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
public class NotificationRes {
    Long id;
    String title;
    String message;
    String type;
    String resourceType;
    Long resourceId;
    Boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime readAt;
}
