package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Invitation;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;
import org.hackcelestial.sportsbridge.Repositories.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Transactional
    public boolean sendInvitation(User sender, Post post, String message) {
        try {
            // Check if invitation already exists
            if (invitationRepository.existsBySenderAndPost(sender, post)) {
                return false; // Invitation already sent
            }

            // Don't allow sending invitation to own post
            if (post.getUser().getId().equals(sender.getId())) {
                return false;
            }

            Invitation invitation = new Invitation();
            invitation.setSender(sender);
            invitation.setReceiver(post.getUser());
            invitation.setPost(post);
            invitation.setMessage(message);
            invitation.setStatus(InvitationStatus.PENDING);
            invitation.setSentAt(LocalDateTime.now());

            invitationRepository.save(invitation);
            return true;
        } catch (Exception e) {
            System.out.println("Error sending invitation: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean respondToInvitation(Long invitationId, InvitationStatus status, User user) {
        try {
            Invitation invitation = invitationRepository.findById(invitationId).orElse(null);
            if (invitation == null) {
                return false;
            }

            // Check if user is the receiver
            if (!invitation.getReceiver().getId().equals(user.getId())) {
                return false;
            }

            // Check if invitation is still pending
            if (invitation.getStatus() != InvitationStatus.PENDING) {
                return false;
            }

            invitation.setStatus(status);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation);
            return true;
        } catch (Exception e) {
            System.out.println("Error responding to invitation: " + e.getMessage());
            return false;
        }
    }

    public List<Invitation> getSentInvitations(User sender) {
        try {
            return invitationRepository.findBySenderOrderBySentAtDesc(sender);
        } catch (Exception e) {
            System.out.println("Error fetching sent invitations: " + e.getMessage());
            return List.of();
        }
    }

    public List<Invitation> getReceivedInvitations(User receiver) {
        try {
            return invitationRepository.findByReceiverOrderBySentAtDesc(receiver);
        } catch (Exception e) {
            System.out.println("Error fetching received invitations: " + e.getMessage());
            return List.of();
        }
    }

    public List<Invitation> getPendingInvitations(User receiver) {
        try {
            return invitationRepository.findPendingInvitationsByReceiver(receiver);
        } catch (Exception e) {
            System.out.println("Error fetching pending invitations: " + e.getMessage());
            return List.of();
        }
    }

    public Invitation getInvitationById(Long id) {
        return invitationRepository.findById(id).orElse(null);
    }

    public boolean hasAlreadySentInvitation(User sender, Post post) {
        try {
            return invitationRepository.existsBySenderAndPost(sender, post);
        } catch (Exception e) {
            System.out.println("Error checking existing invitation: " + e.getMessage());
            return false;
        }
    }

    // Scheduled task to clean up old invitations (runs daily at 2 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldInvitations() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(1);
            List<Invitation> oldInvitations = invitationRepository.findOldRespondedInvitations(cutoffDate);

            if (!oldInvitations.isEmpty()) {
                invitationRepository.deleteAll(oldInvitations);
                System.out.println("Cleaned up " + oldInvitations.size() + " old invitations");
            }
        } catch (Exception e) {
            System.out.println("Error cleaning up old invitations: " + e.getMessage());
        }
    }

    public Map<String, Object> createInvitationResponse(Invitation invitation) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", invitation.getId());
        response.put("message", invitation.getMessage());
        response.put("status", invitation.getStatus().toString());
        response.put("sentAt", invitation.getSentAt());
        response.put("respondedAt", invitation.getRespondedAt());

        // Sender info
        Map<String, Object> senderInfo = new HashMap<>();
        senderInfo.put("id", invitation.getSender().getId());
        senderInfo.put("name", invitation.getSender().getFirstName() + " " + invitation.getSender().getLastName());
        senderInfo.put("email", invitation.getSender().getEmail());
        senderInfo.put("role", invitation.getSender().getRole());
        senderInfo.put("profileImageUrl", invitation.getSender().getProfileImageUrl());
        response.put("sender", senderInfo);

        // Receiver info
        Map<String, Object> receiverInfo = new HashMap<>();
        receiverInfo.put("id", invitation.getReceiver().getId());
        receiverInfo.put("name", invitation.getReceiver().getFirstName() + " " + invitation.getReceiver().getLastName());
        receiverInfo.put("email", invitation.getReceiver().getEmail());
        receiverInfo.put("role", invitation.getReceiver().getRole());
        receiverInfo.put("profileImageUrl", invitation.getReceiver().getProfileImageUrl());
        response.put("receiver", receiverInfo);

        // Post info
        Map<String, Object> postInfo = new HashMap<>();
        postInfo.put("id", invitation.getPost().getId());
        postInfo.put("title", invitation.getPost().getTitle());
        postInfo.put("description", invitation.getPost().getDescription());
        postInfo.put("postType", invitation.getPost().getPostType());
        postInfo.put("imageUrl", invitation.getPost().getImageUrl());
        response.put("post", postInfo);

        return response;
    }
}
