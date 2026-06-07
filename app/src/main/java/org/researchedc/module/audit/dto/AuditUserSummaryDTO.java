package org.researchedc.module.audit.dto;

public record AuditUserSummaryDTO(
        Integer id,
        String userName,
        String name,
        String firstName,
        String lastName) {
}
