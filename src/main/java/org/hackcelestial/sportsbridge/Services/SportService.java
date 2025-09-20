package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Sport;
import org.hackcelestial.sportsbridge.Repositories.SportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SportService {

    @Autowired
    private SportRepository sportRepository;

    public List<Sport> getAllSports() {
        try {
            return sportRepository.findAll();
        } catch (Exception e) {
            System.out.println("Error fetching sports: " + e.getMessage());
            return List.of();
        }
    }

    public Sport getSportById(Long id) {
        return sportRepository.findById(id).orElse(null);
    }

    public boolean saveSport(Sport sport) {
        try {
            sport.setCreatedAt(LocalDateTime.now());
            return sportRepository.save(sport) != null;
        } catch (Exception e) {
            System.out.println("Error saving sport: " + e.getMessage());
            return false;
        }
    }
}
