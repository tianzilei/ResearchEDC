package org.researchedc.module.audit.entity;

import java.util.Arrays;

public enum AuditLoginStatus {
    SUCCESSFUL_LOGIN(1),
    FAILED_LOGIN(2),
    FAILED_LOGIN_LOCKED(3),
    SUCCESSFUL_LOGOUT(4),
    ACCESS_CODE_VIEWED(5);

    private final int code;

    AuditLoginStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static AuditLoginStatus fromName(String name) {
        return AuditLoginStatus.valueOf(name);
    }

    public static AuditLoginStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(status -> status.code == code)
                .findFirst()
                .orElse(null);
    }
}
