package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;

import java.time.*;

@Entity
@Table(name = "achievements")
public class Achievement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Athlete athlete;
    private String title, description, competitionName, certificateUrl;
    private LocalDate achievementDate;
    private Integer rankPosition;
    private LocalDateTime createdAt;
}
