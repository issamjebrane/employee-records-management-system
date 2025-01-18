package com.example.erm.repositories;

import com.example.erm.entities.AuditTrail;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<AuditTrail, Long> {
    List<AuditTrail> findByTableNameAndRecordId(String tableName, Long recordId);
    List<AuditTrail> findByChangedBy_UserId(Long userId);

    List<AuditTrail> findByTableName(String tableName);
}
