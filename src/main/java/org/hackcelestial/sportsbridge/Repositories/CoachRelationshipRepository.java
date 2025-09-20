package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.CoachRelationship;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoachRelationshipRepository extends JpaRepository<CoachRelationship, Long> {

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.athlete = ?1 AND cr.isActive = true")
    Optional<CoachRelationship> findCurrentCoachByAthlete(User athlete);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.athlete.id = ?1 AND cr.isActive = true")
    Optional<CoachRelationship> findCurrentCoachByAthleteId(Long athleteId);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.athlete = ?1 AND cr.isActive = false ORDER BY cr.endDate DESC")
    List<CoachRelationship> findPastCoachesByAthlete(User athlete);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.athlete.id = ?1 AND cr.isActive = false ORDER BY cr.endDate DESC")
    List<CoachRelationship> findPastCoachesByAthleteId(Long athleteId);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.coach = ?1 AND cr.isActive = true ORDER BY cr.startDate DESC")
    List<CoachRelationship> findCurrentAthletesByCoach(User coach);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.coach.id = ?1 AND cr.isActive = true ORDER BY cr.startDate DESC")
    List<CoachRelationship> findCurrentAthletesByCoachId(Long coachId);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.athlete = ?1 ORDER BY cr.startDate DESC")
    List<CoachRelationship> findAllRelationshipsByAthlete(User athlete);

    @Query("SELECT cr FROM CoachRelationship cr WHERE cr.coach = ?1 ORDER BY cr.startDate DESC")
    List<CoachRelationship> findAllRelationshipsByCoach(User coach);
}
