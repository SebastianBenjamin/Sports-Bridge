package org.hackcelestial.sportsbridge.Models;

import jakarta.persistence.*;
import org.hackcelestial.sportsbridge.Enums.PostType;

import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title, description;
    @Enumerated(EnumType.STRING)
    private PostType postType;

    @ManyToOne
    private User user;
    @ManyToMany
    private List<User> userLikes;

}
