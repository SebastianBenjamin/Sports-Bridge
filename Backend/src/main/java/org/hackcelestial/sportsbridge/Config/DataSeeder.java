package org.hackcelestial.sportsbridge.Config;

import org.hackcelestial.sportsbridge.Api.Entities.*;
import org.hackcelestial.sportsbridge.Api.Repositories.*;
import org.hackcelestial.sportsbridge.Enums.ApiUserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Profile("dev")
public class DataSeeder {

    @Autowired private SbUserRepository userRepository;
    @Autowired private SbPostRepository postRepository;
    @Autowired private SbPostImageRepository postImageRepository;
    @Autowired private SbLikeRepository likeRepository;

    private static final String UPLOAD_ROOT = "AppImages";

    @PostConstruct
    public void seed() {
        // Only seed when there are no posts
        if (postRepository.count() > 0) return;

        // 1) Create demo users
        SbUser benji = createUser("Benji Athlete", ApiUserRole.ATHLETE, "+911111111111", "benji@sports.test");
        SbUser akshay = createUser("Akshay Coach", ApiUserRole.COACH, "+912222222222", "akshay@sports.test");
        SbUser acme = createUser("Acme Sponsor", ApiUserRole.SPONSOR, "+913333333333", "sponsor@sports.test");

        // 2) Collect some image URLs from AppImages (if available)
        List<String> sampleImages = scanUploadImages(5);

        // 3) Create posts
        List<SbPost> posts = new ArrayList<>();
        posts.add(createPost(benji, "Great morning training session!", LocalDateTime.now().minusHours(2)));
        posts.add(createPost(akshay, "Proud of the team's progress this week.", LocalDateTime.now().minusHours(5)));
        posts.add(createPost(acme, "Excited to support upcoming tournaments!", LocalDateTime.now().minusDays(1)));

        // 4) Attach images (1-2 per post if available)
        int idx = 0;
        for (SbPost p : posts) {
            for (int k = 0; k < 2 && idx < sampleImages.size(); k++, idx++) {
                SbPostImage img = new SbPostImage();
                img.setPost(p);
                img.setImageUrl(sampleImages.get(idx));
                postImageRepository.save(img);
            }
        }

        // 5) Likes: others like each post
        like(posts.get(0), akshay);
        like(posts.get(0), acme);
        like(posts.get(1), benji);
        like(posts.get(2), benji);
        like(posts.get(2), akshay);

        // 6) Update like counts on posts
        for (SbPost p : posts) {
            long c = likeRepository.countByPost(p);
            p.setLikeCount(c);
            postRepository.save(p);
        }
    }

    @PostConstruct
    public void seedEdgeCases() {
        try {
            // Ensure we have a pool of images
            List<String> imgs = scanUploadImages(10);
            if (imgs.isEmpty()) {
                // Fallback to placeholder in static if AppImages empty
                imgs = List.of("/uploads/placeholder.jpg");
            }

            // Create additional users for edge cases
            SbUser maxImgUser = ensureUserIfMissing("Max Img", ApiUserRole.ATHLETE, "+919900000201", "max.img@dev.local", true);
            SbUser emptyCapUser = ensureUserIfMissing("Empty Cap", ApiUserRole.ATHLETE, "+919900000202", "empty.cap@dev.local", true);
            SbUser longCapUser = ensureUserIfMissing("Long Cap", ApiUserRole.ATHLETE, "+919900000203", "long.cap@dev.local", true);
            SbUser likerUser = ensureUserIfMissing("Liker All", ApiUserRole.USER, "+919900000204", "liker.all@dev.local", true);
            SbUser zeroLikeUser = ensureUserIfMissing("Zero Like", ApiUserRole.USER, "+919900000205", "zero.like@dev.local", true);
            SbUser unverifiedSponsor = ensureUserIfMissing("Unverified Sponsor", ApiUserRole.SPONSOR, "+919900000206", "unsponsor@dev.local", false);

            // Posts: many images
            ensurePostWithImages(maxImgUser, "EDGE_MANY_IMAGES", 5, imgs);
            // Posts: empty caption + single image
            ensurePostWithImages(emptyCapUser, "", 1, imgs);
            // Posts: max-length caption (250 chars per API policy)
            String maxCap = "X".repeat(250);
            ensurePostWithCaption(longCapUser, maxCap);
            // Additional normal posts
            ensurePostWithCaption(maxImgUser, "Short run and stretch");
            ensurePostWithImages(maxImgUser, "Trail pics", 3, imgs);

            // Likes extremes: likerUser likes all posts; zeroLikeUser likes none
            List<SbPost> all = postRepository.findAll();
            for (SbPost p : all) {
                if (!likeRepository.existsByUserAndPost(likerUser, p)) {
                    like(p, likerUser);
                }
            }
            // Recompute like counts
            for (SbPost p : all) {
                long c = likeRepository.countByPost(p);
                if (p.getLikeCount() != c) { p.setLikeCount(c); postRepository.save(p); }
            }
        } catch (Exception ignore) {
            // do not fail startup; best-effort seeding
        }
    }

