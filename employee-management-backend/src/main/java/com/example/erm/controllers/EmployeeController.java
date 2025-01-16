package com.example.erm.controllers;

import com.example.erm.dto.EmployeeDTO;
import com.example.erm.dto.EmployeeMapper;
import com.example.erm.dto.EmployeeResponseDTO;
import com.example.erm.dto.EmployeeSearchCriteria;
import com.example.erm.entities.Employee;
import com.example.erm.entities.EmployeeStatus;
import com.example.erm.entities.User;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.UserRepository;
import com.example.erm.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final EmployeeMapper employeeMapper;
    private final UserRepository userRepository;
    @Autowired
    public EmployeeController(EmployeeService employeeService, EmployeeMapper employeeMapper, UserRepository userRepository) {
        this.employeeService = employeeService;
        this.employeeMapper = employeeMapper;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create employee", description = "Create a new employee")
    public ResponseEntity<Employee> createEmployee(
            @RequestBody @Valid EmployeeDTO employeeDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = employeeService.createEmployee(
                employeeMapper.toEntity(employeeDTO),
                currentUser
        );
        return ResponseEntity
                .created(URI.create("/api/v1/employees/" + employee.getEmpId()))
                .body(employee);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(
            summary = "Get employee by ID",
            description = "Retrieve detailed information about an employee"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved employee details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Employee not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - insufficient permissions"
            )
    })
    public ResponseEntity<EmployeeResponseDTO> getEmployee(
            @PathVariable("id") Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Employee employee = employeeService.getEmployee(employeeId, currentUser);
        return ResponseEntity.ok(employeeMapper.toResponseDTO(employee));
    }
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER')")
    @Operation(
            summary = "Search employees",
            description = "Search and filter employees by various criteria with pagination"
    )
    public ResponseEntity<Page<EmployeeResponseDTO>> searchEmployees(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hireDateStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate hireDateEnd,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "empId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @AuthenticationPrincipal User currentUser
    ) {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setEmployeeId(employeeId);
        criteria.setSearchTerm(searchTerm);
        criteria.setDepartmentId(departmentId);
        criteria.setJobTitle(jobTitle);
        criteria.setStatus(status);
        criteria.setHireDateStart(hireDateStart);
        criteria.setHireDateEnd(hireDateEnd);
        criteria.setPage(page);
        criteria.setSize(size);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDirection);

        Page<Employee> employees = employeeService.searchEmployees(criteria, currentUser);

        return ResponseEntity.ok(employees.map(employeeMapper::toResponseDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(
            summary = "Delete employee",
            description = "Soft delete employee by setting status to INACTIVE"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Employee successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Employee not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - insufficient permissions"),
            @ApiResponse(responseCode = "400", description = "Invalid employee ID supplied")
    })
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable("id") @Parameter(description = "Employee ID", required = true) Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        employeeService.deleteEmployee(employeeId, currentUser);
        return ResponseEntity.noContent().build();
    }
}