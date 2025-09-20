package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private SponsorService sponsorService;

    @Autowired
    private HttpSession session;

    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> getProfileData() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get fresh user data from database
            User freshUser = userService.getUserById(user.getId());
            if (freshUser == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            // Build personal data
            Map<String, Object> personalData = new HashMap<>();
            personalData.put("id", freshUser.getId());
            personalData.put("firstName", freshUser.getFirstName());
            personalData.put("lastName", freshUser.getLastName());
            personalData.put("email", freshUser.getEmail());
            personalData.put("phone", freshUser.getPhone());
            personalData.put("bio", freshUser.getBio());
            personalData.put("country", freshUser.getCountry());
            personalData.put("gender", freshUser.getGender());
            personalData.put("profileImageUrl", freshUser.getProfileImageUrl());
            personalData.put("aadhaarNumber", freshUser.getAadhaarNumber());

            response.put("personal", personalData);
            response.put("role", freshUser.getRole().toString().toLowerCase());

            // Get professional data based on role
            Map<String, Object> professionalData = new HashMap<>();

            switch (freshUser.getRole()) {
                case ATHLETE:
                    Athlete athlete = athleteService.getAthleteByUser(freshUser);
                    if (athlete != null) {
                        professionalData.put("id", athlete.getId());
                        professionalData.put("height", athlete.getHeight());
                        professionalData.put("weight", athlete.getWeight());
                        professionalData.put("isDisabled", athlete.getIsDisabled());
                        professionalData.put("disabilityType", athlete.getDisabilityType());
                        professionalData.put("emergencyContactName", athlete.getEmergencyContactName());
                        professionalData.put("emergencyContactPhone", athlete.getEmergencyContactPhone());
                        professionalData.put("state", athlete.getState());
                        professionalData.put("district", athlete.getDistrict());
                        if (athlete.getCurrentCoach() != null) {
                            Map<String, Object> coachData = new HashMap<>();
                            coachData.put("id", athlete.getCurrentCoach().getId());
                            coachData.put("name", athlete.getCurrentCoach().getUser().getFirstName() + " " +
                                                  athlete.getCurrentCoach().getUser().getLastName());
                            professionalData.put("currentCoach", coachData);
                        }
                    }
                    break;

                case COACH:
                    Coach coach = coachService.getCoachByUser(freshUser);
                    if (coach != null) {
                        professionalData.put("id", coach.getId());
                        professionalData.put("authority", coach.getAuthority());
                        professionalData.put("specialization", coach.getSpecialization());
                        professionalData.put("experienceYears", coach.getExperienceYears());
                        professionalData.put("state", coach.getState());
                        professionalData.put("district", coach.getDistrict());
                    }
                    break;

                case SPONSOR:
                    Sponsor sponsor = sponsorService.getSponsorByUser(freshUser);
                    if (sponsor != null) {
                        professionalData.put("id", sponsor.getId());
                        professionalData.put("companyName", sponsor.getCompanyName());
                        professionalData.put("industry", sponsor.getIndustry());
                        professionalData.put("website", sponsor.getWebsite());
                        professionalData.put("budgetRange", sponsor.getBudgetRange());
                    }
                    break;
            }

            response.put("professional", professionalData);
            response.put("success", true);

        } catch (Exception e) {
            System.out.println("Error fetching profile data: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching profile data");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/personal")
    public ResponseEntity<Map<String, Object>> updatePersonalData(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "aadhaarNumber", required = false) String aadhaarNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get fresh user data
            User freshUser = userService.getUserById(user.getId());
            if (freshUser == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            // Update fields if provided
            if (firstName != null && !firstName.trim().isEmpty()) {
                freshUser.setFirstName(firstName.trim());
            }
            if (lastName != null && !lastName.trim().isEmpty()) {
                freshUser.setLastName(lastName.trim());
            }
            if (phone != null && !phone.trim().isEmpty()) {
                freshUser.setPhone(phone.trim());
            }
            if (bio != null) {
                freshUser.setBio(bio.trim());
            }
            if (country != null && !country.trim().isEmpty()) {
                freshUser.setCountry(country.trim());
            }
            if (gender != null && !gender.trim().isEmpty()) {
                try {
                    freshUser.setGender(org.hackcelestial.sportsbridge.Enums.Gender.valueOf(gender.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", "Invalid gender value");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            if (aadhaarNumber != null && !aadhaarNumber.trim().isEmpty()) {
                freshUser.setAadhaarNumber(aadhaarNumber.trim());
            }

            freshUser.setUpdatedAt(LocalDateTime.now());

            boolean saved = userService.updateUser(freshUser);
            if (saved) {
                // Update session with fresh user data
                session.setAttribute("user", freshUser);
                response.put("success", true);
                response.put("message", "Personal information updated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to update personal information");
            }

        } catch (Exception e) {
            System.out.println("Error updating personal data: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating personal information");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/professional")
    public ResponseEntity<Map<String, Object>> updateProfessionalData(@RequestBody Map<String, Object> professionalData) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get fresh user data
            User freshUser = userService.getUserById(user.getId());
            if (freshUser == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(404).body(response);
            }

            boolean saved = false;

            switch (freshUser.getRole()) {
                case ATHLETE:
                    saved = updateAthleteData(freshUser, professionalData);
                    break;
                case COACH:
                    saved = updateCoachData(freshUser, professionalData);
                    break;
                case SPONSOR:
                    saved = updateSponsorData(freshUser, professionalData);
                    break;
            }

            if (saved) {
                response.put("success", true);
                response.put("message", "Professional information updated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to update professional information");
            }

        } catch (Exception e) {
            System.out.println("Error updating professional data: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error updating professional information");
        }

        return ResponseEntity.ok(response);
    }

    private boolean updateAthleteData(User user, Map<String, Object> data) {
        try {
            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                return false;
            }

            if (data.containsKey("height") && data.get("height") != null) {
                athlete.setHeight(Double.valueOf(data.get("height").toString()));
            }
            if (data.containsKey("weight") && data.get("weight") != null) {
                athlete.setWeight(Double.valueOf(data.get("weight").toString()));
            }
            if (data.containsKey("isDisabled")) {
                athlete.setIsDisabled(Boolean.valueOf(data.get("isDisabled").toString()));
            }
            if (data.containsKey("disabilityType")) {
                athlete.setDisabilityType(data.get("disabilityType").toString());
            }
            if (data.containsKey("emergencyContactName")) {
                athlete.setEmergencyContactName(data.get("emergencyContactName").toString());
            }
            if (data.containsKey("emergencyContactPhone")) {
                athlete.setEmergencyContactPhone(data.get("emergencyContactPhone").toString());
            }
            if (data.containsKey("state")) {
                athlete.setState(data.get("state").toString());
            }
            if (data.containsKey("district")) {
                athlete.setDistrict(data.get("district").toString());
            }

            return athleteService.updateAthlete(athlete);
        } catch (Exception e) {
            System.out.println("Error updating athlete data: " + e.getMessage());
            return false;
        }
    }

    private boolean updateCoachData(User user, Map<String, Object> data) {
        try {
            Coach coach = coachService.getCoachByUser(user);
            if (coach == null) {
                return false;
            }

            if (data.containsKey("authority")) {
                coach.setAuthority(data.get("authority").toString());
            }
            if (data.containsKey("specialization")) {
                coach.setSpecialization(data.get("specialization").toString());
            }
            if (data.containsKey("experienceYears") && data.get("experienceYears") != null) {
                coach.setExperienceYears(Integer.valueOf(data.get("experienceYears").toString()));
            }
            if (data.containsKey("state")) {
                coach.setState(data.get("state").toString());
            }
            if (data.containsKey("district")) {
                coach.setDistrict(data.get("district").toString());
            }

            return coachService.updateCoach(coach);
        } catch (Exception e) {
            System.out.println("Error updating coach data: " + e.getMessage());
            return false;
        }
    }

    private boolean updateSponsorData(User user, Map<String, Object> data) {
        try {
            Sponsor sponsor = sponsorService.getSponsorByUser(user);
            if (sponsor == null) {
                return false;
            }

            if (data.containsKey("companyName")) {
                sponsor.setCompanyName(data.get("companyName").toString());
            }
            if (data.containsKey("industry")) {
                sponsor.setIndustry(data.get("industry").toString());
            }
            if (data.containsKey("website")) {
                sponsor.setWebsite(data.get("website").toString());
            }
            if (data.containsKey("budgetRange")) {
                sponsor.setBudgetRange(data.get("budgetRange").toString());
            }

            return sponsorService.updateSponsor(sponsor);
        } catch (Exception e) {
            System.out.println("Error updating sponsor data: " + e.getMessage());
            return false;
        }
    }
}
