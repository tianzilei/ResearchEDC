package org.researchedc.module.audit.dto;

public record AuditStudyDTO(
        int id,
        String name,
        String identifier,
        String secondaryIdentifier,
        String oid) {
}
