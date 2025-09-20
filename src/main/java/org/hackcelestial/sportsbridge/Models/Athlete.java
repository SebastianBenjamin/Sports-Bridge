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


    @OneToOne @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    private Coach currentCoach;

    @ManyToOne
    private Sponsor currentSponsor;


    @ManyToMany
    private List<Coach> previousCoaches;

    @ManyToMany
    private List<Sponsor> previousSponsors;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(Boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public String getDisabilityType() {
        return disabilityType;
    }

    public void setDisabilityType(String disabilityType) {
        this.disabilityType = disabilityType;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Coach getCurrentCoach() {
        return currentCoach;
    }

    public void setCurrentCoach(Coach currentCoach) {
        this.currentCoach = currentCoach;
    }

    public Sponsor getCurrentSponsor() {
        return currentSponsor;
    }

    public void setCurrentSponsor(Sponsor currentSponsor) {
        this.currentSponsor = currentSponsor;
    }

    public List<Coach> getPreviousCoaches() {
        return previousCoaches;
    }

    public void setPreviousCoaches(List<Coach> previousCoaches) {
        this.previousCoaches = previousCoaches;
    }

    public List<Sponsor> getPreviousSponsors() {
        return previousSponsors;
    }

    public void setPreviousSponsors(List<Sponsor> previousSponsors) {
        this.previousSponsors = previousSponsors;
    }

    public Athlete() {
    }
}
