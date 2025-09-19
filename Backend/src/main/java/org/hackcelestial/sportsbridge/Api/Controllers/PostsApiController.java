package org.hackcelestial.sportsbridge.Api.Controllers;

import org.hackcelestial.sportsbridge.Api.Entities.*;
import org.hackcelestial.sportsbridge.Api.Repositories.*;
import org.hackcelestial.sportsbridge.Api.Security.CurrentUser;
import org.hackcelestial.sportsbridge.Api.Services.RolePolicyService;
import org.hackcelestial.sportsbridge.Services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostsApiController {

    @Autowired private SbPostRepository postRepository;
    @Autowired private SbPostImageRepository postImageRepository;
    @Autowired private SbLikeRepository likeRepository;
    @Autowired private RolePolicyService rolePolicyService;
    @Autowired private UtilityService utilityService;
    @Autowired(required = false) private JdbcTemplate jdbcTemplate;

    public static class CreatePostResponse { public Long id; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> createPost(@CurrentUser SbUser user,
                                        @RequestParam(required = false) String caption,
                                        @RequestParam(required = false, defaultValue = "PUBLIC") String visibility,
                                        @RequestParam(required = false, name = "images") List<MultipartFile> images) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        if (!rolePolicyService.canPost(user)) return ResponseEntity.status(403).body(Map.of("error", "Role not allowed to post"));

        SbPost post = new SbPost();
        post.setAuthor(user);
        post.setCaption(caption);
        post.setVisibility(visibility);
        post.setLikeCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post = postRepository.save(post);

        if (images != null) {
            for (MultipartFile f : images) {
                if (f == null || f.isEmpty()) continue;
                try {
                    String abs = utilityService.storeFile(f);
                    String url = toWebPath(abs);
                    SbPostImage img = new SbPostImage();
                    img.setPost(post);
                    img.setImageUrl(url);
                    postImageRepository.save(img);
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Failed to store image"));
                }
            }
        }

        mirrorInsertLegacyPosts(caption);

        CreatePostResponse resp = new CreatePostResponse();
        resp.id = post.getId();
        return ResponseEntity.ok(resp);
    }

    private void mirrorInsertLegacyPosts(String caption) {
        try {
            if (jdbcTemplate != null) {
                jdbcTemplate.update("INSERT INTO posts (title, description) VALUES (?, ?)", caption, caption);
            }
        } catch (Exception ignore) { /* best-effort only */ }
    }

    private String toWebPath(String absolutePath) {
        String marker = java.io.File.separator + "AppImages" + java.io.File.separator;
        int idx = absolutePath.lastIndexOf(marker);
        if (idx != -1) {
            String rel = absolutePath.substring(idx + marker.length());
            return "/uploads/" + rel.replace('\\', '/');
        }
        return "/uploads/" + new java.io.File(absolutePath).getName();
    }

    @GetMapping("/feed")
    public ResponseEntity<?> feed(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        Page<SbPost> posts = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), Math.min(50, size)));
        List<Map<String, Object>> items = new ArrayList<>();
        for (SbPost p : posts.getContent()) {
            long likeCount = likeRepository.countByPost(p);
            if (p.getLikeCount() != likeCount) {
                p.setLikeCount(likeCount);
                postRepository.save(p);
            }
            List<SbPostImage> imgs = postImageRepository.findByPost(p);
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", p.getId());
            dto.put("caption", p.getCaption());
            dto.put("imageUrl", p.getImageUrl()); // legacy single-image
            dto.put("images", imgs.stream().map(SbPostImage::getImageUrl).toArray());
            dto.put("createdAt", p.getCreatedAt());
            Map<String, Object> author = new LinkedHashMap<>();
            author.put("id", p.getAuthor() != null ? p.getAuthor().getId() : null);
            author.put("fullName", p.getAuthor() != null ? p.getAuthor().getFullName() : null);
            author.put("role", (p.getAuthor() != null && p.getAuthor().getRole() != null) ? p.getAuthor().getRole().name() : null);
            author.put("profilePicUrl", p.getAuthor() != null ? p.getAuthor().getProfilePicUrl() : null);
            dto.put("author", author);
            dto.put("likeCount", likeCount);
            items.add(dto);
        }
        return ResponseEntity.ok(Map.of(
                "page", posts.getNumber(),
                "size", posts.getSize(),
                "totalPages", posts.getTotalPages(),
                "items", items
        ));
    }

    @PostMapping("/{id}/toggle-like")
    @Transactional
    public ResponseEntity<?> toggleLike(@CurrentUser SbUser user, @PathVariable Long id) {
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        SbPost post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.status(404).body(Map.of("error", "Post not found"));
        if (!rolePolicyService.canLike(user, post)) return ResponseEntity.status(403).body(Map.of("error", "Not allowed to like this post"));

        Optional<SbLike> existing = likeRepository.findByUserAndPost(user, post);
        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            SbLike like = new SbLike();
            like.setUser(user);
            like.setPost(post);
            like.setCreatedAt(LocalDateTime.now());
            likeRepository.save(like);
            liked = true;
        }
        long count = likeRepository.countByPost(post);
        post.setLikeCount(count);
        postRepository.save(post);
        return ResponseEntity.ok(Map.of("liked", liked, "likeCount", count));
    }
}
