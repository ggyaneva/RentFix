package app.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import app.model.User;
import app.model.enums.Role;
import app.repository.UserRepository;
import app.web.dto.ProfileUpdateRequest;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("Username already exists");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email already exists");

        User u = new User();
        u.setUsername(request.getUsername());
        u.setEmail(request.getEmail());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setFirstName(request.getFirstName());
        u.setLastName(request.getLastName());
        u.setPhoneNumber(request.getPhoneNumber());
        u.setRole(request.getRole() == null ? Role.TENANT : request.getRole());

        userRepository.save(u);
        log.info("New user registered: {}", u.getUsername());
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void updateProfile(UUID userId, ProfileUpdateRequest req) {
        User u = userRepository.findById(userId).orElseThrow();

        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setPhoneNumber(req.getPhoneNumber());

        userRepository.save(u);
        log.info("Profile updated for user {}", userId);
    }
    @Transactional
    public void changeEmail(UUID userId, String newEmail) {

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("This email is already taken");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);

        log.info("Email updated for user {} -> {}", userId, newEmail);
    }


}

