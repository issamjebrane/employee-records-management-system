package com.example.erm.repositories;


import com.example.erm.entities.User;
import com.example.erm.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Find by username
    Optional<User> findByUsername(String username);

    // Check if username exists
    boolean existsByUsername(String username);

    // Find by email
    Optional<User> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(UserRole role);

    // Find users by department
    List<User> findByDepartmentDeptId(Long departmentId);

    // Find active users (not logged in for a while)
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since")
    List<User> findActiveUsers(@Param("since") LocalDateTime since);

    // Search users by username or email
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    // Find users who have never logged in
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL")
    List<User> findUsersNeverLoggedIn();

    // Find users created between dates
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}