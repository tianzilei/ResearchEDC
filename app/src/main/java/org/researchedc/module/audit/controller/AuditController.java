package org.researchedc.module.audit.controller;

import org.researchedc.module.audit.dto.AuditLogDTO;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> listAuditLogs(
            @RequestParam(required = false) Integer studyId,
            Pageable pageable) {
        return ResponseEntity.ok(auditService.listAuditLogs(studyId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> getAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getAuditLog(id));
    }

    @PostMapping
    public ResponseEntity<AuditLogDTO> recordAudit(
            @RequestParam(required = false) Integer studyId,
            @RequestParam AuditEventType eventType,
            @RequestParam String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String entityLabel,
            @RequestParam(required = false) String oldValue,
            @RequestParam(required = false) String newValue,
            @RequestParam(required = false) Integer performedBy,
            @RequestParam(required = false) String details,
            @RequestParam(defaultValue = "api") String sourceModule) {
        AuditLogDTO dto = auditService.recordAudit(
            studyId, eventType, entityType, entityId, entityLabel,
            oldValue, newValue, performedBy, details, sourceModule);
        return ResponseEntity.ok(dto);
    }
}
