package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private User reporter;

    private Long reportedEntityId;

    private String reportedEntityType;

    private String reason;

    private String description;

    private LocalDateTime createdAt, reviewedAt;
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

}
