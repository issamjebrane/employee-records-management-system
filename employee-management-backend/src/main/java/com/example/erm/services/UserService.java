package com.example.erm.services;

import com.example.erm.entities.User;
import com.example.erm.exceptions.DuplicateResourceException;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public User createUser(User user) {
        // Validate unique constraints
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        // Encode password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        auditService.logActivity(
                "users",
                savedUser.getUserId(),
                "CREATE",
                null,
                savedUser,
                null  // system action
        );

        return savedUser;
    }

    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check unique constraints only if values changed
        if (!existingUser.getUsername().equals(updatedUser.getUsername())
                && userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (!existingUser.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        // Store old state for audit
        User oldState = copyUserState(existingUser);

        // Update fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());

        // Only update password if provided
        if (updatedUser.getPasswordHash() != null && !updatedUser.getPasswordHash().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
        }

        User savedUser = userRepository.save(existingUser);

        auditService.logActivity(
                "users",
                savedUser.getUserId(),
                "UPDATE",
                oldState,
                savedUser,
                null  // system action
        );

        return savedUser;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        userRepository.delete(user);

        auditService.logActivity(
                "users",
                userId,
                "DELETE",
                user,
                null,
                null  // system action
        );
    }

    private User copyUserState(User user) {
        User copy = new User();
        copy.setUserId(user.getUserId());
        copy.setUsername(user.getUsername());
        copy.setEmail(user.getEmail());
        copy.setRole(user.getRole());
        copy.setPasswordHash(user.getPasswordHash());
        copy.setCreatedAt(user.getCreatedAt());
        copy.setLastLogin(user.getLastLogin());
        return copy;
    }
}