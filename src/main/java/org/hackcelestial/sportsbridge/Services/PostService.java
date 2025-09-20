package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Enums.PostType;
import org.hackcelestial.sportsbridge.Repositories.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    @Autowired
    PostRepository postRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public boolean save(Post post) {
        try {
            post.setPosted_at(LocalDateTime.now());
            return postRepository.save(post) != null;
        } catch (Exception e) {
            System.out.println("Error saving post: " + e.getMessage());
            return false;
        }
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllPostsOrderedByDate();
    }

    public List<Post> getPostsByUser(User user) {
        return postRepository.findByUserOrderByPostedAtDesc(user);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Transactional
    public boolean deletePost(Long postId) {
        try {
            // First, get the post to ensure it exists
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                System.out.println("Post not found: " + postId);
                return false;
            }

            System.out.println("Attempting to delete post " + postId + " with " +
                (post.getUserLikes() != null ? post.getUserLikes().size() : 0) + " likes");

            // Method 1: Clear likes using JPA relationship
            if (post.getUserLikes() != null && !post.getUserLikes().isEmpty()) {
                int likesCount = post.getUserLikes().size();
                post.getUserLikes().clear();
                postRepository.save(post);
                entityManager.flush(); // Force the database update using EntityManager
                System.out.println("Cleared " + likesCount + " likes from post " + postId + " using JPA");
            }

            // Method 2: Backup - Direct SQL delete for any remaining likes
            try {
                int deletedLikes = entityManager.createNativeQuery(
                    "DELETE FROM post_likes WHERE post_id = :postId")
                    .setParameter("postId", postId)
                    .executeUpdate();
                if (deletedLikes > 0) {
                    System.out.println("Removed " + deletedLikes + " remaining likes using direct SQL");
                }
            } catch (Exception sqlEx) {
                System.out.println("Warning: Could not delete likes using SQL (may not exist): " + sqlEx.getMessage());
            }

            // Force synchronization before deletion
            entityManager.flush();
            entityManager.clear();

            // Now delete the post
            postRepository.deleteById(postId);
            System.out.println("Post deleted successfully: " + postId);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting post: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean toggleLike(Post post, User user) {
        try {
            // Initialize userLikes list if null
            if (post.getUserLikes() == null) {
                post.setUserLikes(new ArrayList<>());
            }

            boolean wasLiked = post.getUserLikes().contains(user);

            if (wasLiked) {
                // Remove like (unlike)
                post.getUserLikes().remove(user);
                System.out.println("User " + user.getId() + " unliked post " + post.getId());
            } else {
                // Add like
                post.getUserLikes().add(user);
                System.out.println("User " + user.getId() + " liked post " + post.getId());
            }

            // Save the post with updated likes
            Post savedPost = postRepository.save(post);

            // Verify the save was successful
            if (savedPost != null) {
                System.out.println("Post " + post.getId() + " saved successfully with " +
                    (savedPost.getUserLikes() != null ? savedPost.getUserLikes().size() : 0) + " likes");
                return !wasLiked; // Return true if now liked, false if unliked
            } else {
                System.out.println("Error: Failed to save post " + post.getId());
                return false;
            }

        } catch (Exception e) {
            System.out.println("Error toggling like for post " + post.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to toggle like", e);
        }
    }

    public List<Post> searchPosts(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return getAllPosts();
            }
            return postRepository.findByTitleOrDescriptionContainingIgnoreCase(query);
        } catch (Exception e) {
            System.out.println("Error searching posts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Post> getPostsByType(org.hackcelestial.sportsbridge.Enums.PostType postType) {
        try {
            return postRepository.findByPostTypeOrderByPostedAtDesc(postType);
        } catch (Exception e) {
            System.out.println("Error filtering posts by type: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
