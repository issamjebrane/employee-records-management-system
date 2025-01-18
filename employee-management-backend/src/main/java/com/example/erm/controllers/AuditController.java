package com.example.erm.controllers;

import com.example.erm.dto.AuditTrailDTO;
import com.example.erm.entities.AuditTrail;
import com.example.erm.services.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Audit Management", description = "APIs for managing audit logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    //get all audit logs
    @GetMapping
    @Operation(summary = "Get all audit logs", description = "Get all audit logs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditTrail> getAllAuditLogs(
            @AuthenticationPrincipal UserDetails userDetails) {

        // get all audit logs and convert to DTO
        return auditService.getAllAuditTrail();


    }
}
