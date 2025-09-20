package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoachRepository extends JpaRepository<Coach, Integer> {
    @Override
    Optional<Coach> findById(Integer integer);

    Coach findByUser(User user);
}
