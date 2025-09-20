package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    // Corrected signatures for Spring Data derived queries
    boolean existsByEmail(String email);
    boolean existsByEmailAndPassword(String email, String password);

    // Finder used for fetching the actual user on login
    User findByEmailAndPassword(String email, String password);

    // NEW: find legacy user by phone to sync OTP signups into legacy table
    User findByPhone(String phone);
}
