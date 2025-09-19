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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @GetMapping("/login")
    public String loginPage() {
        // Render the legacy login page; it now includes links to OTP and phone+password flows
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        // Updated: verify against hashed password if present
        User user = userService.findByEmail(email);
        if (user == null) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
        String stored = user.getPassword();
        boolean ok = false;
        if (stored != null) {
            if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
                ok = encoder.matches(password, stored);
            } else {
                ok = stored.equals(password);
            }
        }
        if (!ok) {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
        session.setAttribute("user", user);
        return "redirect:/dashboard";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        // Redirect to the new OTP-based signup
        return "redirect:/otp-login.html";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("userForm") User userForm,
                           @RequestParam("profileImage") MultipartFile profileImage,
                           Model model) {
        // Legacy handler retained for backward-compat, but not used by the new UI
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

        // Hash the password before saving (legacy fallback preserves existing plaintext comparisons)
        String raw = userForm.getPassword();
        if (raw != null && !raw.isBlank()) {
            userForm.setPassword(encoder.encode(raw));
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

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        // Route users to OTP page; after OTP they can set password
        return "redirect:/otp-login.html";
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/auth/login";
    }
}
