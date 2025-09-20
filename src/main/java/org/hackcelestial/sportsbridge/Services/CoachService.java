package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Repositories.CoachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoachService {
    @Autowired
    CoachRepository coachRepository;
    public boolean save(Coach coach) {
        return coachRepository.save(coach) != null;
    }
}
