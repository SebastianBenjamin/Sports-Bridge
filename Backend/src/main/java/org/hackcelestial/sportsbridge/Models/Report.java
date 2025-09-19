package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportedEntityId;

    private String reportedEntityType;

    private String reason;

    private String description;

    private LocalDateTime createdAt, reviewedAt;

    private String reportConclusion;
    private Boolean solved;
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
}
