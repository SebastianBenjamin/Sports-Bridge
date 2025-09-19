package org.hackcelestial.sportsbridge.Api.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sb_posts", indexes = {
        @Index(name = "idx_sb_post_created_at", columnList = "created_at DESC")
})
public class SbPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SbUser author;

    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "visibility", length = 32)
    private String visibility; // PUBLIC by default

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SbUser getAuthor() { return author; }
    public void setAuthor(SbUser author) { this.author = author; }
    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getLikeCount() { return likeCount; }
    public void setLikeCount(long likeCount) { this.likeCount = likeCount; }
}
