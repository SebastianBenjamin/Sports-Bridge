package org.hackcelestial.sportsbridge.Api.Entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sb_likes",
       uniqueConstraints = @UniqueConstraint(name = "uk_like_user_post", columnNames = {"user_id", "post_id"}),
       indexes = @Index(name = "idx_like_post", columnList = "post_id"))
public class SbLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SbUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private SbPost post;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SbUser getUser() { return user; }
    public void setUser(SbUser user) { this.user = user; }
    public SbPost getPost() { return post; }
    public void setPost(SbPost post) { this.post = post; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

