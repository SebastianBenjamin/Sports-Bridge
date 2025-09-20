package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.AthleteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AthleteService {
    @Autowired
    AthleteRepository athleteRepository;

    public Athlete getAthleteById(int id) {
        return athleteRepository.findById(id).get();
    }

    public Athlete getAthleteByUser(User user) {
        try {
            return athleteRepository.findByUser(user);
        } catch (Exception e) {
            System.out.println("Error finding athlete by user: " + e.getMessage());
            return null;
        }
    }

    public boolean save(Athlete athlete) {
        return athleteRepository.save(athlete) != null;
    }
}
