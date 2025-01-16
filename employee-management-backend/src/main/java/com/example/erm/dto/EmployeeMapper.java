package com.example.erm.dto;

import com.example.erm.entities.Department;
import com.example.erm.entities.Employee;
import com.example.erm.entities.EmployeeStatus;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.exceptions.ValidationException;
import com.example.erm.repositories.DepartmentRepository;
import com.example.erm.repositories.EmployeeRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmployeeMapper {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public Employee toEntity(EmployeeDTO dto) {
        if (dto == null) {
            return null;
        }

        Employee employee = new Employee();

        // Set basic fields
        employee.setEmpId(dto.getEmpId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setHireDate(dto.getHireDate());
        employee.setJobTitle(dto.getJobTitle());
        employee.setSalary(dto.getSalary());
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : EmployeeStatus.ACTIVE);

        // Set Department relationship
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Set Manager relationship
        if (dto.getManagerId() != null) {
            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id: " + dto.getManagerId()));
            employee.setManager(manager);
        }

        return employee;
    }

    public Employee updateEntity(Employee existingEmployee, EmployeeDTO dto) {
        // Update basic fields
        existingEmployee.setFirstName(dto.getFirstName());
        existingEmployee.setLastName(dto.getLastName());
        existingEmployee.setEmail(dto.getEmail());
        existingEmployee.setHireDate(dto.getHireDate());
        existingEmployee.setJobTitle(dto.getJobTitle());
        existingEmployee.setSalary(dto.getSalary());

        if (dto.getStatus() != null) {
            existingEmployee.setStatus(dto.getStatus());
        }

        // Update Department if changed
        if (dto.getDepartmentId() != null &&
                (existingEmployee.getDepartment() == null ||
                        !existingEmployee.getDepartment().getDeptId().equals(dto.getDepartmentId()))) {

            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + dto.getDepartmentId()));
            existingEmployee.setDepartment(department);
        }

        // Update Manager if changed
        if (dto.getManagerId() != null &&
                (existingEmployee.getManager() == null ||
                        !existingEmployee.getManager().getEmpId().equals(dto.getManagerId()))) {

            // Prevent self-reference as manager
            if (existingEmployee.getEmpId().equals(dto.getManagerId())) {
                throw new ValidationException("An employee cannot be their own manager",
                        Map.of("managerId", "Self-reference not allowed"));
            }

            Employee manager = employeeRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Manager not found with id: " + dto.getManagerId()));
            existingEmployee.setManager(manager);
        } else if (dto.getManagerId() == null) {
            existingEmployee.setManager(null); // Remove manager if set to null
        }

        return existingEmployee;
    }
    public EmployeeResponseDTO toResponseDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO();

        // Map basic fields
        responseDTO.setEmpId(employee.getEmpId());
        responseDTO.setFirstName(employee.getFirstName());
        responseDTO.setLastName(employee.getLastName());
        responseDTO.setEmail(employee.getEmail());
        responseDTO.setHireDate(employee.getHireDate());
        responseDTO.setJobTitle(employee.getJobTitle());
        responseDTO.setSalary(employee.getSalary());
        responseDTO.setStatus(employee.getStatus());

        // Map department info
        if (employee.getDepartment() != null) {
            responseDTO.setDepartmentId(employee.getDepartment().getDeptId());
            responseDTO.setDepartmentName(employee.getDepartment().getDeptName());
        }

        // Map manager info
        if (employee.getManager() != null) {
            responseDTO.setManagerId(employee.getManager().getEmpId());
            responseDTO.setManagerName(employee.getManager().getFirstName() + " " +
                    employee.getManager().getLastName());
        }

        // Map audit fields
        responseDTO.setCreatedAt(employee.getCreatedAt());
        responseDTO.setUpdatedAt(employee.getUpdatedAt());

        if (employee.getCreatedBy() != null) {
            responseDTO.setCreatedBy(employee.getCreatedBy().getUsername());
        }

        if (employee.getUpdatedBy() != null) {
            responseDTO.setUpdatedBy(employee.getUpdatedBy().getUsername());
        }

        return responseDTO;
    }
}