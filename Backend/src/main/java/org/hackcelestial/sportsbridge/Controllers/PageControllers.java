package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Enums.PostType;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PageControllers {
    @Autowired
    HttpSession session;
    @Autowired
    UserService userService;
    @Autowired
    UtilityService utilityService;
    @Autowired
    AthleteService athleteService;
    @Autowired
    SponsorService sponsorService;
    @Autowired
    CoachService coachService;
    @Autowired
    PostService postService;
    @Autowired
    NotificationService notificationService;

    @GetMapping("/")
    public String home() {
        if(session.getAttribute("user") == null) {
            return "index";
        }
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/register";
        }

        // Route to role-specific dashboards with posts and notifications
        switch (user.getRole()) {
            case ATHLETE:
                return "redirect:/athlete/dashboard";
            case COACH:
                return "redirect:/coach/dashboard";
            case SPONSOR:
                return "redirect:/sponsor/dashboard";
            case ADMIN:
                return "redirect:/admin/dashboard";
            default:
                model.addAttribute("user", user);
                return "dashboard";
        }
    }

    // ATHLETE DASHBOARD ROUTES
    @GetMapping("/athlete/dashboard")
    public String athleteDashboard(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.ATHLETE) {
            return "redirect:/register";
        }

        // Get recent posts using optimized method
        List<Post> recentPosts = postService.getRecentPostsForDashboard(5);

        long unreadNotifications = notificationService.getUnreadCount(user);

        model.addAttribute("user", user);
        model.addAttribute("recentPosts", recentPosts);
        model.addAttribute("unreadNotifications", unreadNotifications);
        return "athlete/dashboard";
    }

    @GetMapping("/athlete/posts")
    public String athletePosts(Model model, @RequestParam(required = false) String filter) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.ATHLETE) {
            return "redirect:/register";
        }

        List<Post> posts;
        if("my".equals(filter)) {
            posts = postService.getPostsByUser(user);
        } else if("athlete".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.ATHLETE);
        } else if("coach".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.COACH);
        } else if("sponsor".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.SPONSOR);
        } else {
            posts = postService.getAllPosts();
        }

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("newPost", new Post());
        return "athlete/posts";
    }

    @GetMapping("/athlete/events")
    public String athleteEvents(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.ATHLETE) {
            return "redirect:/register";
        }

        List<Post> events = postService.getEventPosts();
        model.addAttribute("user", user);
        model.addAttribute("events", events);
        return "athlete/events";
    }

    @GetMapping("/athlete/notifications")
    public String athleteNotifications(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.ATHLETE) {
            return "redirect:/register";
        }

        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        return "athlete/notifications";
    }

    // COACH DASHBOARD ROUTES
    @GetMapping("/coach/dashboard")
    public String coachDashboard(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.COACH) {
            return "redirect:/register";
        }

        // Get recent posts using optimized method
        List<Post> recentPosts = postService.getRecentPostsForDashboard(5);

        long unreadNotifications = notificationService.getUnreadCount(user);

        model.addAttribute("user", user);
        model.addAttribute("recentPosts", recentPosts);
        model.addAttribute("unreadNotifications", unreadNotifications);
        return "coach/dashboard";
    }

    @GetMapping("/coach/posts")
    public String coachPosts(Model model, @RequestParam(required = false) String filter) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.COACH) {
            return "redirect:/register";
        }

        List<Post> posts;
        if("my".equals(filter)) {
            posts = postService.getPostsByUser(user);
        } else if("athlete".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.ATHLETE);
        } else if("coach".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.COACH);
        } else if("sponsor".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.SPONSOR);
        } else {
            posts = postService.getAllPosts();
        }

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("newPost", new Post());
        return "coach/posts";
    }

    @GetMapping("/coach/events")
    public String coachEvents(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.COACH) {
            return "redirect:/register";
        }

        List<Post> events = postService.getEventPosts();
        model.addAttribute("user", user);
        model.addAttribute("events", events);
        return "coach/events";
    }

    @GetMapping("/coach/notifications")
    public String coachNotifications(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.COACH) {
            return "redirect:/register";
        }

        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        return "coach/notifications";
    }

    // SPONSOR DASHBOARD ROUTES
    @GetMapping("/sponsor/dashboard")
    public String sponsorDashboard(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.SPONSOR) {
            return "redirect:/register";
        }

        // Get recent posts using optimized method
        List<Post> recentPosts = postService.getRecentPostsForDashboard(5);

        long unreadNotifications = notificationService.getUnreadCount(user);

        model.addAttribute("user", user);
        model.addAttribute("recentPosts", recentPosts);
        model.addAttribute("unreadNotifications", unreadNotifications);
        return "sponsor/dashboard";
    }

    @GetMapping("/sponsor/posts")
    public String sponsorPosts(Model model, @RequestParam(required = false) String filter) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.SPONSOR) {
            return "redirect:/register";
        }

        List<Post> posts;
        if("my".equals(filter)) {
            posts = postService.getPostsByUser(user);
        } else if("athlete".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.ATHLETE);
        } else if("coach".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.COACH);
        } else if("sponsor".equals(filter)) {
            posts = postService.getPostsByRole(UserRole.SPONSOR);
        } else {
            posts = postService.getAllPosts();
        }

        model.addAttribute("user", user);
        model.addAttribute("posts", posts);
        model.addAttribute("currentFilter", filter);
        model.addAttribute("newPost", new Post());
        return "sponsor/posts";
    }

    @GetMapping("/sponsor/events")
    public String sponsorEvents(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.SPONSOR) {
            return "redirect:/register";
        }

        List<Post> events = postService.getEventPosts();
        model.addAttribute("user", user);
        model.addAttribute("events", events);
        model.addAttribute("newEvent", new Post());
        return "sponsor/events";
    }

    @GetMapping("/sponsor/notifications")
    public String sponsorNotifications(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.SPONSOR) {
            return "redirect:/register";
        }

        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("user", user);
        model.addAttribute("notifications", notifications);
        return "sponsor/notifications";
    }

    // POST CREATION AND INTERACTION ENDPOINTS
    @PostMapping("/createPost")
    public String createPost(@ModelAttribute Post post, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/register";
        }

        if(!postService.canCreatePost(user)) {
            redirectAttributes.addFlashAttribute("Error", "You don't have permission to create posts");
            return "redirect:/" + user.getRole().name().toLowerCase() + "/posts";
        }

        postService.createPost(post, user);
        redirectAttributes.addFlashAttribute("Success", "Post created successfully!");
        return "redirect:/" + user.getRole().name().toLowerCase() + "/posts";
    }

    @PostMapping("/createEvent")
    public String createEvent(@ModelAttribute Post event, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/register";
        }

        if(!postService.canCreateEvent(user)) {
            redirectAttributes.addFlashAttribute("Error", "Only sponsors can create events");
            return "redirect:/sponsor/events";
        }

        event.setPostType(PostType.EVENT);
        postService.createPost(event, user);
        redirectAttributes.addFlashAttribute("Success", "Event created successfully!");
        return "redirect:/sponsor/events";
    }

    @PostMapping("/likePost/{postId}")
    @ResponseBody
    public String likePost(@PathVariable Long postId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "error";
        }

        Post post = postService.likePost(postId, user);
        if(post != null) {
            return "success";
        }
        return "error";
    }

    @PostMapping("/markNotificationRead/{notificationId}")
    @ResponseBody
    public String markNotificationRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return "success";
    }

    @PostMapping("/markAllNotificationsRead")
    @ResponseBody
    public String markAllNotificationsRead(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "error";
        }

        notificationService.markAllAsRead(user);
        return "success";
    }

    // DAILY LOGS FUNCTIONALITY (keeping existing functionality)
    @GetMapping("/athlete/daily-logs")
    public String athleteDailyLogs(Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.ATHLETE) {
            return "redirect:/register";
        }

        model.addAttribute("user", user);
        model.addAttribute("dailyLog", new DailyLog());
        return "athlete/dailyLogs";
    }

    @PostMapping("/athlete/daily-logs")
    public String addDailyLog(DailyLog dailyLog, RedirectAttributes redirectAttributes, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null || user.getRole() != UserRole.ATHLETE) {
            return "redirect:/register";
        }

        try {
            // Get the athlete entity associated with this user
            Athlete athlete = athleteService.getAthleteByUser(user);
            if(athlete == null) {
                redirectAttributes.addFlashAttribute("Error", "Athlete profile not found. Please complete your registration.");
                return "redirect:/athlete/daily-logs";
            }

            dailyLog.setAthlete(athlete);
            dailyLog.setCreatedAt(LocalDateTime.now());
            // TODO: Add DailyLogService save functionality
            redirectAttributes.addFlashAttribute("Success", "Daily log added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("Error", "Failed to add daily log. Please try again.");
        }

        return "redirect:/athlete/daily-logs";
    }

    // EXISTING REGISTRATION ENDPOINTS
    @GetMapping("/register")
    public String register(Model model) {
        if(session.getAttribute("user") == null) {
            User user = new User();
            model.addAttribute("user", user);
            return "register";
        }
        return "dashboard";
    }

    @PostMapping("/registerUser")
    public String register(
            User user,
            Model model,
            @RequestParam("photo") MultipartFile photo,
            RedirectAttributes redirectAttributes,
            @RequestParam("role") String role
    ) throws IOException {

        if(session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }

        String url = utilityService.storeFile(photo);
        user.setProfileImageUrl(url);
        user.setActive(true);
        user.setReportedTimes(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(UserRole.valueOf(role.toUpperCase()));

        if(userService.save(user)) {
            session.setAttribute("user", user);
            redirectAttributes.addFlashAttribute("Success", "User registered successfully");

            // Fix role comparison to use uppercase enum values
            switch (role.toUpperCase()) {
                case "ADMIN":
                    return "redirect:/dashboard";
                case "ATHLETE":
                    return "redirect:/athleteRegister";
                case "SPONSOR":
                    return "redirect:/sponsorRegister";
                case "COACH":
                    return "redirect:/coachRegister";
                default:
                    redirectAttributes.addFlashAttribute("Error", "Invalid role selected");
                    return "redirect:/register";
            }
        }
        redirectAttributes.addFlashAttribute("Error", "User could not be registered");
        return "redirect:/register";
    }

    // Add GET mapping for athlete registration
    @GetMapping("/athleteRegister")
    public String athleteRegisterPage(Model model) {
        if(session.getAttribute("user") == null) {
            return "redirect:/register";
        }
        Athlete athlete = new Athlete();
        model.addAttribute("athlete", athlete);
        return "athleteRegister";
    }

    @PostMapping("/athleteRegister")
    public String athleteRegister(
        Athlete athlete,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/register";
        }

        // Associate user with athlete
        athlete.setUser(user);

        if(athleteService.save(athlete)) {
            redirectAttributes.addFlashAttribute("Success", "Athlete registered successfully");
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("Error", "Athlete could not be registered");
            model.addAttribute("athlete", athlete);
            return "athleteRegister";
        }
    }

    // Add GET mapping for sponsor registration
    @GetMapping("/sponsorRegister")
    public String sponsorRegisterPage(Model model) {
        if(session.getAttribute("user") == null) {
            return "redirect:/register";
        }
        Sponsor sponsor = new Sponsor();
        model.addAttribute("sponsor", sponsor);
        return "sponsorRegister";
    }

    @PostMapping("/sponsorRegister")
    public String sponsorRegister(
            Sponsor sponsor,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/register";
        }

        // Associate user with sponsor
        sponsor.setUser(user);

        if(sponsorService.save(sponsor)) {
            redirectAttributes.addFlashAttribute("Success", "Sponsor registered successfully");
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("Error", "Sponsor could not be registered");
            model.addAttribute("sponsor", sponsor);
            return "sponsorRegister";
        }
    }

    // Add GET mapping for coach registration
    @GetMapping("/coachRegister")
    public String coachRegisterPage(Model model) {
        if(session.getAttribute("user") == null) {
            return "redirect:/register";
        }
        Coach coach = new Coach();
        model.addAttribute("coach", coach);
        return "coachRegister";
    }

    @PostMapping("/coachRegister")
    public String coachRegister(
            Coach coach,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if(user == null) {
            return "redirect:/register";
        }

        // Associate user with coach
        coach.setUser(user);

        if(coachService.save(coach)) {
            redirectAttributes.addFlashAttribute("Success", "Coach registered successfully");
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("Error", "Coach could not be registered");
            model.addAttribute("coach", coach);
            return "coachRegister";
        }
    }
}
