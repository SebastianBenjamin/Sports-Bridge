package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.SponsorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SponsorService {
    @Autowired
    SponsorRepository sponsorRepository;

    public boolean save(Sponsor sponsor) {
        return sponsorRepository.save(sponsor) != null;
    }

    public Sponsor getSponsorByUser(User user) {
        try {
            return sponsorRepository.findByUser(user);
        } catch (Exception e) {
            System.out.println("Error finding sponsor by user: " + e.getMessage());
            return null;
        }
    }

    public boolean updateSponsor(Sponsor sponsor) {
        try {
            return sponsorRepository.save(sponsor) != null;
        } catch (Exception e) {
            System.out.println("Error updating sponsor: " + e.getMessage());
            return false;
        }
    }
}
