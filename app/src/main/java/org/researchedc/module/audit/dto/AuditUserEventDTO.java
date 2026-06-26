package org.researchedc.module.audit.dto;

import java.util.Map;

public record AuditUserEventDTO(
        Integer id,
        String auditDate,
        String auditTable,
        Integer userId,
        Integer entityId,
        String reasonForChange,
        String reasonForChangeKey,
        String actionMessage,
        String actionMessageKey,
        String columnName,
        String oldValue,
        String newValue,
        Integer studyId,
        String studyName,
        Integer subjectId,
        String subjectName,
        Map<String, Object> changes,
        Map<String, Object> otherInfo) {
}
