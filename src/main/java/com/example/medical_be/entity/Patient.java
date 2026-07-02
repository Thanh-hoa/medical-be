package com.example.medical_be.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.medical_be.converter.EncryptedLocalDateConverter;
import com.example.medical_be.converter.EncryptedStringConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bhyt", length = 500)
    String bhyt;

    @Column(name = "bhyt_hash", length = 64)
    String bhytHash;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "citizen_id", length = 500)
    String citizenId;

    @Column(name = "citizen_id_hash", length = 64)
    String citizenIdHash;

    @Column(name = "account_id")
    Long accountId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "name", length = 500, nullable = false)
    String name;

    @Convert(converter = EncryptedLocalDateConverter.class)
    @Column(name = "dob", length = 500)
    LocalDate dob;

    @Column(name = "gender", length = 10)
    String gender;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "address", columnDefinition = "text")
    String address;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "phone", length = 500)
    String phone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
