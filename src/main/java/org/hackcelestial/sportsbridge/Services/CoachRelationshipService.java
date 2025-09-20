package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.CoachRelationship;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.CoachRelationshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CoachRelationshipService {

    @Autowired
    private CoachRelationshipRepository coachRelationshipRepository;

    public Optional<CoachRelationship> getCurrentCoach(Long athleteId) {
        return coachRelationshipRepository.findCurrentCoachByAthleteId(athleteId);
    }

    public List<CoachRelationship> getPastCoaches(Long athleteId) {
        return coachRelationshipRepository.findPastCoachesByAthleteId(athleteId);
    }

    public List<CoachRelationship> getCurrentAthletes(Long coachId) {
        return coachRelationshipRepository.findCurrentAthletesByCoachId(coachId);
    }

    @Transactional
    public boolean assignCoach(User athlete, User coach) {
        try {
            // Check if athlete already has an active coach
            Optional<CoachRelationship> existingRelationship =
                coachRelationshipRepository.findCurrentCoachByAthlete(athlete);

            if (existingRelationship.isPresent()) {
                // End the current relationship
                CoachRelationship current = existingRelationship.get();
                current.setActive(false);
                current.setEndDate(LocalDateTime.now());
                coachRelationshipRepository.save(current);
            }

            // Create new relationship
            CoachRelationship newRelationship = new CoachRelationship(athlete, coach);
            coachRelationshipRepository.save(newRelationship);

            return true;
        } catch (Exception e) {
            System.out.println("Error assigning coach: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean endCoachRelationship(Long athleteId) {
        try {
            Optional<CoachRelationship> relationship =
                coachRelationshipRepository.findCurrentCoachByAthleteId(athleteId);

            if (relationship.isPresent()) {
                CoachRelationship rel = relationship.get();
                rel.setActive(false);
                rel.setEndDate(LocalDateTime.now());
                coachRelationshipRepository.save(rel);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error ending coach relationship: " + e.getMessage());
            return false;
        }
    }

    public List<CoachRelationship> getAllRelationshipsByAthlete(User athlete) {
        return coachRelationshipRepository.findAllRelationshipsByAthlete(athlete);
    }

    public List<CoachRelationship> getAllRelationshipsByCoach(User coach) {
        return coachRelationshipRepository.findAllRelationshipsByCoach(coach);
    }
}
