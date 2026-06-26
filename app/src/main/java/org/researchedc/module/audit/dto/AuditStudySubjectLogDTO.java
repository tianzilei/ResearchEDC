package org.researchedc.module.audit.dto;

import java.util.List;

public record AuditStudySubjectLogDTO(
        AuditStudySubjectDTO studySubject,
        AuditSubjectDTO subject,
        List<AuditStudySubjectAuditDTO> audits,
        List<AuditStudyEventDTO> events) {
}
