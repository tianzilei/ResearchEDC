package org.researchedc.module.audit.dto;

public record AuditUserLoginDTO(
        Integer id,
        String userName,
        Integer userAccountId,
        String loginAttemptDate,
        String loginStatus,
        String loginStatusCode,
        String details) {
}
