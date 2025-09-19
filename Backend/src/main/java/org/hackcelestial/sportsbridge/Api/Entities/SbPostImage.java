package org.hackcelestial.sportsbridge.Api.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "sb_post_images")
public class SbPostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private SbPost post;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SbPost getPost() { return post; }
    public void setPost(SbPost post) { this.post = post; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

