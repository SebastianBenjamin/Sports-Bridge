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
import java.util.stream.Collectors;

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
    public ResponseEntity<?> feed(@CurrentUser SbUser currentUser,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        try {
            Page<SbPost> posts = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), Math.min(50, size)));
            List<Map<String, Object>> items = new ArrayList<>();
            for (SbPost p : posts.getContent()) {
                long likeCount = likeRepository.countByPost(p);
                if (p.getLikeCount() != likeCount) {
                    p.setLikeCount(likeCount);
                    postRepository.save(p);
                }
                boolean likedByCurrent = currentUser != null && likeRepository.existsByUserAndPost(currentUser, p);
                List<SbPostImage> imgs = postImageRepository.findByPost(p);
                Map<String, Object> dto = new LinkedHashMap<>();
                dto.put("id", p.getId());
                dto.put("caption", p.getCaption());
                dto.put("created_at", p.getCreatedAt());
                Map<String, Object> author = new LinkedHashMap<>();
                author.put("id", p.getAuthor() != null ? p.getAuthor().getId() : null);
                author.put("name", p.getAuthor() != null ? p.getAuthor().getFullName() : null);
                author.put("profilePicUrl", p.getAuthor() != null ? p.getAuthor().getProfilePicUrl() : null);
                dto.put("user", author);
                dto.put("images", imgs.stream().map(SbPostImage::getImageUrl).toArray(String[]::new));
                dto.put("like_count", likeCount);
                dto.put("liked_by_current_user", likedByCurrent);
                items.add(dto);
            }
            // If JPA returned nothing but JDBC is available, try a JDBC fallback to cover mapping issues
            if (items.isEmpty() && jdbcTemplate != null) {
                try {
                    return ResponseEntity.ok(jdbcFeedFallback(currentUser, page, size, false)); // prefer author_id
                } catch (Exception tryUserId) {
                    try {
                        return ResponseEntity.ok(jdbcFeedFallback(currentUser, page, size, true)); // fall back to user_id
                    } catch (Exception finalFail) {
                        // ignore and return empty list below
                    }
                }
            }
            return ResponseEntity.ok(items);
        } catch (Exception ex) {
            // JDBC fallback for schema variance (user_id vs author_id) or lazy mapping issues
            if (jdbcTemplate == null) {
                return ResponseEntity.status(500).body(Map.of("error", "Feed unavailable"));
            }
            try {
                return ResponseEntity.ok(jdbcFeedFallback(currentUser, page, size, true));
            } catch (Exception tryAuthorId) {
                try {
                    return ResponseEntity.ok(jdbcFeedFallback(currentUser, page, size, false));
                } catch (Exception finalFail) {
                    return ResponseEntity.status(500).body(Map.of("error", "Feed unavailable"));
                }
            }
        }
    }

    private List<Map<String, Object>> jdbcFeedFallback(SbUser currentUser, int page, int size, boolean userIdFirst) {
        int p = Math.max(0, page);
        int s = Math.min(50, size);
        String joinCol = userIdFirst ? "user_id" : "author_id";
        String sql = "SELECT p.id, p.caption, p.created_at, u.id AS uid, u.full_name, u.profile_pic_url " +
                "FROM sb_posts p LEFT JOIN sb_users u ON u.id = p." + joinCol + " ORDER BY p.created_at DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = jdbcTemplate.query(sql, (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getLong("id"));
            m.put("caption", rs.getString("caption"));
            m.put("created_at", rs.getTimestamp("created_at"));
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("id", rs.getObject("uid") != null ? rs.getLong("uid") : null);
            user.put("name", rs.getString("full_name"));
            user.put("profilePicUrl", rs.getString("profile_pic_url"));
            m.put("user", user);
            return m;
        }, s, p * s);
        if (rows.isEmpty()) return rows;
        // batch fetch images
        List<Long> ids = rows.stream().map(x -> ((Number)x.get("id")).longValue()).collect(Collectors.toList());
        String inClause = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        Map<Long, List<String>> images = new HashMap<>();
        List<Map<String, Object>> imgRows = jdbcTemplate.queryForList(
                "SELECT post_id, image_url FROM sb_post_images WHERE post_id IN (" + inClause + ")",
                ids.toArray()
        );
        for (Map<String, Object> r : imgRows) {
            long pid = ((Number) r.get("post_id")).longValue();
            String url = (String) r.get("image_url");
            images.computeIfAbsent(pid, k -> new ArrayList<>()).add(url);
        }
        // batch like counts
        Map<Long, Long> likeCounts = new HashMap<>();
        List<Map<String, Object>> lcRows = jdbcTemplate.queryForList(
                "SELECT post_id, COUNT(*) AS c FROM sb_likes WHERE post_id IN (" + inClause + ") GROUP BY post_id",
                ids.toArray()
        );
        for (Map<String, Object> r : lcRows) {
            long pid = ((Number) r.get("post_id")).longValue();
            long c = ((Number) r.get("c")).longValue();
            likeCounts.put(pid, c);
        }
        for (Map<String, Object> m : rows) {
            long pid = ((Number)m.get("id")).longValue();
            m.put("images", images.getOrDefault(pid, List.of()));
            m.put("like_count", likeCounts.getOrDefault(pid, 0L));
            m.put("liked_by_current_user", false);
        }
        return rows;
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

    // Alias endpoint matching spec: POST /api/posts/{postId}/like
    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<?> likeAlias(@CurrentUser SbUser user, @PathVariable("id") Long id) {
        return toggleLike(user, id);
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debug() {
        if (jdbcTemplate == null) return ResponseEntity.status(501).body(Map.of("error", "JDBC not available"));
        try {
            Map<String, Object> out = new LinkedHashMap<>();
            Long postsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sb_posts", Long.class);
            out.put("postsCount", postsCount);
            Integer nullAuthorCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sb_posts WHERE author_id IS NULL", Integer.class);
            out.put("nullAuthorCount", nullAuthorCount);
            List<Map<String, Object>> posts = jdbcTemplate.queryForList(
                    "SELECT id, caption, created_at, author_id, user_id FROM sb_posts ORDER BY created_at DESC LIMIT 5"
            );
            out.put("posts", posts);
            List<Map<String, Object>> imagesPerPost = jdbcTemplate.queryForList(
                    "SELECT post_id, COUNT(*) AS c FROM sb_post_images GROUP BY post_id ORDER BY post_id DESC LIMIT 10"
            );
            out.put("imagesPerPost", imagesPerPost);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
