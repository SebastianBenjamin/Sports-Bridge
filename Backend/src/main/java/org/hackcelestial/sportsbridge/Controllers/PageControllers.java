package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Models.Sponsor;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.AthleteService;
import org.hackcelestial.sportsbridge.Services.SponserService;
import org.hackcelestial.sportsbridge.Services.UserService;
import org.hackcelestial.sportsbridge.Services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    SponserService sponserService;

    @GetMapping("/")
    public String home() {
    if(session.getAttribute("user") == null) {
        return "dashboard";
    }
    return "index";
    }

    @GetMapping("/register")
    public String register(
            Model model
    ) {
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
            MultipartFile photo,
            RedirectAttributes redirectAttributes,
            @RequestParam("role") String role
                           ) throws IOException {
        if(session.getAttribute("user") == null) {
            return "index";
        }
        String url=utilityService.storeFile(photo);
        user.setProfileImageUrl(url);
        user.setActive(true);
        user.setReportedTimes(0);
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(UserRole.valueOf(role.toUpperCase()));
        if(userService.save(user)) {
            session.setAttribute("user", user);
            model.addAttribute("user", user);
            redirectAttributes.addFlashAttribute("Success", "User registered successfully");
            switch (role) {
                case "admin":
                        return "redirect:/dashboard";
                case "athlete":
                    Athlete athlete = new Athlete();
                    model.addAttribute("athlete", athlete);
                    return "redirect:/athleteRegister";
                case "sponsor":
                    Sponsor sponsor = new Sponsor();
                    model.addAttribute("sponsor", sponsor);
                    return "redirect:/sponsorRegister";
                case "coach":
                    Coach coach = new Coach();
                    model.addAttribute("coach", coach);
                    return "redirect:/coachRegister";
            }
        }
        model.addAttribute("user", user);
        redirectAttributes.addFlashAttribute("Error", "User could not be registered");
        return "redirect:/registerUser";
    }
    @PostMapping("/athleteRegister")
    public String adminRegister(
        Athlete athlete,
        Model model,
        RedirectAttributes redirectAttributes
    ){
    if(session.getAttribute("user") == null) {
        return "index";
    }
    if(athlete!=null){

        if(athleteService.save(athlete)) {
            redirectAttributes.addFlashAttribute("Success", "Athlete registered successfully");
            session.setAttribute("role","athlete");
            return "redirect:/dashboard";
        }
        else{
            redirectAttributes.addFlashAttribute("Error", "Athlete could not be registered");
        }
    }
    model.addAttribute("athlete", athlete);
    return "redirect:/registerUser";
    }

    @PostMapping("/sponsorRegister")
    public String sponsorRegister(
            Sponsor sponsor,
            Model model,
            RedirectAttributes redirectAttributes
    ){
        if(session.getAttribute("user") == null) {
            return "index";
        }
        if(sponsor!=null){
            if(sponserService.save(sponsor)) {
                redirectAttributes.addFlashAttribute("Success", "Sponsor registered successfully");
                session.setAttribute("role","sponsor");
                return "redirect:/dashboard";
            }
        }
        model.addAttribute("sponsor", sponsor);
        return "redirect:/registerUser";

    }
}
