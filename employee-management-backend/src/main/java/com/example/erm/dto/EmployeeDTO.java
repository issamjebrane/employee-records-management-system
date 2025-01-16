package com.example.erm.dto;

import com.example.erm.entities.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeDTO {
    private Long empId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotNull(message = "Department is required")
    private Long departmentId;

    private Long managerId;
    private BigDecimal salary;
    private EmployeeStatus status;
}
