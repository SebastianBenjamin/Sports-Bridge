package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SponsorRepository extends JpaRepository<Sponsor, Integer> {
    Optional<Sponsor> findByEmail(String email);
    Optional<Sponsor> findById(int id);
    Optional<Sponsor> save(Sponsor sponsor);
}
