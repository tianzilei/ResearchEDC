package org.researchedc.module.audit.service;

import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditUserEventService {

    private final AuditUserEventPort auditUserEventPort;

    public AuditUserEventService(AuditUserEventPort auditUserEventPort) {
        this.auditUserEventPort = auditUserEventPort;
    }

    public AuditUserEventsDTO listUserEvents(int userId) {
        return auditUserEventPort.findUserEvents(userId);
    }
}
