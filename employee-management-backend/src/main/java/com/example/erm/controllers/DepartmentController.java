package com.example.erm.controllers;

import com.example.erm.dto.DepartmentDTO;
import com.example.erm.dto.EmployeeMapper;
import com.example.erm.entities.Department;
import com.example.erm.entities.User;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.UserRepository;
import com.example.erm.services.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;
    @Autowired
    public DepartmentController(DepartmentService departmentService, UserRepository userRepository, EmployeeMapper employeeMapper) {
        this.departmentService = departmentService;
        this.userRepository = userRepository;
        this.employeeMapper = employeeMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Department> createDepartment(
            @RequestBody @Valid Department department,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Department created = departmentService.createDepartment(department, currentUser);
        return ResponseEntity
                .created(URI.create("/api/v1/departments/" + created.getDeptId()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update department", description = "Update an existing department (Admin only)")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable("id") Long departmentId,
            @RequestBody @Valid Department department,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Department updated = departmentService.updateDepartment(departmentId, department, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department", description = "Delete a department if it has no employees (Admin only)")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable("id") Long departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        departmentService.deleteDepartment(departmentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get department by ID", description = "Retrieve department details by ID")
    public ResponseEntity<Department> getDepartment(
            @PathVariable("id") Long departmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Department department = departmentService.getDepartment(departmentId, currentUser);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all departments", description = "Retrieve all departments (filtered by user role)")
    public ResponseEntity<List<DepartmentDTO>> getAllDepartments(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<Department> departments = departmentService.getAllDepartments(currentUser);
        List<DepartmentDTO> departmentDTOS = employeeMapper.toDepartmentDTOList(departments);
        return ResponseEntity.ok(departmentDTOS);
    }
}