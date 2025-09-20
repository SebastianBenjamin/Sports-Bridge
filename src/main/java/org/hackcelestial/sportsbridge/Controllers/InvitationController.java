package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Models.*;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;
import org.hackcelestial.sportsbridge.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @Autowired
    private PostService postService;

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private SponsorService sponsorService;

    @Autowired
    private HttpSession session;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendInvitation(@RequestBody Map<String, Object> requestBody) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Extract postId and message from request body
            Long postId = null;
            String message = null;

            if (requestBody.get("postId") != null) {
                if (requestBody.get("postId") instanceof Number) {
                    postId = ((Number) requestBody.get("postId")).longValue();
                } else {
                    postId = Long.parseLong(requestBody.get("postId").toString());
                }
            }

            if (requestBody.get("message") != null) {
                message = requestBody.get("message").toString();
            }

            if (postId == null) {
                response.put("success", false);
                response.put("message", "Post ID is required");
                return ResponseEntity.badRequest().body(response);
            }

            Post post = postService.getPostById(postId);
            if (post == null) {
                response.put("success", false);
                response.put("message", "Post not found");
                return ResponseEntity.status(404).body(response);
            }

            // Check if user is trying to send invitation to their own post
            if (post.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "You cannot send an invitation to your own post");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if invitation already sent
            if (invitationService.hasAlreadySentInvitation(user, post)) {
                response.put("success", false);
                response.put("message", "You have already sent an invitation for this post");
                return ResponseEntity.badRequest().body(response);
            }

            boolean sent = invitationService.sendInvitation(user, post, message);
            if (sent) {
                response.put("success", true);
                response.put("message", "Invitation sent successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to send invitation");
            }

        } catch (Exception e) {
            System.out.println("Error sending invitation: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error sending invitation");
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{invitationId}/respond")
    public ResponseEntity<Map<String, Object>> respondToInvitation(
            @PathVariable Long invitationId,
            @RequestParam("status") String status) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            InvitationStatus invitationStatus;
            try {
                invitationStatus = InvitationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", "Invalid status");
                return ResponseEntity.badRequest().body(response);
            }

            // Use the enhanced invitation service
            Map<String, Object> result = invitationService.respondToInvitation(invitationId, invitationStatus, user);

            if ((Boolean) result.get("success")) {
                Invitation invitation = invitationService.getInvitationById(invitationId);
                if (invitation != null && invitationStatus == InvitationStatus.ACCEPTED) {
                    response.put("redirectToProfile", true);
                    response.put("senderRole", invitation.getSender().getRole().toString().toLowerCase());
                    response.put("senderId", invitation.getSender().getId());
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("Error responding to invitation: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error responding to invitation");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{invitationId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmInvitationAcceptance(
            @PathVariable Long invitationId,
            @RequestParam("forceAccept") Boolean forceAccept) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Map<String, Object> result = invitationService.confirmInvitationAcceptance(invitationId, user, forceAccept);

            if ((Boolean) result.get("success")) {
                Invitation invitation = invitationService.getInvitationById(invitationId);
                if (invitation != null) {
                    response.put("redirectToProfile", true);
                    response.put("senderRole", invitation.getSender().getRole().toString().toLowerCase());
                    response.put("senderId", invitation.getSender().getId());
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("Error confirming invitation: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error confirming invitation");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent")
    public ResponseEntity<Map<String, Object>> getSentInvitations() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            List<Invitation> sentInvitations = invitationService.getSentInvitations(user);
            List<Map<String, Object>> invitationResponses = sentInvitations.stream()
                    .map(invitationService::createInvitationResponse)
                    .toList();

            response.put("success", true);
            response.put("invitations", invitationResponses);

        } catch (Exception e) {
            System.out.println("Error fetching sent invitations: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching sent invitations");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/received")
    public ResponseEntity<Map<String, Object>> getReceivedInvitations() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            List<Invitation> receivedInvitations = invitationService.getReceivedInvitations(user);
            List<Map<String, Object>> invitationResponses = receivedInvitations.stream()
                    .map(invitationService::createInvitationResponse)
                    .toList();

            response.put("success", true);
            response.put("invitations", invitationResponses);

        } catch (Exception e) {
            System.out.println("Error fetching received invitations: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching received invitations");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingInvitations() {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            List<Invitation> pendingInvitations = invitationService.getPendingInvitations(user);
            List<Map<String, Object>> invitationResponses = pendingInvitations.stream()
                    .map(invitationService::createInvitationResponse)
                    .toList();

            response.put("success", true);
            response.put("invitations", invitationResponses);

        } catch (Exception e) {
            System.out.println("Error fetching pending invitations: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching pending invitations");
        }

        return ResponseEntity.ok(response);
    }
}
