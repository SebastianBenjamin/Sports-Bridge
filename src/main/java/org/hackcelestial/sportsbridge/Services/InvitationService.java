package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Invitation;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Models.Athlete;
import org.hackcelestial.sportsbridge.Models.Coach;
import org.hackcelestial.sportsbridge.Models.Sponsor;
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

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private SponsorService sponsorService;

    @Autowired
    private UtilityService utilityService;

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
    public Map<String, Object> respondToInvitation(Long invitationId, InvitationStatus status, User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            Invitation invitation = invitationRepository.findById(invitationId).orElse(null);
            if (invitation == null) {
                result.put("success", false);
                result.put("message", "Invitation not found");
                return result;
            }

            // Check if user is the receiver
            if (!invitation.getReceiver().getId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "Unauthorized to respond to this invitation");
                return result;
            }

            // Check if invitation is still pending
            if (invitation.getStatus() != InvitationStatus.PENDING) {
                result.put("success", false);
                result.put("message", "Invitation already responded to");
                return result;
            }

            if (status == InvitationStatus.ACCEPTED) {
                // Handle acceptance logic based on user roles
                Map<String, Object> acceptanceResult = handleInvitationAcceptance(invitation);
                if (!(Boolean) acceptanceResult.get("success")) {
                    return acceptanceResult;
                }

                // If there's a confirmation required, return that info
                if (acceptanceResult.containsKey("requiresConfirmation")) {
                    result.put("requiresConfirmation", true);
                    result.put("confirmationMessage", acceptanceResult.get("confirmationMessage"));
                    result.put("invitationId", invitationId);
                    return result;
                }
            }

            // Update invitation status
            invitation.setStatus(status);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

            result.put("success", true);
            result.put("message", status == InvitationStatus.ACCEPTED ? "Invitation accepted successfully" : "Invitation declined");
            return result;
        } catch (Exception e) {
            System.out.println("Error responding to invitation: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Error processing invitation response");
            return result;
        }
    }

    @Transactional
    public Map<String, Object> confirmInvitationAcceptance(Long invitationId, User user, boolean forceAccept) {
        Map<String, Object> result = new HashMap<>();
        try {
            Invitation invitation = invitationRepository.findById(invitationId).orElse(null);
            if (invitation == null) {
                result.put("success", false);
                result.put("message", "Invitation not found");
                return result;
            }

            // Verify receiver
            if (!invitation.getReceiver().getId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "Unauthorized");
                return result;
            }

            if (invitation.getStatus() != InvitationStatus.PENDING) {
                result.put("success", false);
                result.put("message", "Invitation already processed");
                return result;
            }

            // Force assign relationships
            assignRelationships(invitation, forceAccept);

            // Update invitation status
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation);

            result.put("success", true);
            result.put("message", "Relationship assigned successfully");
            return result;
        } catch (Exception e) {
            System.out.println("Error confirming invitation: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Error processing confirmation");
            return result;
        }
    }

    private Map<String, Object> handleInvitationAcceptance(Invitation invitation) {
        Map<String, Object> result = new HashMap<>();

        User sender = invitation.getSender();
        User receiver = invitation.getReceiver();

        // Only handle coach/sponsor assignments for athlete receivers
        if (!receiver.getRole().toString().equals("ATHLETE")) {
            result.put("success", true);
            return result;
        }

        try {
            Athlete athlete = athleteService.getAthleteByUser(receiver);
            if (athlete == null) {
                result.put("success", false);
                result.put("message", "Athlete profile not found");
                return result;
            }

            // Check if sender is coach or sponsor
            if (sender.getRole().toString().equals("COACH")) {
                if (athlete.getCurrentCoach() != null) {
                    result.put("success", false);
                    result.put("requiresConfirmation", true);
                    result.put("confirmationMessage", "You already have a coach (" +
                              athlete.getCurrentCoach().getUser().getFirstName() + " " +
                              athlete.getCurrentCoach().getUser().getLastName() +
                              "). Do you want to replace them with " +
                              sender.getFirstName() + " " + sender.getLastName() + "?");
                    return result;
                }
            } else if (sender.getRole().toString().equals("SPONSOR")) {
                if (athlete.getCurrentSponsor() != null) {
                    result.put("success", false);
                    result.put("requiresConfirmation", true);
                    result.put("confirmationMessage", "You already have a sponsor (" +
                              athlete.getCurrentSponsor().getUser().getFirstName() + " " +
                              athlete.getCurrentSponsor().getUser().getLastName() +
                              "). Do you want to replace them with " +
                              sender.getFirstName() + " " + sender.getLastName() + "?");
                    return result;
                }
            }

            // No existing relationship, proceed with assignment
            assignRelationships(invitation, false);
            result.put("success", true);
            return result;

        } catch (Exception e) {
            System.out.println("Error handling invitation acceptance: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Error processing acceptance");
            return result;
        }
    }

    private void assignRelationships(Invitation invitation, boolean forceReplace) {
        User sender = invitation.getSender();
        User receiver = invitation.getReceiver();

        if (!receiver.getRole().toString().equals("ATHLETE")) {
            return;
        }

        try {
            Athlete athlete = athleteService.getAthleteByUser(receiver);
            if (athlete == null) return;

            if (sender.getRole().toString().equals("COACH")) {
                Coach coach = coachService.getCoachByUser(sender);
                if (coach != null) {
                    // Move current coach to previous coaches if exists
                    if (athlete.getCurrentCoach() != null && forceReplace) {
                        if (athlete.getPreviousCoaches() != null) {
                            athlete.getPreviousCoaches().add(athlete.getCurrentCoach());
                        }
                    }
                    athlete.setCurrentCoach(coach);
                    athleteService.save(athlete);
                }
            } else if (sender.getRole().toString().equals("SPONSOR")) {
                Sponsor sponsor = sponsorService.getSponsorByUser(sender);
                if (sponsor != null) {
                    // Move current sponsor to previous sponsors if exists
                    if (athlete.getCurrentSponsor() != null && forceReplace) {
                        if (athlete.getPreviousSponsors() != null) {
                            athlete.getPreviousSponsors().add(athlete.getCurrentSponsor());
                        }
                    }
                    athlete.setCurrentSponsor(sponsor);
                    athleteService.save(athlete);
                }
            }
        } catch (Exception e) {
            System.out.println("Error assigning relationships: " + e.getMessage());
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

    public int getPendingInvitationCount(User receiver) {
        try {
            return invitationRepository.findPendingInvitationsByReceiver(receiver).size();
        } catch (Exception e) {
            System.out.println("Error counting pending invitations: " + e.getMessage());
            return 0;
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

        // Sender info with detailed profile for athletes
        Map<String, Object> senderInfo = createUserProfileInfo(invitation.getSender());
        response.put("sender", senderInfo);

        // Receiver info
        Map<String, Object> receiverInfo = new HashMap<>();
        receiverInfo.put("id", invitation.getReceiver().getId());
        receiverInfo.put("name", invitation.getReceiver().getFirstName() + " " + invitation.getReceiver().getLastName());
        receiverInfo.put("email", invitation.getReceiver().getEmail());
        receiverInfo.put("role", invitation.getReceiver().getRole());
        if (invitation.getReceiver().getProfileImageUrl() != null) {
            receiverInfo.put("profileImageUrl", utilityService.convertFilePathToWebUrl(invitation.getReceiver().getProfileImageUrl()));
        }
        response.put("receiver", receiverInfo);

        // Post info
        Map<String, Object> postInfo = new HashMap<>();
        postInfo.put("id", invitation.getPost().getId());
        postInfo.put("title", invitation.getPost().getTitle());
        postInfo.put("description", invitation.getPost().getDescription());
        postInfo.put("postType", invitation.getPost().getPostType());
        if (invitation.getPost().getImageUrl() != null) {
            postInfo.put("imageUrl", utilityService.convertFilePathToWebUrl(invitation.getPost().getImageUrl()));
        }
        response.put("post", postInfo);

        return response;
    }

    private Map<String, Object> createUserProfileInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getFirstName() + " " + user.getLastName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        if (user.getProfileImageUrl() != null) {
            userInfo.put("profileImageUrl", utilityService.convertFilePathToWebUrl(user.getProfileImageUrl()));
        }
        userInfo.put("bio", user.getBio());
        userInfo.put("country", user.getCountry());

        // Add detailed profile information for athletes
        if (user.getRole().toString().equals("ATHLETE")) {
            try {
                Athlete athlete = athleteService.getAthleteByUser(user);
                if (athlete != null) {
                    Map<String, Object> athleteProfile = new HashMap<>();
                    athleteProfile.put("height", athlete.getHeight());
                    athleteProfile.put("weight", athlete.getWeight());
                    athleteProfile.put("state", athlete.getState());
                    athleteProfile.put("district", athlete.getDistrict());
                    athleteProfile.put("isDisabled", athlete.getIsDisabled());
                    athleteProfile.put("disabilityType", athlete.getDisabilityType());
                    athleteProfile.put("emergencyContactName", athlete.getEmergencyContactName());
                    athleteProfile.put("emergencyContactPhone", athlete.getEmergencyContactPhone());

                    // Add current relationships
                    if (athlete.getCurrentCoach() != null) {
                        athleteProfile.put("hasCurrentCoach", true);
                        athleteProfile.put("currentCoachName", athlete.getCurrentCoach().getUser().getFirstName() + " " + athlete.getCurrentCoach().getUser().getLastName());
                    } else {
                        athleteProfile.put("hasCurrentCoach", false);
                    }

                    if (athlete.getCurrentSponsor() != null) {
                        athleteProfile.put("hasCurrentSponsor", true);
                        athleteProfile.put("currentSponsorName", athlete.getCurrentSponsor().getUser().getFirstName() + " " + athlete.getCurrentSponsor().getUser().getLastName());
                    } else {
                        athleteProfile.put("hasCurrentSponsor", false);
                    }

                    userInfo.put("athleteProfile", athleteProfile);
                }
            } catch (Exception e) {
                System.out.println("Error fetching athlete profile: " + e.getMessage());
            }
        }

        return userInfo;
    }
}
