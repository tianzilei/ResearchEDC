package org.researchedc.module.audit.dto;

public record AuditStudyEventDefinitionDTO(
        int id,
        String name,
        String oid,
        String description,
        String category,
        String type,
        boolean repeating) {
}