    private SbUser createUser(String name, ApiUserRole role, String phone, String email) {
        // Create minimal valid user matching non-null constraints
        SbUser u = new SbUser();
        u.setFullName(name);
        u.setRole(role);
        u.setPhone(phone);
        u.setEmail(email);
        u.setVerified(true);
        u.setCreatedAt(LocalDateTime.now());
        // Dummy Aadhaar values to satisfy NOT NULL constraints
        byte[] enc = ("enc-" + phone).getBytes();
        u.setAadhaarEncrypted(enc);
        u.setAadhaarHash(sha256Hex("seed-" + phone));
        // Optional profile pic (use first upload image if present)
        List<String> imgs = scanUploadImages(1);
        if (!imgs.isEmpty()) u.setProfilePicUrl(imgs.get(0));
        return userRepository.save(u);
    }

    private SbPost createPost(SbUser author, String caption, LocalDateTime when) {
        SbPost p = new SbPost();
        p.setAuthor(author);
        p.setCaption(caption);
        p.setVisibility("PUBLIC");
        p.setLikeCount(0);
        p.setCreatedAt(when);
        return postRepository.save(p);
    }

    private void like(SbPost post, SbUser by) {
        SbLike l = new SbLike();
        l.setPost(post);
        l.setUser(by);
        l.setCreatedAt(LocalDateTime.now());
        likeRepository.save(l);
    }

    private List<String> scanUploadImages(int max) {
        List<String> out = new ArrayList<>();
        File root = new File(UPLOAD_ROOT);
        if (!root.exists() || !root.isDirectory()) return out;
        scanRec(root, out, max);
        return out;
    }

    private void scanRec(File dir, List<String> out, int max) {
        if (out.size() >= max) return;
        File[] arr = dir.listFiles();
        if (arr == null) return;
        for (File f : arr) {
            if (out.size() >= max) return;
            if (f.isDirectory()) { scanRec(f, out, max); }
            else if (isImage(f.getName())) {
                String web = toWebPath(f.getAbsolutePath());
                out.add(web);
            }
        }
    }

    private boolean isImage(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif") || n.endsWith(".avif");
    }

    private String toWebPath(String abs) {
        String marker = File.separator + UPLOAD_ROOT + File.separator;
        int idx = abs.lastIndexOf(marker);
        if (idx != -1) {
            String rel = abs.substring(idx + marker.length()).replace('\\', '/');
            return "/uploads/" + rel;
        }
        return "/uploads/" + new File(abs).getName();
    }

    private String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return UUID.randomUUID().toString().replace("-", ""); }
    }

    private SbUser ensureUserIfMissing(String name, ApiUserRole role, String phone, String email, boolean verified) {
        return userRepository.findByPhone(phone).orElseGet(() -> {
            SbUser u = new SbUser();
            u.setFullName(name);
            u.setRole(role);
            u.setPhone(phone);
            u.setEmail(email);
            u.setVerified(verified);
            u.setCreatedAt(LocalDateTime.now());
            // Minimal aadhaar fields
            byte[] enc = ("enc-" + phone).getBytes();
            u.setAadhaarEncrypted(enc);
            u.setAadhaarHash(sha256Hex("seed-" + phone));
            return userRepository.save(u);
        });
    }

    private void ensurePostWithImages(SbUser author, String caption, int imageCount, List<String> pool) {
        // Check if a post with same caption by this author exists (use caption marker)
        boolean exists = postRepository.findAll().stream().anyMatch(p ->
                Objects.equals(p.getAuthor() != null ? p.getAuthor().getId() : null, author.getId()) &&
                        Objects.equals(Objects.toString(p.getCaption(), ""), Objects.toString(caption, ""))
        );
        if (exists) return;
        SbPost p = createPost(author, caption, LocalDateTime.now().minusMinutes(new Random().nextInt(120)));
        for (int i = 0; i < imageCount; i++) {
            String url = pool.get(Math.min(i, pool.size()-1));
            SbPostImage img = new SbPostImage();
            img.setPost(p);
            img.setImageUrl(url);
            postImageRepository.save(img);
        }
    }

    private void ensurePostWithCaption(SbUser author, String caption) {
        boolean exists = postRepository.findAll().stream().anyMatch(p ->
                Objects.equals(p.getAuthor() != null ? p.getAuthor().getId() : null, author.getId()) &&
                        Objects.equals(Objects.toString(p.getCaption(), ""), Objects.toString(caption, ""))
        );
        if (exists) return;
        createPost(author, caption, LocalDateTime.now().minusMinutes(new Random().nextInt(120)));
    }
}
