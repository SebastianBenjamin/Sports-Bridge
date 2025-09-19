package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "athletes")
public class Athlete {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Double height, weight;
    private Boolean isDisabled;
    private String disabilityType;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String state,district;
//    private Coach currentCoach;
//    private List<Coach> previousCoaches;

    // Relations
    @OneToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    private Coach currentCoach;

    @ManyToMany
    private List<Coach> previousCoaches;
}
