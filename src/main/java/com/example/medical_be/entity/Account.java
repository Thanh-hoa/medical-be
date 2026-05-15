package com.example.medical_be.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name="account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
     Long id;
     @Column(name="name" )
     String name;

     @Column(name="birthday")
     LocalDate birthday;

     @Column(name="phone_number" , length = 12)
     String phoneNumber;

     @Column(name="email" , unique = true , nullable = false )
     String email;

     @Column(name="username" , unique = true , nullable = false)
     String username;

     @Column(name="password" , nullable = false)
     String password;

     @Column(name="is_active")
     Boolean isActive;

     @Column(name="is_delete")
     @Builder.Default
     Boolean isDelete = false;

     @Column(name="email_verify_at")
     LocalDateTime emailVerifyAt;

     @Column(name="created_by" )
     Long createdBy;
     
     @Column(name="gender")
     @Builder.Default
     Gender gender = Gender.other;

     @Column(name="photo_url")
     String photoUrl;

     @Column(name="created_at")
     @Builder.Default
     LocalDateTime createdAt = LocalDateTime.now();

     @Column(name="updated_at")
     LocalDateTime updatedAt;


    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    List<RfAccountRole> rfAccountRoles;
}