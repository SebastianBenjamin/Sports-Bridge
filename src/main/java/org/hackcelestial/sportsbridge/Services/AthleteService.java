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

    @Autowired
    UserService userService;

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

    public Athlete getAthleteByUserId(Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                return athleteRepository.findByUser(user);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error finding athlete by user ID: " + e.getMessage());
            return null;
        }
    }

    public boolean save(Athlete athlete) {
        return athleteRepository.save(athlete) != null;
    }

    public boolean updateAthlete(Athlete athlete) {
        try {
            return athleteRepository.save(athlete) != null;
        } catch (Exception e) {
            System.out.println("Error updating athlete: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<Athlete> getAllAthletes() {
        try {
            return athleteRepository.findAll();
        } catch (Exception e) {
            System.out.println("Error fetching all athletes: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
