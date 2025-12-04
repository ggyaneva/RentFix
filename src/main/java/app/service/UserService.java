package app.service;

import app.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import app.model.User;
import app.model.enums.Role;
import app.repository.UserRepository;
import app.web.dto.ProfileUpdateRequest;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
    @CacheEvict(value = "user", allEntries = true)
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("Username already exists");

        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email already exists");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole() == null ? Role.TENANT : request.getRole());

        userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());
    }

    @Cacheable("user")
    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public void updateProfile(UUID userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId).orElseThrow();

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhoneNumber(req.getPhoneNumber());

        userRepository.save(user);
        log.info("Profile updated for user {}", userId);
    }
    @Transactional
    @CacheEvict(value = "user", allEntries = true)
    public void changeEmail(UUID userId, String newEmail) {

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("This email is already taken");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmail(newEmail);
        userRepository.save(user);

        log.info("Email updated for user {} -> {}", userId, newEmail);
    }

    @Cacheable("user")
    public List<User> getAllByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<User> result = new ArrayList<>();
        userRepository.findAllById(ids).forEach(result::add);
        return result;
    }


}

