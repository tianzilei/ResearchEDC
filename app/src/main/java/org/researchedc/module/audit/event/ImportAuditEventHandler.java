package org.researchedc.module.audit.event;

import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.dataimport.event.ImportCommittedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ImportAuditEventHandler {

    private final AuditService auditService;

    public ImportAuditEventHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onImportCommitted(ImportCommittedEvent event) {
        auditService.recordAudit(
                event.studyId(),
                AuditEventType.IMPORT,
                "ImportJob",
                event.importJobId(),
                event.importName(),
                null,
                "committed",
                event.requestedBy(),
                "Committed import job " + event.importJobId() + " with "
                        + event.eventCrfs() + " event CRF(s) and "
                        + event.items() + " item(s).",
                "dataimport");
    }
}
