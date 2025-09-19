package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.UserService;
import org.hackcelestial.sportsbridge.Services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class PageControllers {
    @Autowired
    HttpSession session;
    @Autowired
    UserService userService;
    @Autowired
    UtilityService utilityService;

    @GetMapping("/")
    public String home() {
        // Redirect anonymous users to unified login, authenticated users to dashboard
        if (session.getAttribute("user") == null) {
            return "redirect:/auth/login";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/registerUser")
    public String register(User user, Model model, MultipartFile photo) throws IOException {
        if(session.getAttribute("user") == null) {
            return "redirect:/auth/login";
        }
        String url = utilityService.storeFile(photo);
        return "redirect:/" + url;
    }
}
