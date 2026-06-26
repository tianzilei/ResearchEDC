package org.researchedc.module.audit.dto;

import java.util.List;

public record AuditStudyEventDTO(
        int id,
        int studyEventDefinitionId,
        int studySubjectId,
        String location,
        int sampleOrdinal,
        String dateStarted,
        String dateEnded,
        String status,
        String stage,
        String subjectEventStatus,
        AuditStudyEventDefinitionDTO definition,
        List<AuditEventCrfDTO> eventCrfs) {
}
