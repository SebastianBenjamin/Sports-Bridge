package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Invitation;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findBySenderOrderBySentAtDesc(User sender);

    List<Invitation> findByReceiverOrderBySentAtDesc(User receiver);

    boolean existsBySenderAndPost(User sender, Post post);

    @Query("SELECT i FROM Invitation i WHERE i.status IN ('ACCEPTED', 'DECLINED') AND i.respondedAt < :cutoffDate")
    List<Invitation> findOldRespondedInvitations(LocalDateTime cutoffDate);

    @Query("SELECT i FROM Invitation i WHERE i.receiver = ?1 AND i.status = 'PENDING' ORDER BY i.sentAt DESC")
    List<Invitation> findPendingInvitationsByReceiver(User receiver);
}
