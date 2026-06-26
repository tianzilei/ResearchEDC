package org.researchedc.module.audit.event;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.dataimport.event.ImportCommittedEvent;

class ImportAuditEventHandlerTest {

    @Test
    void onImportCommitted_recordsImportAudit() {
        AuditService auditService = mock(AuditService.class);
        ImportAuditEventHandler handler = new ImportAuditEventHandler(auditService);

        handler.onImportCommitted(new ImportCommittedEvent(
                7L, 11, "Nightly ODM", 42, 3, 27));

        verify(auditService).recordAudit(
                eq(11),
                eq(AuditEventType.IMPORT),
                eq("ImportJob"),
                eq(7L),
                eq("Nightly ODM"),
                isNull(),
                eq("committed"),
                eq(42),
                contains("3 event CRF(s)"),
                eq("dataimport"));
    }
}
