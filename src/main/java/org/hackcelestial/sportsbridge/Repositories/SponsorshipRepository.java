package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Models.Sponsorship;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SponsorshipRepository extends JpaRepository<Sponsorship, Long> {

    List<Sponsorship> findByAthleteOrderByCreatedAtDesc(Athlete athlete);

    List<Sponsorship> findBySponsorOrderByCreatedAtDesc(Sponsor sponsor);

    @Query("SELECT s FROM Sponsorship s WHERE s.athlete = :athlete AND s.status = :status")
    List<Sponsorship> findActiveByAthlete(@Param("athlete") Athlete athlete, @Param("status") InvitationStatus status);

    @Query("SELECT s FROM Sponsorship s WHERE s.sponsor = :sponsor AND s.status = :status")
    List<Sponsorship> findActiveBySponsor(@Param("sponsor") Sponsor sponsor, @Param("status") InvitationStatus status);

    List<Sponsorship> findByAthleteAndStatus(Athlete athlete, InvitationStatus status);

    List<Sponsorship> findBySponsorAndStatus(Sponsor sponsor, InvitationStatus status);
}
