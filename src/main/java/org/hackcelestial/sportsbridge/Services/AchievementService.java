package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Achievement;
import org.hackcelestial.sportsbridge.Repositories.AchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AchievementService {

    @Autowired
    private AchievementRepository achievementRepository;

    public Achievement saveAchievement(Achievement achievement) {
        try {
            return achievementRepository.save(achievement);
        } catch (Exception e) {
            System.out.println("Error saving achievement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Achievement> getAchievementsByUserId(Long userId) {
        try {
            return achievementRepository.findByUserIdOrderByAchievementDateDesc(userId);
        } catch (Exception e) {
            System.out.println("Error fetching achievements for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public Achievement getAchievementById(Long achievementId) {
        try {
            Optional<Achievement> achievement = achievementRepository.findById(achievementId);
            return achievement.orElse(null);
        } catch (Exception e) {
            System.out.println("Error fetching achievement " + achievementId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteAchievement(Long achievementId) {
        try {
            if (achievementRepository.existsById(achievementId)) {
                achievementRepository.deleteById(achievementId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error deleting achievement " + achievementId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Achievement> getAchievementsByCategory(Long userId, String category) {
        try {
            return achievementRepository.findByUserIdAndCategoryOrderByAchievementDateDesc(userId, category);
        } catch (Exception e) {
            System.out.println("Error fetching achievements by category: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Achievement> getAchievementsByLevel(Long userId, String level) {
        try {
            return achievementRepository.findByUserIdAndLevelOrderByAchievementDateDesc(userId, level);
        } catch (Exception e) {
            System.out.println("Error fetching achievements by level: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public Long getAchievementsCount(Long userId) {
        try {
            return achievementRepository.countByUserId(userId);
        } catch (Exception e) {
            System.out.println("Error counting achievements: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public Achievement updateAchievement(Achievement achievement) {
        try {
            if (achievementRepository.existsById(achievement.getId())) {
                return achievementRepository.save(achievement);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error updating achievement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
