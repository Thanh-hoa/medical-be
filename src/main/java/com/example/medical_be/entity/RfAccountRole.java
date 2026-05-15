package com.example.medical_be.entity;
import java.time.LocalDateTime;

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
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name="tbl_rf_account_role")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RfAccountRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name="account_id")
    Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id", insertable = false , updatable = false)
    @ToString.Exclude
    Account account;

    @Column(name="role_id")
    Long roleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="role_id" , insertable = false , updatable = false)
    @ToString.Exclude
    Role role;

    @Column(name="created_at")
    LocalDateTime createdAt ;

    @Column(name="updated_at")
    LocalDateTime updatedAt;
    
}
