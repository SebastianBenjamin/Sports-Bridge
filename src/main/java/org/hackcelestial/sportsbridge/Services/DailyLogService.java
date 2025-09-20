package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.DailyLog;
import org.hackcelestial.sportsbridge.Models.Sport;
import org.hackcelestial.sportsbridge.Repositories.DailyLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyLogService {

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Transactional
    public boolean saveDailyLog(DailyLog dailyLog) {
        try {
            dailyLog.setCreatedAt(LocalDateTime.now());

            // Calculate and update streak and total duration
            calculateStreakAndTotalDuration(dailyLog);

            DailyLog saved = dailyLogRepository.save(dailyLog);
            System.out.println("Daily log saved successfully with ID: " + saved.getId());
            return saved != null;
        } catch (Exception e) {
            System.out.println("Error saving daily log: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void calculateStreakAndTotalDuration(DailyLog newLog) {
        Athlete athlete = newLog.getAthlete();

        // Get all logs for this athlete ordered by date
        List<DailyLog> allLogs = dailyLogRepository.findByAthleteOrderByCreatedAtDesc(athlete);

        // Calculate total lifetime duration
        int totalDuration = allLogs.stream()
                .mapToInt(DailyLog::getTrainingDurationMinutes)
                .sum() + newLog.getTrainingDurationMinutes();

        newLog.setTotalLifetimeDuration(totalDuration);

        // Calculate current streak
        int currentStreak = calculateCurrentStreak(allLogs, newLog);
        newLog.setCurrentStreak(currentStreak);

        System.out.println("Calculated streak: " + currentStreak + ", Total duration: " + totalDuration);
    }

    private int calculateCurrentStreak(List<DailyLog> existingLogs, DailyLog newLog) {
        // Get all logs including the new one, grouped by date
        List<DailyLog> allLogs = new ArrayList<>(existingLogs);
        allLogs.add(newLog);

        // Group logs by date (convert to LocalDate)
        Map<LocalDate, List<DailyLog>> logsByDate = new HashMap<>();
        for (DailyLog log : allLogs) {
            LocalDate date = log.getCreatedAt().toLocalDate();
            logsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(log);
        }

        // Get unique dates sorted in descending order
        List<LocalDate> uniqueDates = logsByDate.keySet().stream()
                .sorted((a, b) -> b.compareTo(a)) // Sort descending (most recent first)
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));

        if (uniqueDates.isEmpty()) {
            return 0;
        }

        // Start from today and count consecutive days
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        // Count consecutive days starting from today
        while (logsByDate.containsKey(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    public List<DailyLog> getDailyLogsByAthlete(Athlete athlete) {
        try {
            return dailyLogRepository.findByAthleteOrderByCreatedAtDesc(athlete);
        } catch (Exception e) {
            System.out.println("Error fetching daily logs for athlete: " + e.getMessage());
            return List.of();
        }
    }

    public List<DailyLog> getTodaysDailyLogs(Athlete athlete) {
        try {
            return dailyLogRepository.findTodaysLogsByAthlete(athlete);
        } catch (Exception e) {
            System.out.println("Error fetching today's logs: " + e.getMessage());
            return List.of();
        }
    }

    public List<DailyLog> getDailyLogsByDateRange(Athlete athlete, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            return dailyLogRepository.findByAthleteAndDateRange(athlete, startDateTime, endDateTime);
        } catch (Exception e) {
            System.out.println("Error fetching logs by date range: " + e.getMessage());
            return List.of();
        }
    }

    public DailyLog getDailyLogById(Long id) {
        return dailyLogRepository.findById(id).orElse(null);
    }

    @Transactional
    public boolean deleteDailyLog(Long id) {
        try {
            dailyLogRepository.deleteById(id);
            System.out.println("Daily log deleted successfully: " + id);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting daily log: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getAthleteStats(Athlete athlete) {
        List<DailyLog> allLogs = dailyLogRepository.findByAthleteOrderByCreatedAtDesc(athlete);
        Map<String, Object> stats = new HashMap<>();

        if (allLogs.isEmpty()) {
            stats.put("currentStreak", 0);
            stats.put("totalLifetimeDuration", 0);
            stats.put("totalSessions", 0);
            return stats;
        }

        // Calculate current streak using the corrected logic
        int currentStreak = calculateStreakFromExistingLogs(allLogs);
        int totalDuration = allLogs.stream().mapToInt(DailyLog::getTrainingDurationMinutes).sum();

        stats.put("currentStreak", currentStreak);
        stats.put("totalLifetimeDuration", totalDuration);
        stats.put("totalSessions", allLogs.size());

        return stats;
    }

    // Helper method to calculate streak from existing logs
    private int calculateStreakFromExistingLogs(List<DailyLog> logs) {
        if (logs.isEmpty()) {
            return 0;
        }

        // Group logs by date
        Map<LocalDate, List<DailyLog>> logsByDate = new HashMap<>();
        for (DailyLog log : logs) {
            LocalDate date = log.getCreatedAt().toLocalDate();
            logsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(log);
        }

        // Start from today and count consecutive days
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        // Count consecutive days starting from today
        while (logsByDate.containsKey(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    public Map<String, Object> getChartData(Athlete athlete, int days) {
        Map<String, Object> chartData = new HashMap<>();

        // Get logs for the specified number of days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        List<DailyLog> logs = getDailyLogsByDateRange(athlete, startDate, endDate);

        // Prepare data for daily duration chart
        Map<String, Integer> dailyDurations = new HashMap<>();
        Map<String, Integer> trainingTypeCounts = new HashMap<>();

        // Initialize all dates in range with 0 duration
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            dailyDurations.put(date.toString(), 0);
        }

        // Aggregate data from logs
        for (DailyLog log : logs) {
            LocalDate logDate = log.getCreatedAt().toLocalDate();
            String dateStr = logDate.toString();

            // Sum up duration for each day
            dailyDurations.put(dateStr,
                    dailyDurations.getOrDefault(dateStr, 0) + log.getTrainingDurationMinutes());

            // Count training types
            String trainingType = log.getTrainingType();
            trainingTypeCounts.put(trainingType,
                    trainingTypeCounts.getOrDefault(trainingType, 0) + 1);
        }

        // Prepare chart data structures
        List<String> labels = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();

        // Sort dates and prepare data arrays
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            labels.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")));
            durations.add(dailyDurations.get(date.toString()));
        }

        // Prepare training type chart data
        List<String> trainingTypeLabels = new ArrayList<>(trainingTypeCounts.keySet());
        List<Integer> trainingTypeValues = new ArrayList<>();
        for (String type : trainingTypeLabels) {
            trainingTypeValues.add(trainingTypeCounts.get(type));
        }

        // Build response
        chartData.put("durationChart", Map.of(
                "labels", labels,
                "data", durations
        ));

        chartData.put("trainingTypeChart", Map.of(
                "labels", trainingTypeLabels,
                "data", trainingTypeValues
        ));

        return chartData;
    }
}
