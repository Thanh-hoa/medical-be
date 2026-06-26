package com.example.medical_be.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "actor_id")
    Long actorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "actor_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_audit_logs_actor"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    Account actor;

    @Column(name = "action", length = 50, nullable = false)
    String action;

    @Column(name = "resource_type", length = 50)
    String resourceType;

    @Column(name = "resource_id")
    Long resourceId;

    @Column(name = "old_value", columnDefinition = "text")
    String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    String newValue;

    @Column(name = "ip_address", length = 50)
    String ipAddress;

    @Column(name = "user_agent", length = 500)
    String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;
}
