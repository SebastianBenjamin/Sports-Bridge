package org.hackcelestial.sportsbridge.Repositories;

import org.hackcelestial.sportsbridge.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);
    User findById(int id);
    User existsByEmail(String email);
    User existsByEmailAndPassword(String email, String password);
    User save(User user);

}
