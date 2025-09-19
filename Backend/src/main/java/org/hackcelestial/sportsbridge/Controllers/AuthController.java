package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.UserService;
import org.hackcelestial.sportsbridge.Services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    private HttpSession session;

    @GetMapping("/login")
    public String loginPage() {
        // If already logged in, go to dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        User user = userService.findByEmailAndPassword(email, password);
        if (user == null) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
        session.setAttribute("user", user);
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new User());
        }
        model.addAttribute("roles", UserRole.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("userForm") User userForm,
                           @RequestParam("profileImage") MultipartFile profileImage,
                           Model model) {
        // Basic validations
        if (userForm.getEmail() == null || userForm.getPassword() == null || userForm.getRole() == null) {
            model.addAttribute("error", "Email, password and role are required");
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }
        if (profileImage == null || profileImage.isEmpty()) {
            model.addAttribute("error", "Profile image is required");
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }
        if (userService.userExists(userForm.getEmail())) {
            model.addAttribute("error", "User already exists with this email");
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }
        try {
            String absolutePath = utilityService.storeFile(profileImage);
            String webPath = toWebPath(absolutePath);
            userForm.setProfileImageUrl(webPath);
        } catch (Exception ex) {
            model.addAttribute("error", "Failed to upload profile image");
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }

        userForm.setCreatedAt(LocalDateTime.now());
        userForm.setActive(true);
        boolean saved = userService.save(userForm);
        if (!saved) {
            model.addAttribute("error", "Failed to register user");
            model.addAttribute("roles", UserRole.values());
            return "auth/register";
        }
        // Auto-login after registration
        session.setAttribute("user", userForm);
        return "redirect:/dashboard";
    }

    private String toWebPath(String absolutePath) {
        // Map .../AppImages/<sub>/<file> -> /uploads/<sub>/<file>
        String marker = File.separator + "AppImages" + File.separator;
        int idx = absolutePath.lastIndexOf(marker);
        if (idx != -1) {
            String rel = absolutePath.substring(idx + marker.length());
            return "/uploads/" + rel.replace('\\', '/');
        }
        // Fallback: serve whole absolute path as file URL (best-effort)
        return "/uploads/" + Paths.get(absolutePath).getFileName().toString();
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
