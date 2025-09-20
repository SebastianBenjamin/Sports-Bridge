package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Enums.PostType;
import org.hackcelestial.sportsbridge.Enums.UserRole;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationService notificationService;

    public Post createPost(Post post, User author) {
        post.setUser(author);
        post.setPosted_at(LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        
        // Create notification for new post
        notificationService.createPostNotification(savedPost);
        
        return savedPost;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByPosted_atDesc();
    }

    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUserOrderByPosted_atDesc(user);
    }

    public List<Post> getPostsByType(PostType postType) {
        return postRepository.findByPostTypeOrderByPosted_atDesc(postType);
    }

    public List<Post> getPostsByRole(UserRole userRole) {
        return postRepository.findByUserRoleOrderByPosted_atDesc(userRole.name());
    }

    public List<Post> getEventPosts() {
        return postRepository.findByPostTypeOrderByPosted_atDesc(PostType.EVENT);
    }

    // Optimized method for dashboard - gets recent posts without loading all likes
    public List<Post> getRecentPostsForDashboard(int limit) {
        List<Post> posts = postRepository.findRecentPostsOptimized();
        return posts.size() > limit ? posts.subList(0, limit) : posts;
    }

    // Get like count without loading all user data
    public Long getLikeCount(Long postId) {
        return postRepository.countLikesByPostId(postId);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post likePost(Long postId, User user) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            List<User> likes = post.getUserLikes();
            
            if (likes.contains(user)) {
                likes.remove(user); // Unlike
            } else {
                likes.add(user); // Like
                // Create notification for like
                notificationService.createLikeNotification(post, user);
            }
            
            post.setUserLikes(likes);
            return postRepository.save(post);
        }
        return null;
    }

    public boolean canCreateEvent(User user) {
        return user.getRole() == UserRole.SPONSOR;
    }

    public boolean canCreatePost(User user) {
        return user.getRole() == UserRole.ATHLETE || 
               user.getRole() == UserRole.COACH || 
               user.getRole() == UserRole.SPONSOR;
    }
}
