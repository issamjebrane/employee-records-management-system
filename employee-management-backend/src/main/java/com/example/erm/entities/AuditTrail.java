package com.example.erm.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_trail")
public class AuditTrail {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_id_seq")
    @SequenceGenerator(name = "audit_id_seq", sequenceName = "audit_id_seq", allocationSize = 1)
    private Long auditId;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "record_id", nullable = false)
    private Long recordId;

    @Column(nullable = false)
    private String action;

    @Column(name = "old_values")
    @Lob
    private String oldValues;

    @Column(name = "new_values")
    @Lob
    private String newValues;

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt = LocalDateTime.now();
}