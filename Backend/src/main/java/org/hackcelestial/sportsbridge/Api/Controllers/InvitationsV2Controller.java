package org.hackcelestial.sportsbridge.Api.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.hackcelestial.sportsbridge.Api.Security.CurrentUser;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/invitations")
public class InvitationsV2Controller {

    @Autowired private HttpSession session;
    @Autowired private JdbcTemplate jdbc;

    private Long resolveLegacyUserId(User sessionUser, SbUser currentUser) {
        if (sessionUser != null && sessionUser.getId() != null) return sessionUser.getId();
        if (currentUser == null) return null;
        // Try mapping by email or phone in legacy users table
        List<Long> ids = jdbc.query("SELECT id FROM users WHERE (email = ? AND email IS NOT NULL) OR (phone = ? AND phone IS NOT NULL) ORDER BY id LIMIT 1",
                (rs, i) -> rs.getLong(1),
                Optional.ofNullable(currentUser.getEmail()).orElse(""),
                Optional.ofNullable(currentUser.getPhone()).orElse("")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    @GetMapping("/my")
    public ResponseEntity<?> list(@CurrentUser SbUser currentUser) {
        User su = (User) session.getAttribute("user");
        Long uid = resolveLegacyUserId(su, currentUser);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        NamedParameterJdbcTemplate np = new NamedParameterJdbcTemplate(jdbc);
        Map<String, Object> params = Map.of("uid", uid);
        List<Map<String, Object>> rows = np.queryForList(
                "SELECT id, coach_id, player_id, status, created_at FROM invitations WHERE coach_id = :uid OR player_id = :uid ORDER BY created_at DESC",
                params
        );
        return ResponseEntity.ok(rows);
    }

    public static class CreateInvitationBody { public Long playerId; public String message; }

    @PostMapping
    public ResponseEntity<?> createInvitation(@RequestBody CreateInvitationBody body, @CurrentUser SbUser currentUser) {
        if (body == null || body.playerId == null) return ResponseEntity.badRequest().body(Map.of("error", "playerId required"));
        User su = (User) session.getAttribute("user");
        Long coachId = resolveLegacyUserId(su, currentUser);
        if (coachId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        // Ensure current legacy user is a coach
        String role = jdbc.query("SELECT role FROM users WHERE id = ?", rs -> rs.next() ? rs.getString(1) : null, coachId);
        if (!"COACH".equalsIgnoreCase(String.valueOf(role))) {
            return ResponseEntity.status(403).body(Map.of("error", "Only coaches can send invitations"));
        }
        // Insert invitation PENDING
        NamedParameterJdbcTemplate np = new NamedParameterJdbcTemplate(jdbc);
        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("coach", coachId)
                .addValue("player", body.playerId)
                .addValue("status", "PENDING");
        Long id = np.queryForObject(
                "INSERT INTO invitations (coach_id, player_id, status, created_at) VALUES (:coach, :player, :status, NOW()) RETURNING id",
                ps, Long.class);
        if (id == null) return ResponseEntity.status(500).body(Map.of("error", "Failed to create invitation"));
        return ResponseEntity.ok(Map.of("id", id, "status", "PENDING"));
    }

    public static class RespondBody { public String action; }

    @PostMapping("/{id}/respond")
    public ResponseEntity<?> respond(@PathVariable Long id, @RequestBody RespondBody body, @CurrentUser SbUser currentUser) {
        if (id == null || body == null || body.action == null) return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
        User su = (User) session.getAttribute("user");
        Long uid = resolveLegacyUserId(su, currentUser);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        Map<String, Object> inv = jdbc.query("SELECT id, coach_id, player_id, status FROM invitations WHERE id = ?",
                rs -> rs.next() ? Map.of(
                        "id", rs.getLong("id"),
                        "coach_id", rs.getLong("coach_id"),
                        "player_id", rs.getLong("player_id"),
                        "status", rs.getString("status")
                ) : null,
                id);
        if (inv == null) return ResponseEntity.status(404).body(Map.of("error", "Invitation not found"));
        Long playerId = (Long) inv.get("player_id");
        if (!Objects.equals(playerId, uid)) {
            return ResponseEntity.status(403).body(Map.of("error", "Only the invited player can respond"));
        }
        String action = body.action.toLowerCase(Locale.ROOT);
        if (!action.equals("accept") && !action.equals("reject")) {
            return ResponseEntity.badRequest().body(Map.of("error", "action must be accept or reject"));
        }
        String newStatus = action.equals("accept") ? "ACCEPTED" : "REJECTED";
        jdbc.update("UPDATE invitations SET status = ? WHERE id = ?", newStatus, id);

        Map<String, Object> result = new HashMap<>();
        result.put("status", newStatus);
        if (action.equals("accept")) {
            Long coachId = (Long) inv.get("coach_id");
            // Create chat room if not exists
            List<Long> rid = jdbc.query("SELECT id FROM chat_rooms WHERE coach_id = ? AND player_id = ?",
                    (rs, i) -> rs.getLong(1), coachId, playerId);
            Long roomId;
            if (rid.isEmpty()) {
                roomId = jdbc.query("INSERT INTO chat_rooms (coach_id, player_id, created_at) VALUES (?, ?, NOW()) RETURNING id",
                        rs -> rs.next() ? rs.getLong(1) : null,
                        coachId, playerId);
            } else { roomId = rid.get(0); }
            result.put("roomId", roomId);
        }
        return ResponseEntity.ok(result);
    }
}
