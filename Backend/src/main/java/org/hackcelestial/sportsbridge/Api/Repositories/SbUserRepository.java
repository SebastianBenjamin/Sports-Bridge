package org.hackcelestial.sportsbridge.Api.Repositories;

import org.hackcelestial.sportsbridge.Api.Entities.SbUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SbUserRepository extends JpaRepository<SbUser, Long> {
    Optional<SbUser> findByPhone(String phone);
    Optional<SbUser> findByAadhaarHash(String aadhaarHash);
    boolean existsByPhone(String phone);
    boolean existsByAadhaarHash(String aadhaarHash);
}

