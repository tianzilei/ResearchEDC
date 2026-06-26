package org.researchedc.module.audit.dto;

import org.springframework.data.domain.Pageable;

public record AuditUserLoginQuery(
        String userName,
        String loginAttemptDate,
        String loginStatus,
        String details,
        Pageable pageable) {
}
