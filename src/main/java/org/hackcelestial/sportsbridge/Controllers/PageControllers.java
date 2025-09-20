package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class PageControllers {
    @Autowired
    UserService userService;
    @Autowired
    UtilityService utilityService;
    @Autowired
    HttpSession session;
    @Autowired
    AthleteService athleteService;
    @Autowired
    CoachService coachService;
    @Autowired
    SponsorService sponsorService;
    @Autowired
    PostService postService;
    @Autowired
    InvitationService invitationService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email,
                           @RequestParam("password") String password,
                           RedirectAttributes redirectAttributes) {
        try {
            // Check if user exists with the given email
            if (!userService.userExists(email)) {
                redirectAttributes.addFlashAttribute("errorMessage", "No account found with this email address.");
                return "redirect:/login";
            }

            // Get user by email
            User user = userService.getUserByEmail(email);

            // Verify password (assuming password is stored as plain text for now)
            if (user != null && user.getPassword().equals(password)) {
                // Set user in session
                session.setAttribute("user", user);
                session.setAttribute("role", user.getRole());

                System.out.println("User logged in successfully: " + user.getEmail() + ", Role: " + user.getRole());

                // Redirect to appropriate dashboard based on user role
                switch (user.getRole()) {
                    case ATHLETE -> {
                        redirectAttributes.addFlashAttribute("message", "Welcome back, " + user.getFirstName() + "!");
                        return "redirect:/athlete/dashboard";
                    }
                    case COACH -> {
                        redirectAttributes.addFlashAttribute("message", "Welcome back, " + user.getFirstName() + "!");
                        return "redirect:/coach/dashboard";
                    }
                    case SPONSOR -> {
                        redirectAttributes.addFlashAttribute("message", "Welcome back, " + user.getFirstName() + "!");
                        return "redirect:/sponsor/dashboard";
                    }
                    default -> {
                        redirectAttributes.addFlashAttribute("errorMessage", "Unknown user role.");
                        return "redirect:/login";
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Invalid email or password.");
                return "redirect:/login";
            }

        } catch (Exception e) {
            System.out.println("Exception during login: " + e.getMessage());
            System.err.println("Login error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred during login. Please try again.");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(RedirectAttributes redirectAttributes) {
        try {
            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "You have been logged out successfully.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during logout.");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes, @RequestParam("photo") MultipartFile photo, Model model) {
        try {
            if (user != null && !userService.userExists(user.getEmail())) {
                String url = utilityService.storeFile(photo);
                user.setProfileImageUrl(url);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                user.setReportedTimes(0);
                if (userService.save(user)) {
                    redirectAttributes.addFlashAttribute("message", "User registered successfully");
                    session.setAttribute("user", user);
                    session.setAttribute("role", user.getRole());
                    model.addAttribute("user", user);
                    String entitytype = user.getRole().name().toLowerCase();
                    model.addAttribute("entitytype", entitytype);
                    switch (user.getRole()) {
                        case ATHLETE -> {
                            Athlete athlete = new Athlete();
                            athlete.setUser(user);
                            model.addAttribute("entity", athlete);
                            return "roleRegister";
                        }
                        case COACH -> {
                            Coach coach = new Coach();
                            coach.setUser(user);
                            model.addAttribute("entity", coach);
                            return "roleRegister";
                        }
                        case SPONSOR -> {
                            Sponsor sponsor = new Sponsor();
                            sponsor.setUser(user);
                            model.addAttribute("entity", sponsor);
                            return "roleRegister";
                        }
                    }
                    return "login";
                }
            }
            return "redirect:/register";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error registering user. Please try again.");
            return "redirect:/register";
        }
    }

    @GetMapping("/roleRegister")
    public String showRoleRegister(Model model) {
        User user = (User) session.getAttribute("user");
        UserRole role = (UserRole) session.getAttribute("role");

        if (user == null || role == null) {
            return "redirect:/register";
        }

        model.addAttribute("user", user);
        String entitytype = role.name().toLowerCase();
        model.addAttribute("entitytype", entitytype);

        switch (role) {
            case ATHLETE -> {
                Athlete athlete = new Athlete();
                athlete.setUser(user);
                model.addAttribute("entity", athlete);
            }
            case COACH -> {
                Coach coach = new Coach();
                coach.setUser(user);
                model.addAttribute("entity", coach);
            }
            case SPONSOR -> {
                Sponsor sponsor = new Sponsor();
                sponsor.setUser(user);
                model.addAttribute("entity", sponsor);
            }
        }

        return "roleRegister";
    }

    @PostMapping("/roleRegister")
    public String saveRoleDetails(@ModelAttribute("entity") Object entity,
                                  @RequestParam(value = "entitytype", required = false) String entitytype,
                                  RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            UserRole role = (UserRole) session.getAttribute("role");

            System.out.println("POST roleRegister called - User: " + (user != null ? user.getEmail() : "null"));
            System.out.println("Role: " + role);
            System.out.println("Entity type param: " + entitytype);
            System.out.println("Entity class: " + (entity != null ? entity.getClass().getName() : "null"));

            if (user == null || role == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please register again.");
                return "redirect:/register";
            }

            // If entitytype is null, get it from role
            if (entitytype == null) {
                entitytype = role.name().toLowerCase();
            }

            switch (role) {
                case ATHLETE -> {
                    Athlete athlete;
                    if (entity instanceof Athlete) {
                        athlete = (Athlete) entity;
                    } else {
                        System.out.println("Entity is not Athlete type, creating new one");
                        athlete = new Athlete();
                        // Manual binding might be needed here
                    }
                    athlete.setUser(user);
                    System.out.println("Saving athlete: height=" + athlete.getHeight() + ", weight=" + athlete.getWeight() + ", state=" + athlete.getState());
                    boolean saved = athleteService.save(athlete);
                    System.out.println("Athlete saved: " + saved);
                    if (saved) {
                        redirectAttributes.addFlashAttribute("message", "Athlete profile created successfully");
                        return "redirect:/athlete/dashboard";
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "Failed to save athlete profile");
                        return "redirect:/roleRegister";
                    }
                }
                case COACH -> {
                    Coach coach;
                    if (entity instanceof Coach) {
                        coach = (Coach) entity;
                    } else {
                        System.out.println("Entity is not Coach type, creating new one");
                        coach = new Coach();
                    }
                    coach.setUser(user);
                    System.out.println("Saving coach: specialization=" + coach.getSpecialization() + ", authority=" + coach.getAuthority());
                    boolean saved = coachService.save(coach);
                    System.out.println("Coach saved: " + saved);
                    if (saved) {
                        redirectAttributes.addFlashAttribute("message", "Coach profile created successfully");
                        return "redirect:/coach/dashboard";
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "Failed to save coach profile");
                        return "redirect:/roleRegister";
                    }
                }
                case SPONSOR -> {
                    Sponsor sponsor;
                    if (entity instanceof Sponsor) {
                        sponsor = (Sponsor) entity;
                    } else {
                        System.out.println("Entity is not Sponsor type, creating new one");
                        sponsor = new Sponsor();
                    }
                    sponsor.setUser(user);
                    System.out.println("Saving sponsor: companyName=" + sponsor.getCompanyName() + ", industry=" + sponsor.getIndustry());
                    boolean saved = sponsorService.save(sponsor);
                    System.out.println("Sponsor saved: " + saved);
                    if (saved) {
                        redirectAttributes.addFlashAttribute("message", "Sponsor profile created successfully");
                        return "redirect:/sponsor/dashboard";
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessage", "Failed to save sponsor profile");
                        return "redirect:/roleRegister";
                    }
                }
            }

            redirectAttributes.addFlashAttribute("errorMessage", "Unknown role type");
            return "redirect:/roleRegister";

        } catch (Exception e) {
            System.out.println("Exception in saveRoleDetails: " + e.getMessage());
            System.err.println("Role registration error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating profile: " + e.getMessage());
            return "redirect:/roleRegister";
        }
    }

    @PostMapping("/roleRegister/athlete")
    public String saveAthleteDetails(@ModelAttribute Athlete athlete, RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please register again.");
                return "redirect:/register";
            }

            athlete.setUser(user);
            System.out.println("Saving athlete: height=" + athlete.getHeight() + ", weight=" + athlete.getWeight() + ", state=" + athlete.getState());

            boolean saved = athleteService.save(athlete);
            System.out.println("Athlete saved: " + saved);

            if (saved) {
                redirectAttributes.addFlashAttribute("message", "Athlete profile created successfully");
                return "redirect:/athlete/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to save athlete profile");
                return "redirect:/roleRegister";
            }
        } catch (Exception e) {
            System.out.println("Exception saving athlete: " + e.getMessage());
            System.err.println("Athlete save error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating athlete profile: " + e.getMessage());
            return "redirect:/roleRegister";
        }
    }

    @PostMapping("/roleRegister/coach")
    public String saveCoachDetails(@ModelAttribute Coach coach, RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please register again.");
                return "redirect:/register";
            }

            coach.setUser(user);
            System.out.println("Saving coach: specialization=" + coach.getSpecialization() + ", authority=" + coach.getAuthority());

            boolean saved = coachService.save(coach);
            System.out.println("Coach saved: " + saved);

            if (saved) {
                redirectAttributes.addFlashAttribute("message", "Coach profile created successfully");
                return "redirect:/coach/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to save coach profile");
                return "redirect:/roleRegister";
            }
        } catch (Exception e) {
            System.out.println("Exception saving coach: " + e.getMessage());
            System.err.println("Coach save error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating coach profile: " + e.getMessage());
            return "redirect:/roleRegister";
        }
    }

    @PostMapping("/roleRegister/sponsor")
    public String saveSponsorDetails(@ModelAttribute Sponsor sponsor, RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please register again.");
                return "redirect:/register";
            }

            sponsor.setUser(user);
            System.out.println("Saving sponsor: companyName=" + sponsor.getCompanyName() + ", industry=" + sponsor.getIndustry());

            boolean saved = sponsorService.save(sponsor);
            System.out.println("Sponsor saved: " + saved);

            if (saved) {
                redirectAttributes.addFlashAttribute("message", "Sponsor profile created successfully");
                return "redirect:/sponsor/dashboard";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to save sponsor profile");
                return "redirect:/roleRegister";
            }
        } catch (Exception e) {
            System.out.println("Exception saving sponsor: " + e.getMessage());
            System.err.println("Sponsor profile visit error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating sponsor profile: " + e.getMessage());
            return "redirect:/roleRegister";
        }
    }

    // Role-based dashboard routes
    @GetMapping("/athlete/dashboard")
    public String athleteDashboard(Model model) {
        return getAthletePageWithTab(model, "explore");
    }

    @GetMapping("/athlete/explore")
    public String athleteExplore(Model model) {
        return getAthletePageWithTab(model, "explore");
    }

    @GetMapping("/athlete/dailylogs")
    public String athleteDailyLogs(Model model) {
        return getAthletePageWithTab(model, "dailylogs");
    }

    @GetMapping("/athlete/invitations")
    public String athleteInvitations(Model model) {
        return getAthletePageWithTab(model, "invitations");
    }

    @GetMapping("/athlete/profile")
    public String athleteProfile(Model model) {
        return getAthletePageWithTab(model, "profile");
    }

    @GetMapping("/athlete/mycoach")
    public String athleteMyCoach(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("role", user.getRole().toString().toLowerCase());
        return "mycoach";
    }

    // Coach routes
    @GetMapping("/coach/dashboard")
    public String coachDashboard(Model model) {
        return getCoachPageWithTab(model, "explore");
    }

    @GetMapping("/coach/explore")
    public String coachExplore(Model model) {
        return getCoachPageWithTab(model, "explore");
    }

    @GetMapping("/coach/invitations")
    public String coachInvitations(Model model) {
        return getCoachPageWithTab(model, "invitations");
    }

    @GetMapping("/coach/profile")
    public String coachProfile(Model model) {
        return getCoachPageWithTab(model, "profile");
    }

    @GetMapping("/coach/myathletes")
    public String coachMyAthletes(Model model) {
        return getCoachPageWithTab(model, "myathletes");
    }

    // Sponsor routes
    @GetMapping("/sponsor/dashboard")
    public String sponsorDashboard(Model model) {
        return getSponsorPageWithTab(model, "explore");
    }

    @GetMapping("/sponsor/explore")
    public String sponsorExplore(Model model) {
        return getSponsorPageWithTab(model, "explore");
    }

    @GetMapping("/sponsor/invitations")
    public String sponsorInvitations(Model model) {
        return getSponsorPageWithTab(model, "invitations");
    }

    @GetMapping("/sponsor/profile")
    public String sponsorProfile(Model model) {
        return getSponsorPageWithTab(model, "profile");
    }

    @GetMapping("/sponsor/sponsorships")
    public String sponsorSponsorships(Model model) {
        return getSponsorPageWithTab(model, "sponsorships");
    }

    // Profile visiting endpoints
    @GetMapping("/athlete/profile/{userId}")
    public String visitAthleteProfile(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view profiles.");
                return "redirect:/login";
            }

            User targetUser = userService.getUserById(userId);
            if (targetUser == null || !targetUser.getRole().equals(UserRole.ATHLETE)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Athlete profile not found.");
                return "redirect:/" + currentUser.getRole().toString().toLowerCase() + "/dashboard";
            }

            Athlete athlete = athleteService.getAthleteByUserId(userId);

            model.addAttribute("user", currentUser);
            model.addAttribute("role", currentUser.getRole().toString().toLowerCase());
            model.addAttribute("activeTab", "profile");
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("targetProfile", athlete);
            model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

            return "userProfile";
        } catch (Exception e) {
            System.out.println("Error visiting athlete profile: " + e.getMessage());
            System.err.println("Athlete profile visit error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading profile.");
            return "redirect:/" + ((User) session.getAttribute("user")).getRole().toString().toLowerCase() + "/dashboard";
        }
    }

    @GetMapping("/coach/profile/{userId}")
    public String visitCoachProfile(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view profiles.");
                return "redirect:/login";
            }

            User targetUser = userService.getUserById(userId);
            if (targetUser == null || !targetUser.getRole().equals(UserRole.COACH)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Coach profile not found.");
                return "redirect:/" + currentUser.getRole().toString().toLowerCase() + "/dashboard";
            }

            Coach coach = coachService.getCoachByUserId(userId);

            model.addAttribute("user", currentUser);
            model.addAttribute("role", currentUser.getRole().toString().toLowerCase());
            model.addAttribute("activeTab", "profile");
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("targetProfile", coach);
            model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

            return "userProfile";
        } catch (Exception e) {
            System.out.println("Error visiting coach profile: " + e.getMessage());
            System.err.println("Coach profile visit error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading profile.");
            return "redirect:/" + ((User) session.getAttribute("user")).getRole().toString().toLowerCase() + "/dashboard";
        }
    }

    @GetMapping("/sponsor/profile/{userId}")
    public String visitSponsorProfile(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please log in to view profiles.");
                return "redirect:/login";
            }

            User targetUser = userService.getUserById(userId);
            if (targetUser == null || !targetUser.getRole().equals(UserRole.SPONSOR)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Sponsor profile not found.");
                return "redirect:/" + currentUser.getRole().toString().toLowerCase() + "/dashboard";
            }

            Sponsor sponsor = sponsorService.getSponsorByUserId(userId);

            model.addAttribute("user", currentUser);
            model.addAttribute("role", currentUser.getRole().toString().toLowerCase());
            model.addAttribute("activeTab", "profile");
            model.addAttribute("targetUser", targetUser);
            model.addAttribute("targetProfile", sponsor);
            model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

            return "userProfile";
        } catch (Exception e) {
            System.out.println("Error visiting sponsor profile: " + e.getMessage());
            System.err.println("Sponsor profile visit error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading profile.");
            return "redirect:/" + ((User) session.getAttribute("user")).getRole().toString().toLowerCase() + "/dashboard";
        }
    }

    // Helper method for viewing other users' profiles
    private String getUserProfilePage(Long userId, Model model, String expectedRole) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get the target user
        User targetUser = userService.getUserById(userId);
        if (targetUser == null) {
            model.addAttribute("errorMessage", "User not found");
            return "redirect:/" + currentUser.getRole().toString().toLowerCase() + "/dashboard";
        }

        // Verify role matches the URL pattern using equalsIgnoreCase
        if (!targetUser.getRole().toString().equalsIgnoreCase(expectedRole)) {
            model.addAttribute("errorMessage", "Invalid profile link");
            return "redirect:/" + currentUser.getRole().toString().toLowerCase() + "/dashboard";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("role", currentUser.getRole().toString().toLowerCase());
        model.addAttribute("targetRole", targetUser.getRole().toString().toLowerCase());
        model.addAttribute("isOwnProfile", currentUser.getId().equals(userId));

        return "userProfile";
    }

    // Helper methods for role-based page rendering
    private String getAthletePageWithTab(Model model, String activeTab) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals(UserRole.ATHLETE)) {
            return "redirect:/login";
        }

        List<Post> posts = postService.getAllPosts();
        int pendingInvitationCount = invitationService.getPendingInvitationCount(user);

        model.addAttribute("user", user);
        model.addAttribute("role", "athlete");
        model.addAttribute("posts", posts);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("pendingInvitationCount", pendingInvitationCount);

        return "dashboard";
    }

    private String getCoachPageWithTab(Model model, String activeTab) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals(UserRole.COACH)) {
            return "redirect:/login";
        }

        List<Post> posts = postService.getAllPosts();
        int pendingInvitationCount = invitationService.getPendingInvitationCount(user);

        model.addAttribute("user", user);
        model.addAttribute("role", "coach");
        model.addAttribute("posts", posts);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("pendingInvitationCount", pendingInvitationCount);

        return "dashboard";
    }

    private String getSponsorPageWithTab(Model model, String activeTab) {
        User user = (User) session.getAttribute("user");
        if (user == null || !user.getRole().equals(UserRole.SPONSOR)) {
            return "redirect:/login";
        }

        List<Post> posts = postService.getAllPosts();
        int pendingInvitationCount = invitationService.getPendingInvitationCount(user);

        model.addAttribute("user", user);
        model.addAttribute("role", "sponsor");
        model.addAttribute("posts", posts);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("pendingInvitationCount", pendingInvitationCount);

        return "dashboard";
    }
}
