package org.hackcelestial.sportsbridge.Api.Entities;

import jakarta.persistence.*;
import org.hackcelestial.sportsbridge.Enums.ApiUserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "sb_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sb_user_phone", columnNames = {"phone"}),
                @UniqueConstraint(name = "uk_sb_user_aadhaar_hash", columnNames = {"aadhaar_hash"})
        },
        indexes = {
                @Index(name = "idx_sb_user_created_at", columnList = "created_at")
        })
public class SbUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ApiUserRole role;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone; // E.164

    @Lob
    @Column(name = "aadhaar_encrypted", nullable = false)
    private byte[] aadhaarEncrypted; // AES-GCM ciphertext including IV + tag format (we store as bytes)

    @Column(name = "aadhaar_hash", nullable = false, length = 64)
    private String aadhaarHash; // hex SHA-256(pepper + aadhaar)

    @Column(name = "email")
    private String email;

    @Column(name = "profile_pic_url")
    private String profilePicUrl;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "password_hash", length = 100)
    private String passwordHash; // BCrypt

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public ApiUserRole getRole() { return role; }
    public void setRole(ApiUserRole role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public byte[] getAadhaarEncrypted() { return aadhaarEncrypted; }
    public void setAadhaarEncrypted(byte[] aadhaarEncrypted) { this.aadhaarEncrypted = aadhaarEncrypted; }

    public String getAadhaarHash() { return aadhaarHash; }
    public void setAadhaarHash(String aadhaarHash) { this.aadhaarHash = aadhaarHash; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
