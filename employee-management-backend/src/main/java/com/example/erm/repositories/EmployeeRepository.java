package com.example.erm.repositories;

import com.example.erm.entities.Employee;
import com.example.erm.entities.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE " +
            "(:employeeId IS NULL OR e.empId = :employeeId) AND " +
            "(:searchTerm IS NULL OR " +
            "   LOWER(e.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "   LOWER(e.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "(:departmentId IS NULL OR e.department.deptId = :departmentId) AND " +
            "(:jobTitle IS NULL OR LOWER(e.jobTitle) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:hireDateStart IS NULL OR e.hireDate >= :hireDateStart) AND " +
            "(:hireDateEnd IS NULL OR e.hireDate <= :hireDateEnd)")
    Page<Employee> searchEmployees(
            @Param("employeeId") Long employeeId,
            @Param("searchTerm") String searchTerm,
            @Param("departmentId") Long departmentId,
            @Param("jobTitle") String jobTitle,
            @Param("status") EmployeeStatus status,
            @Param("hireDateStart") LocalDate hireDateStart,
            @Param("hireDateEnd") LocalDate hireDateEnd,
            Pageable pageable
    );


    boolean existsByEmail(String email);

    List<Employee> findByStatus(EmployeeStatus status);

    List<Employee> findByDepartmentDeptId(Long departmentId);
}