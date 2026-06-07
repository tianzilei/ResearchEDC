package org.researchedc.module.audit.dto;

import java.util.List;

public record AuditUserEventsDTO(
        AuditUserSummaryDTO user,
        List<AuditUserEventDTO> events) {
}
