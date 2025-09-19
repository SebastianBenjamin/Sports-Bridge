package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "athletes")
public class Athlete {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "user_id")
    private User user;
    private Double height, weight;
    private Boolean isDisabled;
    private String disabilityType;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String state,district;


}
