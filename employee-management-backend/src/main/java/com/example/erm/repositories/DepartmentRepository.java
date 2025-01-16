package com.example.erm.repositories;

import com.example.erm.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // Find by department name (case-insensitive)
    Optional<Department> findByDeptNameIgnoreCase(String deptName);

    // Check if department name exists
    boolean existsByDeptName(String deptName);

    // Search departments by name containing string (case-insensitive)
    List<Department> findByDeptNameContainingIgnoreCase(String searchTerm);

    // Find departments with employee count
    @Query("SELECT d, COUNT(e) FROM Department d LEFT JOIN d.employees e GROUP BY d")
    List<Object[]> findDepartmentsWithEmployeeCount();

    // Find departments with no employees
    @Query("SELECT d FROM Department d WHERE SIZE(d.employees) = 0")
    List<Department> findDepartmentsWithNoEmployees();

    // Find departments by manager
    @Query("SELECT d FROM Department d INNER JOIN d.users u WHERE u.role = 'MANAGER' AND u.userId = :managerId")
    Optional<Department> findByManagerId(@Param("managerId") Long managerId);

    // Find departments with employee count greater than specified number
    @Query("SELECT d FROM Department d WHERE SIZE(d.employees) > :count")
    List<Department> findDepartmentsWithEmployeeCountGreaterThan(@Param("count") int count);

    // Custom query to get department statistics
    @Query("SELECT new map(" +
            "d.deptId as departmentId, " +
            "d.deptName as departmentName, " +
            "COUNT(e) as employeeCount, " +
            "AVG(e.salary) as averageSalary) " +
            "FROM Department d " +
            "LEFT JOIN d.employees e " +
            "GROUP BY d.deptId, d.deptName")
    List<Object> getDepartmentStatistics();
}