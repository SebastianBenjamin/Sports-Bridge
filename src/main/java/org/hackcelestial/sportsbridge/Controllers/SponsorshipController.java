package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sponsorships")
public class SponsorshipController {

    @Autowired
    private SponsorshipService sponsorshipService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private SponsorService sponsorService;

    @Autowired
    private HttpSession session;

    @GetMapping("/athlete")
    public ResponseEntity<Map<String, Object>> getAthleteSponsorships() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            if (!"ATHLETE".equals(user.getRole().toString())) {
                response.put("success", false);
                response.put("message", "Access denied. Only athletes can view their sponsorships");
                return ResponseEntity.status(403).body(response);
            }

            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "Athlete profile not found");
                return ResponseEntity.status(404).body(response);
            }

            List<Sponsorship> sponsorships = sponsorshipService.getSponsorshipsByAthlete(athlete);
            List<Sponsorship> activeSponsorships = sponsorshipService.getActiveSponsorshipsByAthlete(athlete);

            List<Map<String, Object>> sponsorshipResponses = sponsorships.stream()
                    .map(sponsorshipService::createSponsorshipResponse)
                    .toList();

            List<Map<String, Object>> activeSponsorshipResponses = activeSponsorships.stream()
                    .map(sponsorshipService::createSponsorshipResponse)
                    .toList();

            response.put("success", true);
            response.put("sponsorships", sponsorshipResponses);
            response.put("activeSponsorships", activeSponsorshipResponses);
            response.put("totalCount", sponsorships.size());
            response.put("activeCount", activeSponsorships.size());

        } catch (Exception e) {
            System.out.println("Error fetching athlete sponsorships: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching sponsorships");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sponsor")
    public ResponseEntity<Map<String, Object>> getSponsorSponsorships() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            if (!"SPONSOR".equals(user.getRole().toString())) {
                response.put("success", false);
                response.put("message", "Access denied. Only sponsors can view their sponsorships");
                return ResponseEntity.status(403).body(response);
            }

            Sponsor sponsor = sponsorService.getSponsorByUser(user);
            if (sponsor == null) {
                response.put("success", false);
                response.put("message", "Sponsor profile not found");
                return ResponseEntity.status(404).body(response);
            }

            List<Sponsorship> sponsorships = sponsorshipService.getSponsorshipsBySponsor(sponsor);

            List<Map<String, Object>> sponsorshipResponses = sponsorships.stream()
                    .map(sponsorshipService::createSponsorshipResponse)
                    .toList();

            response.put("success", true);
            response.put("sponsorships", sponsorshipResponses);
            response.put("totalCount", sponsorships.size());

        } catch (Exception e) {
            System.out.println("Error fetching sponsor sponsorships: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching sponsorships");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSponsorshipDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Sponsorship sponsorship = sponsorshipService.getSponsorshipById(id);
            if (sponsorship == null) {
                response.put("success", false);
                response.put("message", "Sponsorship not found");
                return ResponseEntity.status(404).body(response);
            }

            // Check if user has access to this sponsorship
            boolean hasAccess = false;
            if ("ATHLETE".equals(user.getRole().toString())) {
                Athlete athlete = athleteService.getAthleteByUser(user);
                hasAccess = athlete != null && athlete.getId().equals(sponsorship.getAthlete().getId());
            } else if ("SPONSOR".equals(user.getRole().toString())) {
                Sponsor sponsor = sponsorService.getSponsorByUser(user);
                hasAccess = sponsor != null && sponsor.getId().equals(sponsorship.getSponsor().getId());
            }

            if (!hasAccess) {
                response.put("success", false);
                response.put("message", "Access denied");
                return ResponseEntity.status(403).body(response);
            }

            Map<String, Object> sponsorshipData = sponsorshipService.createSponsorshipResponse(sponsorship);
            response.put("success", true);
            response.put("sponsorship", sponsorshipData);

        } catch (Exception e) {
            System.out.println("Error fetching sponsorship details: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching sponsorship details");
        }

        return ResponseEntity.ok(response);
    }
}
