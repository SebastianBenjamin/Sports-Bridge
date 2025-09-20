package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.CoachRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CoachService {
    @Autowired
    CoachRepository coachRepository;

    @Autowired
    UserService userService;

    public boolean save(Coach coach) {
        return coachRepository.save(coach) != null;
    }

    public Coach getCoachByUser(User user) {
        try {
            return coachRepository.findByUser(user);
        } catch (Exception e) {
            System.out.println("Error finding coach by user: " + e.getMessage());
            return null;
        }
    }

    public Coach getCoachByUserId(Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                return coachRepository.findByUser(user);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error finding coach by user ID: " + e.getMessage());
            return null;
        }
    }

    public boolean updateCoach(Coach coach) {
        try {
            return coachRepository.save(coach) != null;
        } catch (Exception e) {
            System.out.println("Error updating coach: " + e.getMessage());
            return false;
        }
    }

    public java.util.List<Coach> getAllCoaches() {
        try {
            return coachRepository.findAll();
        } catch (Exception e) {
            System.out.println("Error fetching all coaches: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
