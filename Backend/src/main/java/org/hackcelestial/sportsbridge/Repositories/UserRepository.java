package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    // Corrected signatures for Spring Data derived queries
    boolean existsByEmail(String email);
    boolean existsByEmailAndPassword(String email, String password);

    // Finder used for fetching the actual user on login
    User findByEmailAndPassword(String email, String password);

    User save(User user);

}
