package com.example.erm.dto;

import com.example.erm.entities.EmployeeStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeSearchCriteria {
    private Long employeeId;
    private String searchTerm;
    private Long departmentId;
    private String jobTitle;
    private EmployeeStatus status;
    private LocalDate hireDateStart;
    private LocalDate hireDateEnd;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}