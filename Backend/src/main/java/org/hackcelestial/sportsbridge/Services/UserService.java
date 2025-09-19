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
        return userRepository.existsByEmail(email).get()!=null;
    }
    public boolean userExists(String email, String password) {
        return userRepository.existsByEmailAndPassword(email,password).get()!=null;
    }
    public boolean save(User user) {
        return userRepository.save(user)!=null;
    }
}
