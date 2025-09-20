package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private HttpSession session;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAchievement(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "achievementDate", required = false) String achievementDate,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "level", required = false) String level,
            @RequestParam(value = "organization", required = false) String organization,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "certificate", required = false) MultipartFile certificate) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Check if user is athlete or coach
            if (!user.getRole().name().equals("ATHLETE") && !user.getRole().name().equals("COACH")) {
                response.put("success", false);
                response.put("message", "Only athletes and coaches can add achievements");
                return ResponseEntity.status(403).body(response);
            }

            Achievement achievement = new Achievement();
            achievement.setTitle(title);
            achievement.setDescription(description);
            achievement.setCategory(category);
            achievement.setLevel(level);
            achievement.setOrganization(organization);
            achievement.setPosition(position);
            achievement.setUserId(user.getId());

            // Parse achievement date
            if (achievementDate != null && !achievementDate.isEmpty()) {
                try {
                    achievement.setAchievementDate(LocalDate.parse(achievementDate));
                } catch (Exception e) {
                    System.out.println("Error parsing achievement date: " + e.getMessage());
                }
            }

            // Handle certificate upload
            String certificateUrl = null;
            if (certificate != null && !certificate.isEmpty()) {
                // Here you would implement file upload logic
                // For now, we'll just store a placeholder
                certificateUrl = "certificates/" + user.getId() + "_" + System.currentTimeMillis() + "_" + certificate.getOriginalFilename();
                achievement.setCertificateUrl(certificateUrl);
            }

            Achievement savedAchievement = achievementService.saveAchievement(achievement);

            if (savedAchievement != null) {
                response.put("success", true);
                response.put("message", "Achievement added successfully");
                response.put("achievement", createAchievementResponse(savedAchievement));
            } else {
                response.put("success", false);
                response.put("message", "Failed to save achievement");
            }

        } catch (Exception e) {
            System.out.println("Error adding achievement: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error adding achievement");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAchievements(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            List<Achievement> achievements = achievementService.getAchievementsByUserId(userId);
            List<Map<String, Object>> achievementResponses = achievements.stream()
                    .map(this::createAchievementResponse)
                    .toList();

            response.put("success", true);
            response.put("achievements", achievementResponses);

        } catch (Exception e) {
            System.out.println("Error fetching achievements: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching achievements");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyAchievements() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            List<Achievement> achievements = achievementService.getAchievementsByUserId(user.getId());
            List<Map<String, Object>> achievementResponses = achievements.stream()
                    .map(this::createAchievementResponse)
                    .toList();

            response.put("success", true);
            response.put("achievements", achievementResponses);

        } catch (Exception e) {
            System.out.println("Error fetching my achievements: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching achievements");
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{achievementId}")
    public ResponseEntity<Map<String, Object>> deleteAchievement(@PathVariable Long achievementId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Achievement achievement = achievementService.getAchievementById(achievementId);
            if (achievement == null) {
                response.put("success", false);
                response.put("message", "Achievement not found");
                return ResponseEntity.status(404).body(response);
            }

            // Check if user owns this achievement
            if (!achievement.getUserId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "You can only delete your own achievements");
                return ResponseEntity.status(403).body(response);
            }

            boolean deleted = achievementService.deleteAchievement(achievementId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Achievement deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete achievement");
            }

        } catch (Exception e) {
            System.out.println("Error deleting achievement: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error deleting achievement");
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createAchievementResponse(Achievement achievement) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", achievement.getId());
        response.put("title", achievement.getTitle());
        response.put("description", achievement.getDescription());
        response.put("achievementDate", achievement.getAchievementDate());
        response.put("category", achievement.getCategory());
        response.put("level", achievement.getLevel());
        response.put("organization", achievement.getOrganization());
        response.put("position", achievement.getPosition());
        response.put("certificateUrl", achievement.getCertificateUrl());
        response.put("createdAt", achievement.getCreatedAt());
        return response;
    }
}
