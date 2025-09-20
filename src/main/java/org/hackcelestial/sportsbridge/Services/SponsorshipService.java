package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;
import org.hackcelestial.sportsbridge.Repositories.SponsorshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SponsorshipService {

    @Autowired
    private SponsorshipRepository sponsorshipRepository;

    @Autowired
    private UtilityService utilityService;

    public List<Sponsorship> getSponsorshipsByAthlete(Athlete athlete) {
        try {
            return sponsorshipRepository.findByAthleteOrderByCreatedAtDesc(athlete);
        } catch (Exception e) {
            System.out.println("Error fetching sponsorships for athlete: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Sponsorship> getSponsorshipsBySponsor(Sponsor sponsor) {
        try {
            return sponsorshipRepository.findBySponsorOrderByCreatedAtDesc(sponsor);
        } catch (Exception e) {
            System.out.println("Error fetching sponsorships for sponsor: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Sponsorship> getActiveSponsorshipsByAthlete(Athlete athlete) {
        try {
            return sponsorshipRepository.findActiveByAthlete(athlete, InvitationStatus.ACCEPTED);
        } catch (Exception e) {
            System.out.println("Error fetching active sponsorships for athlete: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> createSponsorshipResponse(Sponsorship sponsorship) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", sponsorship.getId());
        response.put("amount", sponsorship.getAmount());
        response.put("currency", sponsorship.getCurrency().toString());
        response.put("terms", sponsorship.getTerms());
        response.put("status", sponsorship.getStatus().toString());
        response.put("contractStartDate", sponsorship.getContractStartDate());
        response.put("contractEndDate", sponsorship.getContractEndDate());
        response.put("createdAt", sponsorship.getCreatedAt());

        // Sponsor info
        if (sponsorship.getSponsor() != null && sponsorship.getSponsor().getUser() != null) {
            Map<String, Object> sponsorInfo = new HashMap<>();
            User sponsorUser = sponsorship.getSponsor().getUser();

            sponsorInfo.put("id", sponsorship.getSponsor().getId());
            sponsorInfo.put("userId", sponsorUser.getId());
            sponsorInfo.put("name", sponsorUser.getFirstName() + " " + sponsorUser.getLastName());
            sponsorInfo.put("email", sponsorUser.getEmail());
            sponsorInfo.put("companyName", sponsorship.getSponsor().getCompanyName());
            sponsorInfo.put("industry", sponsorship.getSponsor().getIndustry());
            sponsorInfo.put("website", sponsorship.getSponsor().getWebsite());
            sponsorInfo.put("budgetRange", sponsorship.getSponsor().getBudgetRange());


            if (sponsorUser.getProfileImageUrl() != null) {
                sponsorInfo.put("profileImageUrl", utilityService.convertFilePathToWebUrl(sponsorUser.getProfileImageUrl()));
            }

            response.put("sponsor", sponsorInfo);
        }

        // Athlete info
        if (sponsorship.getAthlete() != null && sponsorship.getAthlete().getUser() != null) {
            Map<String, Object> athleteInfo = new HashMap<>();
            User athleteUser = sponsorship.getAthlete().getUser();

            athleteInfo.put("id", sponsorship.getAthlete().getId());
            athleteInfo.put("userId", athleteUser.getId());
            athleteInfo.put("name", athleteUser.getFirstName() + " " + athleteUser.getLastName());
            athleteInfo.put("email", athleteUser.getEmail());
            athleteInfo.put("sport", getSportFromUser(athleteUser));
            athleteInfo.put("state", sponsorship.getAthlete().getState());
            athleteInfo.put("district", sponsorship.getAthlete().getDistrict());

            if (athleteUser.getProfileImageUrl() != null) {
                athleteInfo.put("profileImageUrl", utilityService.convertFilePathToWebUrl(athleteUser.getProfileImageUrl()));
            }

            response.put("athlete", athleteInfo);
        }

        return response;
    }

    private String getSportFromUser(User user) {
        // Helper method to extract sport information from user's bio or other fields
        // This can be enhanced based on your User model structure
        return user.getBio() != null ? user.getBio() : "Not specified";
    }

    public Sponsorship getSponsorshipById(Long id) {
        try {
            return sponsorshipRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.out.println("Error fetching sponsorship by ID: " + e.getMessage());
            return null;
        }
    }

    public Sponsorship saveSponsorShip(Sponsorship sponsorship) {
        try {
            return sponsorshipRepository.save(sponsorship);
        } catch (Exception e) {
            System.out.println("Error saving sponsorship: " + e.getMessage());
            return null;
        }
    }
}
