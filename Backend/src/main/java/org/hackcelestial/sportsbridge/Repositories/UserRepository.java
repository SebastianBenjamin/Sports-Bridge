package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Override
    Optional<User> findById(Integer integer);

    Optional<User> findByEmail(String email);
    Optional<User> existsByEmail(String email);
    Optional<User> existsByEmailAndPassword(String email, String password);
    Optional<User> save(User user);

}
