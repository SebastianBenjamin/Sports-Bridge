package org.hackcelestial.sportsbridge.Api.Controllers;

import org.hackcelestial.sportsbridge.Api.Entities.*;
import org.hackcelestial.sportsbridge.Api.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UsersApiController {

    @Autowired private SbUserRepository userRepository;
    @Autowired private SbPostRepository postRepository;
    @Autowired private SbPostImageRepository postImageRepository;
    @Autowired private SbLikeRepository likeRepository;

    @GetMapping("/{id}/profile")
    public ResponseEntity<?> profile(@PathVariable Long id) {
        SbUser user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", user.getId());
        dto.put("fullName", user.getFullName());
        dto.put("role", user.getRole().name());
        dto.put("profilePicUrl", user.getProfilePicUrl());
        dto.put("phone", user.getPhone());

        List<SbPost> posts = postRepository.findAll(); // simplistic; ideally filter by author id
        List<Map<String, Object>> myPosts = new ArrayList<>();
        for (SbPost p : posts) {
            if (!p.getAuthor().getId().equals(user.getId())) continue;
            long likeCount = likeRepository.countByPost(p);
            List<SbPostImage> imgs = postImageRepository.findByPost(p);
            Map<String, Object> postDto = new LinkedHashMap<>();
            postDto.put("id", p.getId());
            postDto.put("caption", p.getCaption());
            postDto.put("images", imgs.stream().map(SbPostImage::getImageUrl).toArray());
            postDto.put("createdAt", p.getCreatedAt());
            postDto.put("likeCount", likeCount);
            myPosts.add(postDto);
        }
        dto.put("posts", myPosts);
        return ResponseEntity.ok(dto);
    }
}

