package com.example.erm.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
@Builder
@Data
public class AuditTrailDTO {
    private Long auditId;
    private String tableName;
    private Long recordId;
    private String action;
    private String changes;
    private String performedBy;
    private LocalDateTime performedAt;
    private String oldValues;
    private String newValues;
}