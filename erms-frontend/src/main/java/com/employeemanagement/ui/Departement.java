package com.employeemanagement.ui;

import lombok.Data;

import java.util.List;

@Data
public class Departement {
    private Long deptId;
    private String deptName;
    private String createdAt;
    private String updatedAt;
    private List<Employee> employees;
    private List<User> users;
}


