package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.DailyLog;
import org.hackcelestial.sportsbridge.Models.Sport;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.AthleteService;
import org.hackcelestial.sportsbridge.Services.DailyLogService;
import org.hackcelestial.sportsbridge.Services.SportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dailylogs")
public class DailyLogController {

    @Autowired
    private DailyLogService dailyLogService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private SportService sportService;

    @Autowired
    private HttpSession session;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createDailyLog(
            @RequestParam("trainingType") String trainingType,
            @RequestParam("trainingDurationMinutes") Integer trainingDurationMinutes,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "sportId", required = false) Long sportId) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Get athlete by user
            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "Only athletes can create daily logs");
                return ResponseEntity.status(403).body(response);
            }

            // Create new daily log
            DailyLog dailyLog = new DailyLog();
            dailyLog.setTrainingType(trainingType);
            dailyLog.setTrainingDurationMinutes(trainingDurationMinutes);
            dailyLog.setNotes(notes);
            dailyLog.setAthlete(athlete);

            // Set sport if provided
            if (sportId != null) {
                Sport sport = sportService.getSportById(sportId);
                if (sport != null) {
                    dailyLog.setSport(sport);
                }
            }

            boolean saved = dailyLogService.saveDailyLog(dailyLog);

            if (saved) {
                response.put("success", true);
                response.put("message", "Daily log created successfully");
                response.put("dailyLog", createDailyLogResponse(dailyLog));
            } else {
                response.put("success", false);
                response.put("message", "Failed to create daily log");
            }

        } catch (Exception e) {
            System.out.println("Error creating daily log: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred while creating the daily log");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-logs")
    public ResponseEntity<Map<String, Object>> getMyDailyLogs() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "Only athletes can view daily logs");
                return ResponseEntity.status(403).body(response);
            }

            List<DailyLog> dailyLogs = dailyLogService.getDailyLogsByAthlete(athlete);
            List<Map<String, Object>> logResponses = dailyLogs.stream()
                    .map(this::createDailyLogResponse)
                    .toList();

            response.put("success", true);
            response.put("dailyLogs", logResponses);
            response.put("message", dailyLogs.isEmpty() ? "No daily logs found" : null);

        } catch (Exception e) {
            System.out.println("Error fetching daily logs: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching daily logs");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodaysLogs() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "Only athletes can view daily logs");
                return ResponseEntity.status(403).body(response);
            }

            List<DailyLog> todaysLogs = dailyLogService.getTodaysDailyLogs(athlete);
            List<Map<String, Object>> logResponses = todaysLogs.stream()
                    .map(this::createDailyLogResponse)
                    .toList();

            response.put("success", true);
            response.put("dailyLogs", logResponses);
            response.put("totalDuration", todaysLogs.stream()
                    .mapToInt(DailyLog::getTrainingDurationMinutes)
                    .sum());

        } catch (Exception e) {
            System.out.println("Error fetching today's logs: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching today's logs");
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Map<String, Object>> deleteDailyLog(@PathVariable Long logId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            DailyLog dailyLog = dailyLogService.getDailyLogById(logId);
            if (dailyLog == null) {
                response.put("success", false);
                response.put("message", "Daily log not found");
                return ResponseEntity.status(404).body(response);
            }

            // Check if user owns the daily log
            if (!dailyLog.getAthlete().getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "You can only delete your own daily logs");
                return ResponseEntity.status(403).body(response);
            }

            boolean deleted = dailyLogService.deleteDailyLog(logId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Daily log deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete daily log");
            }

        } catch (Exception e) {
            System.out.println("Error deleting daily log: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred while deleting the daily log");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAthleteStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "Only athletes can view stats");
                return ResponseEntity.status(403).body(response);
            }

            Map<String, Object> stats = dailyLogService.getAthleteStats(athlete);
            response.put("success", true);
            response.putAll(stats);

        } catch (Exception e) {
            System.out.println("Error fetching athlete stats: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching athlete stats");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<Map<String, Object>> getChartData(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Athlete athlete = athleteService.getAthleteByUser(user);
            if (athlete == null) {
                response.put("success", false);
                response.put("message", "Only athletes can view chart data");
                return ResponseEntity.status(403).body(response);
            }

            Map<String, Object> chartData = dailyLogService.getChartData(athlete, days);
            response.put("success", true);
            response.putAll(chartData);

        } catch (Exception e) {
            System.out.println("Error fetching chart data: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching chart data");
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createDailyLogResponse(DailyLog dailyLog) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", dailyLog.getId());
        response.put("trainingType", dailyLog.getTrainingType());
        response.put("trainingDurationMinutes", dailyLog.getTrainingDurationMinutes());
        response.put("notes", dailyLog.getNotes());
        response.put("createdAt", dailyLog.getCreatedAt());
        response.put("currentStreak", dailyLog.getCurrentStreak());
        response.put("totalLifetimeDuration", dailyLog.getTotalLifetimeDuration());

        if (dailyLog.getSport() != null) {
            Map<String, Object> sportData = new HashMap<>();
            sportData.put("id", dailyLog.getSport().getId());
            sportData.put("name", dailyLog.getSport().getName());
            response.put("sport", sportData);
        }

        return response;
    }
}
