package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.CoachRelationship;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.CoachRelationshipService;
import org.hackcelestial.sportsbridge.Services.UserService;
import org.hackcelestial.sportsbridge.Services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coach-relationship")
public class CoachRelationshipController {

    @Autowired
    private CoachRelationshipService coachRelationshipService;

    @Autowired
    private UserService userService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private HttpSession session;

    @GetMapping("/my-coach")
    public ResponseEntity<Map<String, Object>> getMyCoach() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get current coach
            var currentCoachRelationship = coachRelationshipService.getCurrentCoach(user.getId());
            Map<String, Object> currentCoachData = null;

            if (currentCoachRelationship.isPresent()) {
                CoachRelationship relationship = currentCoachRelationship.get();
                User coach = relationship.getCoach();
                currentCoachData = createCoachResponse(coach, relationship);
            }

            // Get past coaches
            List<CoachRelationship> pastCoachRelationships = coachRelationshipService.getPastCoaches(user.getId());
            List<Map<String, Object>> pastCoachesData = new ArrayList<>();

            for (CoachRelationship relationship : pastCoachRelationships) {
                User coach = relationship.getCoach();
                pastCoachesData.add(createCoachResponse(coach, relationship));
            }

            response.put("success", true);
            response.put("currentCoach", currentCoachData);
            response.put("pastCoaches", pastCoachesData);

        } catch (Exception e) {
            System.out.println("Error fetching coach information: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching coach information");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/coach-profile/{coachId}")
    public ResponseEntity<Map<String, Object>> getCoachProfile(@PathVariable Long coachId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            User coach = userService.getUserById(coachId);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "Coach not found");
                return ResponseEntity.status(404).body(response);
            }

            // Create detailed coach profile response
            Map<String, Object> coachProfile = createDetailedCoachProfile(coach);
            response.put("success", true);
            response.put("coach", coachProfile);

        } catch (Exception e) {
            System.out.println("Error fetching coach profile: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching coach profile");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign-coach/{coachId}")
    public ResponseEntity<Map<String, Object>> assignCoach(@PathVariable Long coachId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User athlete = (User) session.getAttribute("user");
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            User coach = userService.getUserById(coachId);
            if (coach == null) {
                response.put("success", false);
                response.put("message", "Coach not found");
                return ResponseEntity.status(404).body(response);
            }

            boolean assigned = coachRelationshipService.assignCoach(athlete, coach);

            if (assigned) {
                response.put("success", true);
                response.put("message", "Coach assigned successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to assign coach");
            }

        } catch (Exception e) {
            System.out.println("Error assigning coach: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error assigning coach");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/end-coaching")
    public ResponseEntity<Map<String, Object>> endCoaching() {
        Map<String, Object> response = new HashMap<>();

        try {
            User athlete = (User) session.getAttribute("user");
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            boolean ended = coachRelationshipService.endCoachRelationship(athlete.getId());

            if (ended) {
                response.put("success", true);
                response.put("message", "Coaching relationship ended successfully");
            } else {
                response.put("success", false);
                response.put("message", "No active coaching relationship found");
            }

        } catch (Exception e) {
            System.out.println("Error ending coaching relationship: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error ending coaching relationship");
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createCoachResponse(User coach, CoachRelationship relationship) {
        Map<String, Object> coachData = new HashMap<>();
        coachData.put("id", coach.getId());
        coachData.put("firstName", coach.getFirstName());
        coachData.put("lastName", coach.getLastName());
        coachData.put("email", coach.getEmail());
        coachData.put("phone", coach.getPhone());
        coachData.put("bio", coach.getBio());
        coachData.put("country", coach.getCountry());

        // Convert profile image URL if exists
        if (coach.getProfileImageUrl() != null) {
            coachData.put("profileImageUrl", utilityService.convertFilePathToWebUrl(coach.getProfileImageUrl()));
        } else {
            coachData.put("profileImageUrl", null);
        }

        // Add relationship details
        coachData.put("startDate", relationship.getStartDate());
        coachData.put("endDate", relationship.getEndDate());
        coachData.put("isActive", relationship.getActive());
        coachData.put("notes", relationship.getNotes());

        return coachData;
    }

    private Map<String, Object> createDetailedCoachProfile(User coach) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", coach.getId());
        profile.put("firstName", coach.getFirstName());
        profile.put("lastName", coach.getLastName());
        profile.put("email", coach.getEmail());
        profile.put("phone", coach.getPhone());
        profile.put("bio", coach.getBio());
        profile.put("country", coach.getCountry());
        profile.put("gender", coach.getGender());
        profile.put("role", coach.getRole());
        profile.put("createdAt", coach.getCreatedAt());

        // Convert profile image URL if exists
        if (coach.getProfileImageUrl() != null) {
            profile.put("profileImageUrl", utilityService.convertFilePathToWebUrl(coach.getProfileImageUrl()));
        } else {
            profile.put("profileImageUrl", null);
        }

        // Get current athletes count for this coach
        List<CoachRelationship> currentAthletes = coachRelationshipService.getCurrentAthletes(coach.getId());
        profile.put("currentAthletesCount", currentAthletes.size());

        return profile;
    }
}
