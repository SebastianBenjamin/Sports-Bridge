package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Repositories.AthleteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AthleteService {
    @Autowired
    AthleteRepository athleteRepository;
    public Athlete getAthleteById(int id) {
    return     athleteRepository.findById(id).get();
    }
    public boolean save(Athlete athlete) {
        return athleteRepository.save(athlete).get()!=null;
    }

}
