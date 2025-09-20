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
            results.put("users", new ArrayList<>());
            results.put("totalResults", 0);
            return results;
        }

        String searchQuery = query.trim().toLowerCase();

        // Search posts
        List<Map<String, Object>> postResults = searchPosts(searchQuery);

        // Search users (all types)
        List<Map<String, Object>> userResults = searchUsers(searchQuery);

        int totalResults = postResults.size() + userResults.size();

        results.put("posts", postResults);
        results.put("users", userResults);
        results.put("totalResults", totalResults);
        results.put("query", query);

        return results;
    }

    private List<Map<String, Object>> searchPosts(String query) {
        try {
            List<Post> allPosts = postRepository.findAll();
            return allPosts.stream()
                    .filter(post -> matchesPostSearch(post, query))
                    .map(this::createPostSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Error searching posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> searchUsers(String query) {
        List<Map<String, Object>> userResults = new ArrayList<>();

        try {
            // Search athletes
            List<Athlete> athletes = athleteService.getAllAthletes();
            userResults.addAll(athletes.stream()
                    .filter(athlete -> matchesAthleteSearch(athlete, query))
                    .map(this::createAthleteSearchResult)
                    .collect(Collectors.toList()));

            // Search coaches
            List<Coach> coaches = coachService.getAllCoaches();
            userResults.addAll(coaches.stream()
                    .filter(coach -> matchesCoachSearch(coach, query))
                    .map(this::createCoachSearchResult)
                    .collect(Collectors.toList()));

            // Search sponsors
            List<Sponsor> sponsors = sponsorService.getAllSponsors();
            userResults.addAll(sponsors.stream()
                    .filter(sponsor -> matchesSponsorSearch(sponsor, query))
                    .map(this::createSponsorSearchResult)
                    .collect(Collectors.toList()));

        } catch (Exception e) {
            System.out.println("Error searching users: " + e.getMessage());
        }

        return userResults;
    }

    private boolean matchesPostSearch(Post post, String query) {
        String lowerQuery = query.toLowerCase();

        boolean titleMatch = containsIgnoreCase(post.getTitle(), lowerQuery);
        boolean descMatch = containsIgnoreCase(post.getDescription(), lowerQuery);
        boolean typeMatch = containsIgnoreCase(post.getPostType().toString(), lowerQuery);

        boolean userMatch = false;
        if (post.getUser() != null) {
            userMatch = containsIgnoreCase(post.getUser().getFirstName(), lowerQuery) ||
                       containsIgnoreCase(post.getUser().getLastName(), lowerQuery);
        }

        return titleMatch || descMatch || typeMatch || userMatch;
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
        result.put("postType", post.getPostType().toString());
        result.put("createdAt", post.getPosted_at());
        result.put("likesCount", post.getUserLikes().size() != 0 ? post.getUserLikes().size() : 0);
        result.put("type", "POST");

        if (post.getImageUrl() != null) {
            result.put("imageUrl", utilityService.convertFilePathToWebUrl(post.getImageUrl()));
        }

        if (post.getUser() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", post.getUser().getId());
            userInfo.put("firstName", post.getUser().getFirstName());
            userInfo.put("lastName", post.getUser().getLastName());
            userInfo.put("role", post.getUser().getRole().toString());
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

        result.put("id", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("bio", user.getBio());
        result.put("country", user.getCountry());
        result.put("role", "ATHLETE");
        result.put("type", "USER");

        if (user.getProfileImageUrl() != null) {
            result.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }

        // Athlete specific info
        Map<String, Object> professionalInfo = new HashMap<>();
        professionalInfo.put("state", athlete.getState());
        professionalInfo.put("district", athlete.getDistrict());
        professionalInfo.put("height", athlete.getHeight());
        professionalInfo.put("weight", athlete.getWeight());

        professionalInfo.put("disabilityType", athlete.getDisabilityType());
        result.put("professionalInfo", professionalInfo);

        return result;
    }

    private Map<String, Object> createCoachSearchResult(Coach coach) {
        Map<String, Object> result = new HashMap<>();
        User user = coach.getUser();

        result.put("id", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("bio", user.getBio());
        result.put("country", user.getCountry());
        result.put("role", "COACH");
        result.put("type", "USER");

        if (user.getProfileImageUrl() != null) {
            result.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }

        // Coach specific info
        Map<String, Object> professionalInfo = new HashMap<>();
        professionalInfo.put("specialization", coach.getSpecialization());
        professionalInfo.put("experienceYears", coach.getExperienceYears());
        professionalInfo.put("authority", coach.getAuthority());
        result.put("professionalInfo", professionalInfo);

        return result;
    }

    private Map<String, Object> createSponsorSearchResult(Sponsor sponsor) {
        Map<String, Object> result = new HashMap<>();
        User user = sponsor.getUser();

        result.put("id", user.getId());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("email", user.getEmail());
        result.put("bio", user.getBio());
        result.put("country", user.getCountry());
        result.put("role", "SPONSOR");
        result.put("type", "USER");

        if (user.getProfileImageUrl() != null) {
            result.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }

        // Sponsor specific info
        Map<String, Object> professionalInfo = new HashMap<>();
        professionalInfo.put("companyName", sponsor.getCompanyName());
        professionalInfo.put("industry", sponsor.getIndustry());
        professionalInfo.put("website", sponsor.getWebsite());
        result.put("professionalInfo", professionalInfo);

        return result;
    }
}
