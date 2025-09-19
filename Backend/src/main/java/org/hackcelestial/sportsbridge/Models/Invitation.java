package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;

import java.time.LocalDateTime;

public class Invitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String message;
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;
    private LocalDateTime sentAt, respondedAt;
}