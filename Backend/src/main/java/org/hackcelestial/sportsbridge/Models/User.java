package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;
import org.hackcelestial.sportsbridge.Enums.Gender;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name ="email",unique = true, nullable = false)
    private String email;
    @Column(name ="password",nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name ="role",nullable = false)
    private UserRole role;

    private String firstName, lastName, bio, profileImageUrl,phone,country;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name ="active")
    private Boolean isActive;

    @Column(name="createdAt")
    private LocalDateTime createdAt;

    private String aadhaarNumber;

    @LastModifiedDate
    @Column(name = "updatedAt")
    private LocalDateTime updatedAt;


}
