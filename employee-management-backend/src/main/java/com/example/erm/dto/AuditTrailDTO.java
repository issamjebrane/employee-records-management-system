package com.example.erm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditTrailDTO {
    private Long auditId;
    private String tableName;
    private Long recordId;
    private String action;
    private String changes;
    private String performedBy;
    private LocalDateTime performedAt;
}