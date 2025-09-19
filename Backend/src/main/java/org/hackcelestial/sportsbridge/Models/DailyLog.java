package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_logs")
public class DailyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Athlete athlete;

    @ManyToOne
    private Sport sport;

    private LocalDate logDate;
    private Integer trainingDurationMinutes;
    private String trainingType, notes;
    private LocalDateTime createdAt;
}
