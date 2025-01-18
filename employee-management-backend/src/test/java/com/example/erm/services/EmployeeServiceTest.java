package com.example.erm.services;

import com.example.erm.dto.EmployeeDTO;
import com.example.erm.entities.*;
import com.example.erm.exceptions.ResourceNotFoundException;
import com.example.erm.repositories.DepartmentRepository;
import com.example.erm.repositories.EmployeeRepository;
import com.example.erm.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock(lenient = true)
    private DepartmentRepository departmentRepository;


    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EmployeeService employeeService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currentUser = createSampleUser(); // Create a sample user for testing
        Department department = new Department();
        department.setDeptName("Engineering");
        when(departmentRepository.findByDeptNameIgnoreCase(anyString())).thenReturn(Optional.of(department));

    }

    @Test
    void createEmployee_WithValidData_ShouldReturnCreatedEmployee() {
        // Arrange
        EmployeeDTO employeeDTO = createSampleEmployeeDTO();
        Employee employee = convertToEmployeeEntity(employeeDTO); // Convert DTO to entity
        Department department = employee.getDepartment();
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.of(department));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        // Act
        Employee result = employeeService.createEmployee(employee, currentUser);

        // Assert
        assertNotNull(result);
        assertEquals(employeeDTO.getFirstName(), result.getFirstName());
        assertEquals(employeeDTO.getLastName(), result.getLastName());
        assertEquals(employeeDTO.getEmail(), result.getEmail());
        assertEquals(employeeDTO.getDepartmentId(), result.getDepartment().getDeptId());
        assertEquals(employeeDTO.getSalary(), result.getSalary());
        assertEquals(employeeDTO.getStatus(), result.getStatus());
        assertNotNull(result.getCreatedAt()); // Ensure timestamps are set
        assertNotNull(result.getUpdatedAt());
        verify(employeeRepository).save(any(Employee.class));
        verify(auditService).logActivity(
                "employees",
                result.getEmpId(),
                "CREATE",
                null,
                result,
                currentUser
        );
    }


    @Test
    void createEmployee_WithInvalidDepartment_ShouldThrowException() {
        // Arrange
        EmployeeDTO employeeDTO = createSampleEmployeeDTO();
        when(departmentRepository.findById(anyLong())).thenReturn(Optional.empty()); // Simulate invalid department

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                employeeService.createEmployee(convertToEmployeeEntity(employeeDTO), currentUser));
    }

    private Employee convertToEmployeeEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setEmail(employeeDTO.getEmail());
        employee.setHireDate(employeeDTO.getHireDate());
        employee.setJobTitle(employeeDTO.getJobTitle());

        Department department = new Department();
        department.setDeptId(employeeDTO.getDepartmentId());
        employee.setDepartment(department);

        employee.setSalary(employeeDTO.getSalary());
        employee.setStatus(employeeDTO.getStatus());
        return employee;
    }

    private EmployeeDTO createSampleEmployeeDTO() {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        employeeDTO.setFirstName("John");
        employeeDTO.setLastName("Doe");
        employeeDTO.setEmail("john.doe@example.com");
        employeeDTO.setHireDate(LocalDate.now());
        employeeDTO.setJobTitle("Software Engineer");
        employeeDTO.setDepartmentId(1L);
        employeeDTO.setSalary(new BigDecimal("60000"));
        employeeDTO.setStatus(EmployeeStatus.ACTIVE);
        return employeeDTO;
    }

    private User createSampleUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setPasswordHash("admin123");
        user.setRole(UserRole.ADMIN);
        return user;
    }
}
