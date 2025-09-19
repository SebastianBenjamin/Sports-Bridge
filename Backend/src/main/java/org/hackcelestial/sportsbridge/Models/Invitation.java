package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class Invitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt, respondedAt;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;


}