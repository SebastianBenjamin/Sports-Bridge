package org.hackcelestial.sportsbridge.Api.Repositories;

import org.hackcelestial.sportsbridge.Api.Entities.SbPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SbPostRepository extends JpaRepository<SbPost, Long> {
    Page<SbPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

