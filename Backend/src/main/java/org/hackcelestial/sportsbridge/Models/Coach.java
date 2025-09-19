package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;


@Entity
@Table(name = "coaches")
public class Coach {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne @JoinColumn(name = "user_id")
    private User user;

    private String authority;
    private String specialization;
    private Integer experienceYears;
    private String state,district;

}
