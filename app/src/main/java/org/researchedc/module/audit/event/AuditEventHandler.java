package org.researchedc.module.audit.event;

import org.researchedc.module.audit.service.AuditService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditEventHandler {

    private final AuditService auditService;

    public AuditEventHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void handleAuditRecorded(AuditRecordedEvent event) {
        auditService.onAuditRecorded(event);
    }
}
