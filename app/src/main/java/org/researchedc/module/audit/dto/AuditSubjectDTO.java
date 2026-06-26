package org.researchedc.module.audit.dto;

public record AuditSubjectDTO(
        int id,
        String uniqueIdentifier,
        String label,
        String dateOfBirth,
        String gender,
        boolean dobCollected,
        String status) {
}
