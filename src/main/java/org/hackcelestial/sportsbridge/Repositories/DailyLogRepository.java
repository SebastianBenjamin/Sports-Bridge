package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DailyLogRepository extends JpaRepository<DailyLog, Long> {

    @Query("SELECT d FROM DailyLog d WHERE d.athlete = ?1 ORDER BY d.createdAt DESC")
    List<DailyLog> findByAthleteOrderByCreatedAtDesc(Athlete athlete);

    @Query("SELECT d FROM DailyLog d WHERE d.athlete = ?1 AND d.createdAt >= ?2 AND d.createdAt <= ?3 ORDER BY d.createdAt DESC")
    List<DailyLog> findByAthleteAndDateRange(Athlete athlete, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT d FROM DailyLog d WHERE d.athlete = ?1 AND DATE(d.createdAt) = CURRENT_DATE")
    List<DailyLog> findTodaysLogsByAthlete(Athlete athlete);
}
