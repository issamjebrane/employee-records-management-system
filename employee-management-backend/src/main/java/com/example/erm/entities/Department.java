package com.example.erm.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dept_id_seq")
    @SequenceGenerator(name = "dept_id_seq", sequenceName = "dept_id_seq", allocationSize = 1)
    private Long deptId;

    @NotBlank
    @Column(name = "dept_name", unique = true, nullable = false)
    private String deptName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "department")
    @JsonIgnore
    private List<Employee> employees = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    private List<User> users = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}