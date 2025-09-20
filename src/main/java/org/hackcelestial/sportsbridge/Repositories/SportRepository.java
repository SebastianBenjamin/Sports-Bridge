package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SportRepository extends JpaRepository<Sport, Long> {
}
