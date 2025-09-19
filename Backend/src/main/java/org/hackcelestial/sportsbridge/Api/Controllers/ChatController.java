package org.hackcelestial.sportsbridge.Api.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.hackcelestial.sportsbridge.Api.Security.CurrentUser;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private HttpSession session;

    private Long resolveLegacyUserId(User sessionUser, SbUser currentUser) {
        if (sessionUser != null && sessionUser.getId() != null) return sessionUser.getId();
        if (currentUser == null) return null;
        List<Long> ids = jdbc.query("SELECT id FROM users WHERE (email = ? AND email IS NOT NULL) OR (phone = ? AND phone IS NOT NULL) ORDER BY id LIMIT 1",
                (rs, i) -> rs.getLong(1),
                Optional.ofNullable(currentUser.getEmail()).orElse(""),
                Optional.ofNullable(currentUser.getPhone()).orElse("")
        );
        return ids.isEmpty() ? null : ids.get(0);
    }

    private Map<String, Object> getRoom(Long roomId) {
        return jdbc.query("SELECT id, coach_id, player_id, created_at FROM chat_rooms WHERE id = ?",
                rs -> rs.next() ? Map.of(
                        "id", rs.getLong("id"),
                        "coach_id", rs.getLong("coach_id"),
                        "player_id", rs.getLong("player_id"),
                        "created_at", rs.getTimestamp("created_at")
                ) : null,
                roomId);
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> listMessages(@PathVariable Long roomId, @CurrentUser SbUser currentUser) {
        User su = (User) session.getAttribute("user");
        Long uid = resolveLegacyUserId(su, currentUser);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Map<String, Object> room = getRoom(roomId);
        if (room == null) return ResponseEntity.status(404).body(Map.of("error", "Room not found"));
        Long coachId = (Long) room.get("coach_id");
        Long playerId = (Long) room.get("player_id");
        if (!Objects.equals(uid, coachId) && !Objects.equals(uid, playerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Not a participant of this room"));
        }
        List<Map<String, Object>> items = jdbc.query(
                "SELECT id, room_id, sender_id, content, created_at FROM chat_messages WHERE room_id = ? ORDER BY created_at ASC",
                (rs, i) -> Map.of(
                        "id", rs.getLong("id"),
                        "room_id", rs.getLong("room_id"),
                        "sender_id", rs.getLong("sender_id"),
                        "content", rs.getString("content"),
                        "created_at", rs.getTimestamp("created_at")
                ),
                roomId
        );
        return ResponseEntity.ok(items);
    }

    public static class SendMessageBody { public String content; }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable Long roomId, @RequestBody SendMessageBody body, @CurrentUser SbUser currentUser) {
        if (body == null || body.content == null || body.content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content required"));
        }
        User su = (User) session.getAttribute("user");
        Long uid = resolveLegacyUserId(su, currentUser);
        if (uid == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Map<String, Object> room = getRoom(roomId);
        if (room == null) return ResponseEntity.status(404).body(Map.of("error", "Room not found"));
        Long coachId = (Long) room.get("coach_id");
        Long playerId = (Long) room.get("player_id");
        if (!Objects.equals(uid, coachId) && !Objects.equals(uid, playerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Not a participant of this room"));
        }
        // Enforce: only coach may send the first message
        Integer count = jdbc.query("SELECT COUNT(*) FROM chat_messages WHERE room_id = ?", rs -> rs.next() ? rs.getInt(1) : 0, roomId);
        if (count == 0 && !Objects.equals(uid, coachId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Coach must send the first message"));
        }
        Long msgId = jdbc.query("INSERT INTO chat_messages (room_id, sender_id, content, created_at) VALUES (?, ?, ?, NOW()) RETURNING id",
                rs -> rs.next() ? rs.getLong(1) : null, roomId, uid, body.content.trim());
        Map<String, Object> resp = Map.of("id", msgId, "room_id", roomId, "sender_id", uid, "content", body.content.trim());
        return ResponseEntity.ok(resp);
    }
}

