package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Enums.PostType;
import org.hackcelestial.sportsbridge.Models.Post;
import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.userLikes ORDER BY p.posted_at DESC")
    List<Post> findAllByOrderByPosted_atDesc();

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.userLikes WHERE p.user = :user ORDER BY p.posted_at DESC")
    List<Post> findByUserOrderByPosted_atDesc(@Param("user") User user);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.userLikes WHERE p.postType = :postType ORDER BY p.posted_at DESC")
    List<Post> findByPostTypeOrderByPosted_atDesc(@Param("postType") PostType postType);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.userLikes WHERE p.postType = :postType AND p.user.role = :userRole ORDER BY p.posted_at DESC")
    List<Post> findByPostTypeAndUserRoleOrderByPosted_atDesc(@Param("postType") PostType postType, @Param("userRole") String userRole);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.userLikes WHERE p.user.role = :userRole ORDER BY p.posted_at DESC")
    List<Post> findByUserRoleOrderByPosted_atDesc(@Param("userRole") String userRole);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.userLikes WHERE p.postType = :postType AND p.user = :user ORDER BY p.posted_at DESC")
    List<Post> findByPostTypeAndUserOrderByPosted_atDesc(@Param("postType") PostType postType, @Param("user") User user);

    // Optimized query for dashboard - only fetch recent posts with minimal data
    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.posted_at DESC")
    List<Post> findRecentPostsOptimized();

    // Count likes without fetching all user data
    @Query("SELECT COUNT(ul) FROM Post p JOIN p.userLikes ul WHERE p.id = :postId")
    Long countLikesByPostId(@Param("postId") Long postId);
}
