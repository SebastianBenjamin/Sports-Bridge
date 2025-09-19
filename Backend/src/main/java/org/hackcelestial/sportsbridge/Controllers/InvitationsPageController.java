package org.hackcelestial.sportsbridge.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/invitations")
public class InvitationsPageController {

    @GetMapping
    public String index() {
        return "invitations/index";
    }
}

