package com.employeemanagement.ui;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String hireDate;
    private String jobTitle;
    private Long departmentId;
    private BigDecimal salary;
    private String status;
}