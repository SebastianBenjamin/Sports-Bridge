package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private HttpSession session;

    private String guard() {
        return (session.getAttribute("user") == null) ? "redirect:/auth/login" : null;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        String redirect = guard();
        if (redirect != null) return redirect;
        return "dashboard";
    }

    // Common views
    @GetMapping("/posts")
    public String posts(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "posts");
        return "dashboard";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "notifications");
        return "dashboard";
    }

    // NEW: Explore feed inside dashboard
    @GetMapping("/explore")
    public String explore(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "explore");
        return "dashboard";
    }

    // NEW: Create post inside dashboard
    @GetMapping("/create")
    public String createPost(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "create-post");
        return "dashboard";
    }

    // NEW: Invitations inside dashboard
    @GetMapping("/invitations")
    public String invitations(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "invitations");
        return "dashboard";
    }

    // Athlete views
    @GetMapping("/logs")
    public String logs(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "logs");
        return "dashboard";
    }

    @GetMapping("/sponsorships")
    public String sponsorships(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "sponsorships");
        return "dashboard";
    }

    @GetMapping("/my-coach")
    public String myCoach(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "my-coach");
        return "dashboard";
    }

    // Coach views
    @GetMapping("/students")
    public String students(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "students");
        return "dashboard";
    }

    @GetMapping("/achievements")
    public String achievements(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "achievements");
        return "dashboard";
    }

    // Sponsor views
    @GetMapping("/athletes")
    public String athletes(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "athletes");
        return "dashboard";
    }

    // Admin views
    @GetMapping("/admin/users")
    public String adminUsers(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "admin-users");
        return "dashboard";
    }

    @GetMapping("/admin/sports")
    public String adminSports(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "admin-sports");
        return "dashboard";
    }

    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        String redirect = guard();
        if (redirect != null) return redirect;
        model.addAttribute("activeView", "admin-reports");
        return "dashboard";
    }
}
