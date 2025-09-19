package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Api.Services.JwtService;
import org.hackcelestial.sportsbridge.Api.Repositories.SbUserRepository;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/invitations")
public class InvitationsApiController {

    @Autowired private HttpSession session;
    @Autowired private JwtService jwtService;
    @Autowired private SbUserRepository sbUserRepository;

    private User currentSessionUser() { return (User) session.getAttribute("user"); }

    private boolean authorized(HttpServletRequest req) {
        if (currentSessionUser() != null) return true;
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                String sub = jwtService.parse(token).getSubject();
                Long uid = Long.parseLong(sub);
                Optional<SbUser> u = sbUserRepository.findById(uid);
                return u.isPresent();
            } catch (Exception ignore) { }
        }
        return false;
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest req) {
        if (!authorized(req)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        // TODO: hook to real invitations once schema exists. For now, return an empty list.
        return ResponseEntity.ok(List.of());
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id, HttpServletRequest req) {
        if (!authorized(req)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        // TODO: persist acceptance
        return ResponseEntity.ok(Map.of("status", "accepted", "id", id));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<?> decline(@PathVariable Long id, HttpServletRequest req) {
        if (!authorized(req)) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        // TODO: persist decline
        return ResponseEntity.ok(Map.of("status", "declined", "id", id));
    }
}
