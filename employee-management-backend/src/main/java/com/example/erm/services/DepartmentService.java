package com.example.erm.services;

import com.example.erm.entities.Department;
import com.example.erm.entities.User;
import com.example.erm.entities.UserRole;
import com.example.erm.exceptions.AccessDeniedException;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository, AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    public Department createDepartment(Department department, User currentUser) {
        validateAdminAccess(currentUser);
        validateDepartmentName(department.getDeptName());

        Department savedDepartment = departmentRepository.save(department);

        auditService.logActivity(
                "departments",
                savedDepartment.getDeptId(),
                "CREATE",
                null,
                savedDepartment,
                currentUser
        );

        return savedDepartment;
    }

    public Department updateDepartment(Long deptId, Department updatedDepartment, User currentUser) {
        validateAdminAccess(currentUser);

        Department existingDepartment = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + deptId));

        if (!existingDepartment.getDeptName().equals(updatedDepartment.getDeptName())) {
            validateDepartmentName(updatedDepartment.getDeptName());
        }

        // Store old state for audit
        Department oldState = copyDepartmentState(existingDepartment);

        // Update fields
        existingDepartment.setDeptName(updatedDepartment.getDeptName());

        Department savedDepartment = departmentRepository.save(existingDepartment);

        auditService.logActivity(
                "departments",
                savedDepartment.getDeptId(),
                "UPDATE",
                oldState,
                savedDepartment,
                currentUser
        );

        return savedDepartment;
    }

    public void deleteDepartment(Long deptId, User currentUser) {
        validateAdminAccess(currentUser);

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + deptId));

        if (!department.getEmployees().isEmpty()) {
            throw new IllegalStateException("Cannot delete department with active employees");
        }

        departmentRepository.delete(department);

        auditService.logActivity(
                "departments",
                deptId,
                "DELETE",
                department,
                null,
                currentUser
        );
    }

    public Department getDepartment(Long deptId, User currentUser) {
        validateUserAccess(currentUser, deptId);

        return departmentRepository.findById(deptId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + deptId));
    }

    public List<Department> getAllDepartments(User currentUser) {
        // Admin and HR can see all departments
        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.HR) {
            return departmentRepository.findAll();
        }

        // Managers can only see their own department
        if (currentUser.getRole() == UserRole.MANAGER && currentUser.getDepartment() != null) {
            return List.of(currentUser.getDepartment());
        }

        return List.of(); // Empty list for other roles
    }

    private void validateAdminAccess(User user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Only administrators can modify departments");
        }
    }

    private void validateUserAccess(User user, Long deptId) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.HR) {
            return; // Admin and HR have full access
        }

        if (user.getRole() == UserRole.MANAGER) {
            // Managers can only access their own department
            if (!user.getDepartment().getDeptId().equals(deptId)) {
                throw new AccessDeniedException("Access denied. Managers can only access their own department.");
            }
        }
    }

    private void validateDepartmentName(String deptName) {
        if (departmentRepository.existsByDeptName(deptName)) {
            throw new IllegalArgumentException("Department name already exists");
        }
    }

    private Department copyDepartmentState(Department department) {
        Department copy = new Department();
        copy.setDeptId(department.getDeptId());
        copy.setDeptName(department.getDeptName());
        return copy;
    }
}