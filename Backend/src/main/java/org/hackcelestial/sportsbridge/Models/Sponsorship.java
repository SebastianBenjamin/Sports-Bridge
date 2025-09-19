package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;
import org.hackcelestial.sportsbridge.Enums.CurrencyType;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "sponsorships")
public class Sponsorship {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate contractStartDate, contractEndDate;
    private long amount;
    private CurrencyType currency;
    private String  terms;
    private InvitationStatus status;
    private LocalDateTime createdAt;

    @ManyToOne
    private Sponsor sponsor;

    @ManyToOne
    private Athlete athlete;


}