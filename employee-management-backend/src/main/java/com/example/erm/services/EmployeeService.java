package com.example.erm.services;


import com.example.erm.dto.EmployeeSearchCriteria;
import com.example.erm.entities.*;
import com.example.erm.exceptions.AccessDeniedException;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.DepartmentRepository;
import com.example.erm.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           AuditService auditService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }
    @Transactional
    public Employee createEmployee(Employee employee, User currentUser) {
        validateEmployeeData(employee, "CREATE");
        validateUserPermissions(currentUser, employee.getDepartment().getDeptId());

        employee.setStatus(employee.getStatus() != null ? employee.getStatus() : EmployeeStatus.ACTIVE);
        employee.setCreatedBy(currentUser);

        Employee savedEmployee = employeeRepository.save(employee);

        auditService.logActivity(
                "employees",
                savedEmployee.getEmpId(),
                "CREATE",
                null,
                savedEmployee,
                currentUser
        );

        return savedEmployee;
    }
    @Transactional
    public Employee updateEmployee(Long empId, Employee updatedEmployee, User currentUser) {
        Employee existingEmployee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + empId));

        validateUserPermissions(currentUser, existingEmployee.getDepartment().getDeptId());
        validateEmployeeData(updatedEmployee, "UPDATE");

        // Store old state for audit
        Employee oldState = copyEmployeeState(existingEmployee);

        // Update fields
        updateEmployeeFields(existingEmployee, updatedEmployee);
        existingEmployee.setUpdatedBy(currentUser);

        Employee savedEmployee = employeeRepository.save(existingEmployee);

        auditService.logActivity(
                "employees",
                savedEmployee.getEmpId(),
                "UPDATE",
                oldState,
                savedEmployee,
                currentUser
        );

        return savedEmployee;
    }
    @Transactional
    public void deleteEmployee(Long empId, User currentUser) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + empId));

        validateUserPermissions(currentUser, employee.getDepartment().getDeptId());

        // Soft delete by setting status to INACTIVE
        employee.setStatus(EmployeeStatus.INACTIVE);
        employee.setUpdatedBy(currentUser);

        Employee savedEmployee = employeeRepository.save(employee);

        auditService.logActivity(
                "employees",
                savedEmployee.getEmpId(),
                "DELETE",
                employee,
                savedEmployee,
                currentUser
        );
    }

    public Employee getEmployee(Long empId, User currentUser) {
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + empId));

        validateUserPermissions(currentUser, employee.getDepartment().getDeptId());
        return employee;
    }

    @Transactional
    public Page<Employee> searchEmployees(EmployeeSearchCriteria criteria, User currentUser) {
        // Validate department access if specified
        if (criteria.getDepartmentId() != null) {
            validateUserPermissions(currentUser, criteria.getDepartmentId());
        }

        // Create pageable for pagination and sorting
        Pageable pageable = createPageable(criteria);

        // Perform search with all criteria
        return employeeRepository.searchEmployees(
                criteria.getEmployeeId(),
                criteria.getSearchTerm(),
                criteria.getDepartmentId(),
                criteria.getJobTitle(),
                criteria.getStatus(),
                criteria.getHireDateStart(),
                criteria.getHireDateEnd(),
                pageable
        );
    }

    @Transactional
    protected Pageable createPageable(EmployeeSearchCriteria criteria) {
        int page = criteria.getPage() != null ? criteria.getPage() : 0;
        int size = criteria.getSize() != null ? criteria.getSize() : 10;

        Sort sort = Sort.by(Sort.Direction.fromString(
                        criteria.getSortDirection() != null ? criteria.getSortDirection() : "ASC"),
                criteria.getSortBy() != null ? criteria.getSortBy() : "empId"
        );

        return PageRequest.of(page, size, sort);
    }

    @Transactional
    protected void validateUserPermissions(User user, Long departmentId) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.HR) {
            return; // Admin and HR have full access
        }

        if (user.getRole() == UserRole.MANAGER) {
            // Managers can only access their department
            if (!user.getDepartment().getDeptId().equals(departmentId)) {
                throw new AccessDeniedException("Access denied. Managers can only access their department.");
            }
        }
    }
    @Transactional
    protected void validateEmployeeData(Employee employee, String action) {
        if (employee.getEmail() != null &&
                employeeRepository.existsByEmail(employee.getEmail()) && action.equals("CREATE")) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (employee.getDepartment() != null) {
            departmentRepository.findById(employee.getDepartment().getDeptId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        if (employee.getManager() != null) {
            employeeRepository.findById(employee.getManager().getEmpId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        }
    }
    @Transactional
    protected void updateEmployeeFields(Employee existing, Employee updated) {
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setEmail(updated.getEmail());
        existing.setJobTitle(updated.getJobTitle());
        existing.setSalary(updated.getSalary());
        existing.setDepartment(updated.getDepartment());
        existing.setManager(updated.getManager());
        existing.setStatus(updated.getStatus());
    }
    @Transactional
    protected Employee copyEmployeeState(Employee employee) {
        Employee copy = new Employee();
        copy.setEmpId(employee.getEmpId());
        copy.setFirstName(employee.getFirstName());
        copy.setLastName(employee.getLastName());
        copy.setEmail(employee.getEmail());
        copy.setJobTitle(employee.getJobTitle());
        copy.setSalary(employee.getSalary());
        copy.setDepartment(employee.getDepartment());
        copy.setManager(employee.getManager());
        copy.setStatus(employee.getStatus());
        return copy;
    }
    @Transactional
    protected List<Employee> getUserAccessibleEmployees(User user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.HR) {
            return employeeRepository.findAll();
        } else if (user.getRole() == UserRole.MANAGER) {
            return employeeRepository.findByDepartmentDeptId(user.getDepartment().getDeptId());
        }
        return List.of(); // Empty list for other roles
    }
    @Transactional
    public Page<Employee> getAllEmployees(Integer page, Integer size, String sortBy, String sortDirection, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        return employeeRepository.findAll(pageable);
    }
}