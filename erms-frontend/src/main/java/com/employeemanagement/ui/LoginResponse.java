package com.employeemanagement.ui;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Data
public class LoginResponse {
    // Getters and setters required for JSON deserialization
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String createdAt;
    private String lastLogin;

}
