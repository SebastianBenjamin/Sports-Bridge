package org.hackcelestial.sportsbridge.Api.Repositories;

import org.hackcelestial.sportsbridge.Api.Entities.SbPost;
import org.hackcelestial.sportsbridge.Api.Entities.SbPostImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SbPostImageRepository extends JpaRepository<SbPostImage, Long> {
    List<SbPostImage> findByPost(SbPost post);
}

