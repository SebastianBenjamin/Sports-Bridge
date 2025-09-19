package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;

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
        model.addAttribute("user", user);
        return "dashboard";
    }

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
