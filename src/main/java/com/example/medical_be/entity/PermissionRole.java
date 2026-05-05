package com.example.medical_be.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name="permission_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PermissionRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "permission_id")
    Long permissionId;

    @Column(name = "permission_action_id")
    Long permissionActionId;

    @Column(name = "role_id")
    Long roleId;

    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "permission_id",  insertable = false, updatable = false )
    Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_action_id",  insertable = false, updatable = false)
    PermissionAction permissionAction;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id",  insertable = false, updatable = false)
    Role role;
}
