package com.example.medical_be.dto.res;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InfoAccountRes {
     String id;
     String name;
     LocalDate birthday;
     String phoneNumber;
     String username;
     String email;
     Boolean isActive;
     LocalDateTime emailVerifyAt;
     String photoUrl;
//     List<Role> role;
     LocalDateTime createdAt;
     LocalDateTime updatedAt;

    //public record Role ( Long id , String name) {}
}
