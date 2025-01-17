package com.example.erm.controllers;

import com.example.erm.dto.*;
import com.example.erm.entities.User;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.UserRepository;
import com.example.erm.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserCreateDTO userDTO,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User user = userMapper.toEntity(userDTO);
        User createdUser = userService.createUser(user);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + createdUser.getUserId()))
                .body(userMapper.toResponseDTO(createdUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user", description = "Update an existing user")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserUpdateDTO userDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        User user = userMapper.toEntity(userDTO);
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponseDTO(updatedUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user", description = "Get user by ID")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponseDTO(user));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    //login user
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Login a user")
    public ResponseEntity<UserResponseDTO> loginUser(@RequestBody @Valid UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        User loginUser = userService.loginUser(user);
        return ResponseEntity.ok(userMapper.toResponseDTO(loginUser));
    }

    //get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get all users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users.stream().map(userMapper::toResponseDTO).collect(Collectors.toList()));
    }
}
