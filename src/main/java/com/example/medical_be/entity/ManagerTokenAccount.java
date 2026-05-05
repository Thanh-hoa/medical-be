package com.example.medical_be.entity;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Builder.Default;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name="tbl_manager_token_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagerTokenAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String token;
    
    @Column(name="is_acitve")
    boolean isActive;

    @Column(name="expired_at")
    LocalDateTime expiredAt;

    @Column(name="created_at")
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at")
    LocalDateTime updatedAt;

    @Column(name="account_id")
    Long accountId;

    @ManyToOne
    @JoinColumn(name="account_id" , insertable = false , updatable =  false)
    Account account;
    
}
