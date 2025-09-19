package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AthleteRepository extends JpaRepository<Athlete, Integer> {
    @Override
    Optional<Athlete> findById(Integer integer);
    Optional<Athlete> findByName(String name);
    Optional<Athlete> save(Athlete athlete);



}
