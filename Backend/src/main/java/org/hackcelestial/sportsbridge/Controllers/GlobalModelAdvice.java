package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private HttpSession session;

    @ModelAttribute("user")
    public User addUserToModel() {
        Object obj = session.getAttribute("user");
        if (obj instanceof User) {
            return (User) obj;
        }
        return null;
    }
}

