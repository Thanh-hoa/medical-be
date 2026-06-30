package com.example.medical_be.dto.res;

import java.time.LocalDate;
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
public class PatientRes {
    Long id;
    String bhyt;
    String citizenId;
    String name;
    LocalDate dob;
    String gender;
    String address;
    String phone;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
