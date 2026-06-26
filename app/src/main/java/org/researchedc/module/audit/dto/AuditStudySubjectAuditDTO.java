package org.researchedc.module.audit.dto;

public record AuditStudySubjectAuditDTO(
        int id,
        String auditDate,
        String auditTable,
        int userId,
        String userName,
        int entityId,
        String entityName,
        String auditEventTypeName,
        int auditEventTypeId,
        String oldValue,
        String newValue,
        String reasonForChange) {
}
