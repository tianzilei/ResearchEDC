package org.researchedc.module.audit.dto;

public record AuditEventCrfDTO(
        int id,
        int studyEventId,
        int studySubjectId,
        int crfVersionId,
        String dateInterviewed,
        String interviewerName,
        String dateCompleted,
        String status,
        String stage,
        boolean electronicSignatureStatus,
        boolean sdvStatus) {
}
