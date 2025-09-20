package org.hackcelestial.sportsbridge.Controllers;

import jakarta.servlet.http.HttpSession;
import org.hackcelestial.sportsbridge.Enums.PostType;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Services.PostService;
import org.hackcelestial.sportsbridge.Services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    
    @Autowired
    PostService postService;

    @Autowired
    UtilityService utilityService;

    @Autowired
    HttpSession session;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("postType") String postType,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            // Create new post
            Post post = new Post();
            post.setTitle(title);
            post.setDescription(description);
            post.setPostType(PostType.valueOf(postType.toUpperCase()));
            post.setUser(user);

            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imageUrl = utilityService.storeFile(image);
                post.setImageUrl(imageUrl);
            }

            // Save post
            boolean saved = postService.save(post);

            if (saved) {
                response.put("success", true);
                response.put("message", "Post created successfully");
                response.put("post", createPostResponse(post));
            } else {
                response.put("success", false);
                response.put("message", "Failed to create post");
            }

        } catch (Exception e) {
            System.out.println("Error creating post: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred while creating the post");
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            Post post = postService.getPostById(postId);
            if (post == null) {
                response.put("success", false);
                response.put("message", "Post not found");
                return ResponseEntity.status(404).body(response);
            }

            // Check if user owns the post
            if (!post.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "You can only delete your own posts");
                return ResponseEntity.status(403).body(response);
            }

            boolean deleted = postService.deletePost(postId);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Post deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete post");
            }

        } catch (Exception e) {
            System.out.println("Error deleting post: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred while deleting the post");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long postId) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not authenticated");
                return ResponseEntity.status(401).body(response);
            }

            System.out.println("Like toggle request - User ID: " + user.getId() + ", Post ID: " + postId);

            Post post = postService.getPostById(postId);
            if (post == null) {
                response.put("success", false);
                response.put("message", "Post not found");
                return ResponseEntity.status(404).body(response);
            }

            // Log current state before toggle
            int currentLikes = post.getUserLikes() != null ? post.getUserLikes().size() : 0;
            boolean currentlyLiked = post.getUserLikes() != null && post.getUserLikes().contains(user);
            System.out.println("Before toggle - Post " + postId + " has " + currentLikes + " likes, user liked: " + currentlyLiked);

            boolean liked = postService.toggleLike(post, user);

            // Refresh post to get updated state from database
            post = postService.getPostById(postId);
            int newLikesCount = post.getUserLikes() != null ? post.getUserLikes().size() : 0;

            System.out.println("After toggle - Post " + postId + " has " + newLikesCount + " likes, user liked: " + liked);

            response.put("success", true);
            response.put("liked", liked);
            response.put("likesCount", newLikesCount);
            response.put("message", liked ? "Post liked successfully" : "Post unliked successfully");

        } catch (Exception e) {
            System.out.println("Error toggling like: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "An error occurred while processing like");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllPosts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Post> posts = postService.getAllPosts();
            response.put("success", true);
            response.put("posts", posts.stream().map(this::createPostResponse).toList());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching posts");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(@RequestParam("query") String query) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Post> posts;
            if (query == null || query.trim().isEmpty()) {
                // If no query provided, return all posts
                posts = postService.getAllPosts();
            } else {
                // Search posts containing the query in title or description
                posts = postService.searchPosts(query.trim());
            }

            // Convert posts to response format with proper image URLs
            List<Map<String, Object>> postResponses = posts.stream().map(post -> {
                Map<String, Object> postData = createPostResponse(post);

                // Convert image URLs for existing posts
                if (post.getUser() != null && post.getUser().getProfileImageUrl() != null) {
                    postData.put("user", Map.of(
                        "id", post.getUser().getId(),
                        "firstName", post.getUser().getFirstName(),
                        "lastName", post.getUser().getLastName(),
                        "profileImageUrl", utilityService.convertFilePathToWebUrl(post.getUser().getProfileImageUrl()),
                        "role", post.getUser().getRole()
                    ));
                }

                if (post.getImageUrl() != null) {
                    postData.put("imageUrl", utilityService.convertFilePathToWebUrl(post.getImageUrl()));
                }

                return postData;
            }).toList();

            response.put("success", true);
            response.put("posts", postResponses);
            response.put("message", posts.isEmpty() ? "No posts found matching your search." : null);
        } catch (Exception e) {
            System.out.println("Error searching posts: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error searching posts");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterPosts(@RequestParam("type") String type) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Post> posts;
            if ("all".equalsIgnoreCase(type)) {
                // Return all posts
                posts = postService.getAllPosts();
            } else {
                // Filter posts by type
                posts = postService.getPostsByType(PostType.valueOf(type.toUpperCase()));
            }

            // Convert posts to response format with proper image URLs
            List<Map<String, Object>> postResponses = posts.stream().map(post -> {
                Map<String, Object> postData = createPostResponse(post);

                // Convert image URLs for existing posts
                if (post.getUser() != null && post.getUser().getProfileImageUrl() != null) {
                    postData.put("user", Map.of(
                        "id", post.getUser().getId(),
                        "firstName", post.getUser().getFirstName(),
                        "lastName", post.getUser().getLastName(),
                        "profileImageUrl", utilityService.convertFilePathToWebUrl(post.getUser().getProfileImageUrl()),
                        "role", post.getUser().getRole()
                    ));
                }

                if (post.getImageUrl() != null) {
                    postData.put("imageUrl", utilityService.convertFilePathToWebUrl(post.getImageUrl()));
                }

                return postData;
            }).toList();

            response.put("success", true);
            response.put("posts", postResponses);
            response.put("message", posts.isEmpty() ? "No posts found for this category." : null);
        } catch (Exception e) {
            System.out.println("Error filtering posts: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error filtering posts");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-with-profiles")
    public ResponseEntity<Map<String, Object>> searchPostsAndProfiles(@RequestParam String query) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Post> posts = postService.searchPostsAndProfiles(query);
            List<Map<String, Object>> postResponses = posts.stream()
                    .map(this::createPostResponse)
                    .toList();

            response.put("success", true);
            response.put("posts", postResponses);
            response.put("message", posts.isEmpty() ? "No posts or profiles found matching your search" : null);

        } catch (Exception e) {
            System.out.println("Error searching posts and profiles: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error searching posts and profiles");
        }

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createPostResponse(Post post) {
        Map<String, Object> postData = new HashMap<>();
        postData.put("id", post.getId());
        postData.put("title", post.getTitle());
        postData.put("description", post.getDescription());
        postData.put("postType", post.getPostType());
        postData.put("imageUrl", post.getImageUrl());
        postData.put("postedAt", post.getPosted_at());

        // User information
        if (post.getUser() != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("firstName", post.getUser().getFirstName());
            userData.put("lastName", post.getUser().getLastName());
            userData.put("profileImageUrl", post.getUser().getProfileImageUrl());
            userData.put("role", post.getUser().getRole());
            postData.put("user", userData);
        }

        return postData;
    }
}
