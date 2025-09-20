package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByUserIdOrderByAchievementDateDesc(Long userId);

    List<Achievement> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT a FROM Achievement a WHERE a.userId = :userId AND a.category = :category ORDER BY a.achievementDate DESC")
    List<Achievement> findByUserIdAndCategoryOrderByAchievementDateDesc(@Param("userId") Long userId, @Param("category") String category);

    @Query("SELECT a FROM Achievement a WHERE a.userId = :userId AND a.level = :level ORDER BY a.achievementDate DESC")
    List<Achievement> findByUserIdAndLevelOrderByAchievementDateDesc(@Param("userId") Long userId, @Param("level") String level);

    @Query("SELECT COUNT(a) FROM Achievement a WHERE a.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
