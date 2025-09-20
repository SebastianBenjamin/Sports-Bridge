package org.hackcelestial.sportsbridge.Services;

import org.hackcelestial.sportsbridge.Models.User;
import org.hackcelestial.sportsbridge.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean userExists(String email, String password) {
        return userRepository.existsByEmailAndPassword(email,password);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean save(User user) {
        return userRepository.save(user)!=null;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean updateUser(User user) {
        try {
            return userRepository.save(user) != null;
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
}
