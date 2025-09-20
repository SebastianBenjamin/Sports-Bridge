package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GlobalSearchService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private SponsorService sponsorService;

    @Autowired
    private UtilityService utilityService;

    public Map<String, Object> searchAll(String query) {
        Map<String, Object> results = new HashMap<>();

        if (query == null || query.trim().isEmpty()) {
            results.put("posts", new ArrayList<>());
            results.put("athletes", new ArrayList<>());
            results.put("coaches", new ArrayList<>());
            results.put("sponsors", new ArrayList<>());
            results.put("totalResults", 0);
            return results;
        }

        String searchQuery = query.trim().toLowerCase();

        // Search posts
        List<Map<String, Object>> postResults = searchPosts(searchQuery);

        // Search athletes
        List<Map<String, Object>> athleteResults = searchAthletes(searchQuery);

        // Search coaches
        List<Map<String, Object>> coachResults = searchCoaches(searchQuery);

        // Search sponsors
        List<Map<String, Object>> sponsorResults = searchSponsors(searchQuery);

        int totalResults = postResults.size() + athleteResults.size() + coachResults.size() + sponsorResults.size();

        results.put("posts", postResults);
        results.put("athletes", athleteResults);
        results.put("coaches", coachResults);
        results.put("sponsors", sponsorResults);
        results.put("totalResults", totalResults);
        results.put("query", query);

        return results;
    }

    private List<Map<String, Object>> searchPosts(String query) {
        try {
            List<Post> posts = postRepository.findByTitleOrDescriptionOrUserNameContainingIgnoreCase(query);
            return posts.stream().map(this::createPostSearchResult).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error searching posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> searchAthletes(String query) {
        try {
            List<Athlete> athletes = athleteService.getAllAthletes();
            return athletes.stream()
                    .filter(athlete -> matchesAthleteSearch(athlete, query))
                    .map(this::createAthleteSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error searching athletes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> searchCoaches(String query) {
        try {
            List<Coach> coaches = coachService.getAllCoaches();
            return coaches.stream()
                    .filter(coach -> matchesCoachSearch(coach, query))
                    .map(this::createCoachSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error searching coaches: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> searchSponsors(String query) {
        try {
            List<Sponsor> sponsors = sponsorService.getAllSponsors();
            return sponsors.stream()
                    .filter(sponsor -> matchesSponsorSearch(sponsor, query))
                    .map(this::createSponsorSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error searching sponsors: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean matchesAthleteSearch(Athlete athlete, String query) {
        if (athlete.getUser() == null) return false;

        User user = athlete.getUser();
        String lowerQuery = query.toLowerCase();

        return containsIgnoreCase(user.getFirstName(), lowerQuery) ||
               containsIgnoreCase(user.getLastName(), lowerQuery) ||
               containsIgnoreCase(user.getEmail(), lowerQuery) ||
               containsIgnoreCase(user.getBio(), lowerQuery) ||
               containsIgnoreCase(user.getCountry(), lowerQuery) ||
               containsIgnoreCase(athlete.getState(), lowerQuery) ||
               containsIgnoreCase(athlete.getDistrict(), lowerQuery) ||
               containsIgnoreCase(athlete.getDisabilityType(), lowerQuery);
    }

    private boolean matchesCoachSearch(Coach coach, String query) {
        if (coach.getUser() == null) return false;

        User user = coach.getUser();
        String lowerQuery = query.toLowerCase();

        return containsIgnoreCase(user.getFirstName(), lowerQuery) ||
               containsIgnoreCase(user.getLastName(), lowerQuery) ||
               containsIgnoreCase(user.getEmail(), lowerQuery) ||
               containsIgnoreCase(user.getBio(), lowerQuery) ||
               containsIgnoreCase(user.getCountry(), lowerQuery) ||
               containsIgnoreCase(coach.getSpecialization(), lowerQuery) ||
               containsIgnoreCase(coach.getAuthority(), lowerQuery);
    }

    private boolean matchesSponsorSearch(Sponsor sponsor, String query) {
        if (sponsor.getUser() == null) return false;

        User user = sponsor.getUser();
        String lowerQuery = query.toLowerCase();

        return containsIgnoreCase(user.getFirstName(), lowerQuery) ||
               containsIgnoreCase(user.getLastName(), lowerQuery) ||
               containsIgnoreCase(user.getEmail(), lowerQuery) ||
               containsIgnoreCase(user.getBio(), lowerQuery) ||
               containsIgnoreCase(user.getCountry(), lowerQuery) ||
               containsIgnoreCase(sponsor.getCompanyName(), lowerQuery) ||
               containsIgnoreCase(sponsor.getIndustry(), lowerQuery) ||
               containsIgnoreCase(sponsor.getWebsite(), lowerQuery);
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase().contains(query);
    }

    private Map<String, Object> createPostSearchResult(Post post) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", post.getId());
        result.put("title", post.getTitle());
        result.put("description", post.getDescription());
        result.put("postType", post.getPostType());
        result.put("postedAt", post.getPosted_at());
        result.put("type", "POST");

        if (post.getImageUrl() != null) {
            result.put("imageUrl", utilityService.convertFilePathToWebUrl(post.getImageUrl()));
        }

        if (post.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", post.getUser().getId());
            userInfo.put("firstName", post.getUser().getFirstName());
            userInfo.put("lastName", post.getUser().getLastName());
            userInfo.put("role", post.getUser().getRole());
            if (post.getUser().getProfileImageUrl() != null) {
                userInfo.put("profileImageUrl", utilityService.convertFilePathToWebUrl(post.getUser().getProfileImageUrl()));
            }
            result.put("user", userInfo);
        }

        return result;
    }

    private Map<String, Object> createAthleteSearchResult(Athlete athlete) {
        Map<String, Object> result = new HashMap<>();
        User user = athlete.getUser();

        result.put("id", athlete.getId());
        result.put("userId", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("bio", user.getBio());
        result.put("country", user.getCountry());
        result.put("sport", extractSportFromBio(user.getBio())); // Extract sport from bio
        result.put("height", athlete.getHeight());
        result.put("weight", athlete.getWeight());
        result.put("state", athlete.getState());
        result.put("district", athlete.getDistrict());
        result.put("achievements", ""); // Placeholder for future implementation
        result.put("coachingHistory", buildCoachingHistory(athlete)); // Build from current/previous coaches
        result.put("type", "ATHLETE");
        result.put("role", "ATHLETE");

        if (user.getProfileImageUrl() != null) {
            result.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }

        return result;
    }

    private Map<String, Object> createCoachSearchResult(Coach coach) {
        Map<String, Object> result = new HashMap<>();
        User user = coach.getUser();

        result.put("id", coach.getId());
        result.put("userId", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("bio", user.getBio());
        result.put("country", user.getCountry());
        result.put("specialization", coach.getSpecialization());
        result.put("experience", coach.getExperienceYears() != null ? coach.getExperienceYears() + " years" : "");
        result.put("authority", coach.getAuthority());
        result.put("certifications", ""); // Placeholder for future implementation
        result.put("type", "COACH");
        result.put("role", "COACH");

        if (user.getProfileImageUrl() != null) {
            result.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }

        return result;
    }

    private Map<String, Object> createSponsorSearchResult(Sponsor sponsor) {
        Map<String, Object> result = new HashMap<>();
        User user = sponsor.getUser();

        result.put("id", sponsor.getId());
        result.put("userId", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("bio", user.getBio());
        result.put("country", user.getCountry());
        result.put("companyName", sponsor.getCompanyName());
        result.put("industry", sponsor.getIndustry());
        result.put("website", sponsor.getWebsite());
        result.put("budgetRange", sponsor.getBudgetRange());
        result.put("type", "SPONSOR");
        result.put("role", "SPONSOR");

        if (user.getProfileImageUrl() != null) {
            result.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }

        return result;
    }

    private String extractSportFromBio(String bio) {
        // Extract sport information from bio - placeholder implementation
        if (bio == null) return "";
        // This could be enhanced to parse sport information from bio
        return bio.length() > 50 ? bio.substring(0, 50) + "..." : bio;
    }

    private String buildCoachingHistory(Athlete athlete) {
        StringBuilder history = new StringBuilder();

        if (athlete.getCurrentCoach() != null && athlete.getCurrentCoach().getUser() != null) {
            history.append("Current Coach: ")
                   .append(athlete.getCurrentCoach().getUser().getFirstName())
                   .append(" ")
                   .append(athlete.getCurrentCoach().getUser().getLastName());
        }

        if (athlete.getPreviousCoaches() != null && !athlete.getPreviousCoaches().isEmpty()) {
            if (history.length() > 0) history.append("; ");
            history.append("Previous Coaches: ");
            for (int i = 0; i < Math.min(2, athlete.getPreviousCoaches().size()); i++) {
                Coach prevCoach = athlete.getPreviousCoaches().get(i);
                if (prevCoach.getUser() != null) {
                    if (i > 0) history.append(", ");
                    history.append(prevCoach.getUser().getFirstName())
                           .append(" ")
                           .append(prevCoach.getUser().getLastName());
                }
            }
        }

        return history.toString();
    }
}
