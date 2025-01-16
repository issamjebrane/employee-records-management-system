package com.example.erm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DepartmentDTO {
    private Long deptId;

    @NotBlank(message = "Department name is required")
    private String deptName;

    private LocalDateTime createdAt;
    private Integer employeeCount;
    private BigDecimal averageSalary;
    // Only include employee list when specifically needed
    private List<EmployeeDTO> employees;
}