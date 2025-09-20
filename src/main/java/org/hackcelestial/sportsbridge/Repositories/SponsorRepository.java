package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SponsorRepository extends JpaRepository<Sponsor, Integer> {

    Optional<Sponsor> findById(int id);

    Sponsor findByUser(User user);
}
