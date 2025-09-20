package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Enums.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p ORDER BY p.posted_at DESC")
    List<Post> findAllByOrderByPostedAtDesc();

    @Query("SELECT p FROM Post p WHERE p.user = ?1 ORDER BY p.posted_at DESC")
    List<Post> findByUserOrderByPostedAtDesc(User user);

    @Query("SELECT p FROM Post p ORDER BY p.posted_at DESC")
    List<Post> findAllPostsOrderedByDate();

    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', ?1, '%')) ORDER BY p.posted_at DESC")
    List<Post> findByTitleOrDescriptionContainingIgnoreCase(String query);

    @Query("SELECT p FROM Post p WHERE p.postType = ?1 ORDER BY p.posted_at DESC")
    List<Post> findByPostTypeOrderByPostedAtDesc(PostType postType);

    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(CONCAT(p.user.firstName, ' ', p.user.lastName)) LIKE LOWER(CONCAT('%', ?1, '%')) ORDER BY p.posted_at DESC")
    List<Post> findByTitleOrDescriptionOrUserNameContainingIgnoreCase(String query);

    @Query("SELECT p FROM Post p WHERE p.user.id = ?1 ORDER BY p.posted_at DESC")
    List<Post> findByUserIdOrderByPostedAtDesc(Long userId);
}
