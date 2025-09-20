package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Notification;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.NotificationRepository;
import org.hackcelestial.sportsbridge.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public Notification createNotification(User recipient, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public void createPostNotification(Post post) {
        String message = String.format("%s %s created a new %s post: %s",
                post.getUser().getFirstName(),
                post.getUser().getLastName(),
                post.getPostType().name().toLowerCase(),
                post.getTitle());

        // Notify all users except the post author
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            if (!user.getId().equals(post.getUser().getId())) {
                createNotification(user, message);
            }
        }
    }

    public void createLikeNotification(Post post, User liker) {
        if (!liker.getId().equals(post.getUser().getId())) {
            String message = String.format("%s %s liked your post: %s",
                    liker.getFirstName(),
                    liker.getLastName(),
                    post.getTitle());
            createNotification(post.getUser(), message);
        }
    }

    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByRecipientAndIsReadOrderByCreatedAtDesc(user, false);
    }

    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndIsRead(user, false);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}
