package org.researchedc.module.audit.dto;

import java.util.List;

public record AuditStudySubjectEventsDTO(
        AuditStudyDTO study,
        List<AuditStudySubjectLogDTO> subjects) {
}
