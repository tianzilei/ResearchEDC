package org.researchedc.module.audit.dto;

public record AuditStudySubjectDTO(
        int id,
        String label,
        String secondaryLabel,
        String oid,
        int subjectId,
        int studyId,
        String createdDate,
        Integer ownerId,
        String ownerName,
        String status) {
}
