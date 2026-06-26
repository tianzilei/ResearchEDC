package org.researchedc.module.audit.controller;

import java.util.List;

import org.researchedc.module.audit.dto.AuditLogDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.audit.service.AuditStudySubjectEventService;
import org.researchedc.module.audit.service.AuditUserEventService;
import org.researchedc.module.audit.service.AuditUserLoginService;
import org.researchedc.module.audit.service.DatabaseChangeLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;
    private final AuditStudySubjectEventService auditStudySubjectEventService;
    private final AuditUserEventService auditUserEventService;
    private final AuditUserLoginService auditUserLoginService;
    private final DatabaseChangeLogService databaseChangeLogService;

    public AuditController(AuditService auditService,
                           AuditStudySubjectEventService auditStudySubjectEventService,
                           AuditUserEventService auditUserEventService,
                           AuditUserLoginService auditUserLoginService,
                           DatabaseChangeLogService databaseChangeLogService) {
        this.auditService = auditService;
        this.auditStudySubjectEventService = auditStudySubjectEventService;
        this.auditUserEventService = auditUserEventService;
        this.auditUserLoginService = auditUserLoginService;
        this.databaseChangeLogService = databaseChangeLogService;
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

    @GetMapping("/user-logins")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Page<AuditUserLoginDTO>> listUserLogins(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String loginAttemptDate,
            @RequestParam(required = false) String loginStatus,
            @RequestParam(required = false) String details,
            Pageable pageable) {
        AuditUserLoginQuery query = new AuditUserLoginQuery(
                userName, loginAttemptDate, loginStatus, details, pageable);
        return ResponseEntity.ok(auditUserLoginService.listUserLogins(query));
    }

    @GetMapping("/users/{userId}/events")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<AuditUserEventsDTO> listUserEvents(@PathVariable int userId) {
        return ResponseEntity.ok(auditUserEventService.listUserEvents(userId));
    }

    @GetMapping("/studies/{studyId}/subject-events")
    @PreAuthorize("hasAnyRole('SYSADMIN','STUDY_DIRECTOR','COORDINATOR')")
    public ResponseEntity<AuditStudySubjectEventsDTO> listStudySubjectEvents(@PathVariable int studyId) {
        return ResponseEntity.ok(auditStudySubjectEventService.listStudySubjectEvents(studyId));
    }

    @GetMapping("/database-changelog")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<DatabaseChangeLogDTO>> listDatabaseChangeLog() {
        return ResponseEntity.ok(databaseChangeLogService.listChangeLogs());
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
