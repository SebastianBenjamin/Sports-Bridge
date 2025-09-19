package org.hackcelestial.sportsbridge.Api.Repositories;

import org.hackcelestial.sportsbridge.Api.Entities.SbLike;
import org.hackcelestial.sportsbridge.Api.Entities.SbPost;
import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SbLikeRepository extends JpaRepository<SbLike, Long> {
    long countByPost(SbPost post);
    Optional<SbLike> findByUserAndPost(SbUser user, SbPost post);
    boolean existsByUserAndPost(SbUser user, SbPost post);
}

