package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "achievement_date")
    private LocalDate achievementDate;

    @Column(name = "category")
    private String category; // e.g., "Competition", "Training", "Certification", "Recognition"

    @Column(name = "level")
    private String level; // e.g., "Local", "Regional", "National", "International"

    @Column(name = "organization")
    private String organization; // Organization that awarded the achievement

    @Column(name = "position")
    private String position; // e.g., "1st Place", "Gold Medal", "Winner"

    @Column(name = "certificate_url")
    private String certificateUrl; // URL to certificate/proof image

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
