package com.example.erm.services;

import com.example.erm.entities.AuditTrail;
import com.example.erm.entities.User;
import com.example.erm.repositories.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AuditService {
    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditService(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    public void logActivity(String tableName, Long recordId, String action,
                            Object oldValue, Object newValue, User user) {
        try {
            AuditTrail audit = new AuditTrail();
            audit.setTableName(tableName);
            audit.setRecordId(recordId);
            audit.setAction(action);
            audit.setChangedBy(user);

            if (oldValue != null) {
                audit.setOldValues(objectMapper.writeValueAsString(oldValue));
            }
            if (newValue != null) {
                audit.setNewValues(objectMapper.writeValueAsString(newValue));
            }

            auditRepository.save(audit);
        } catch (Exception e) {
            throw new RuntimeException("Error logging audit trail", e);
        }
    }

    public List<AuditTrail> getAuditTrail(String tableName, Long recordId) {
        return auditRepository.findByTableNameAndRecordId(tableName, recordId);
    }

    public List<AuditTrail> getUserActions(Long userId) {
        return auditRepository.findByChangedBy_UserId(userId);
    }
}