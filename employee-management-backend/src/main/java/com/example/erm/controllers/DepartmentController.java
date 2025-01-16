package com.example.erm.controllers;

import com.example.erm.entities.Department;
import com.example.erm.entities.User;
import com.example.erm.services.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Department Management", description = "APIs for managing departments")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {
    private final DepartmentService departmentService;

    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new department", description = "Create a new department (Admin only)")
    public ResponseEntity<Department> createDepartment(
            @RequestBody @Valid Department department,
            @AuthenticationPrincipal User currentUser) {
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
            @AuthenticationPrincipal User currentUser) {
        Department updated = departmentService.updateDepartment(departmentId, department, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department", description = "Delete a department if it has no employees (Admin only)")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable("id") Long departmentId,
            @AuthenticationPrincipal User currentUser) {
        departmentService.deleteDepartment(departmentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get department by ID", description = "Retrieve department details by ID")
    public ResponseEntity<Department> getDepartment(
            @PathVariable("id") Long departmentId,
            @AuthenticationPrincipal User currentUser) {
        Department department = departmentService.getDepartment(departmentId, currentUser);
        return ResponseEntity.ok(department);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(summary = "Get all departments", description = "Retrieve all departments (filtered by user role)")
    public ResponseEntity<List<Department>> getAllDepartments(@AuthenticationPrincipal User currentUser) {
        List<Department> departments = departmentService.getAllDepartments(currentUser);
        return ResponseEntity.ok(departments);
    }
}