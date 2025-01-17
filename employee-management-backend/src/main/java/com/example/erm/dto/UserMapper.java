package com.example.erm.dto;

import com.example.erm.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    public <T> User toEntity(T dto) {
        User user = new User();
        if(dto instanceof UserCreateDTO createDTO) {
            user.setUsername(createDTO.getUsername());
            user.setPasswordHash(passwordEncoder.encode(createDTO.getPassword()));
            user.setEmail(createDTO.getEmail());
            user.setRole(createDTO.getRole());
        } else if(dto instanceof UserDTO userDTO) {
            user.setUsername(userDTO.getUsername());
            user.setPasswordHash(userDTO.getPassword());
            user.setEmail(userDTO.getEmail());
            user.setRole(userDTO.getRole());
        }
        return user;
    }

    public User toEntity(UserUpdateDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPasswordHash(dto.getPassword()); // Will be encoded in service
        }
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        return user;
    }

    public UserResponseDTO toResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}