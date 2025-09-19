package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Repositories.SponsorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SponserService {
    @Autowired
    SponsorRepository sponsorRepository;
    public boolean save(Sponsor sponsor) {
        return sponsorRepository.save(sponsor)!=null;
    }
}
