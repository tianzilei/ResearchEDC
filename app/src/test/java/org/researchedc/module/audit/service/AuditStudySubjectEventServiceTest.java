package org.researchedc.module.audit.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.AuditStudyDTO;
import org.researchedc.module.audit.dto.AuditStudySubjectEventsDTO;

class AuditStudySubjectEventServiceTest {

    @Test
    void listStudySubjectEvents_delegatesToPort() {
        AuditStudySubjectEventPort port = org.mockito.Mockito.mock(AuditStudySubjectEventPort.class);
        AuditStudySubjectEventsDTO dto = new AuditStudySubjectEventsDTO(
                new AuditStudyDTO(11, "Main Study", "PROTO-1", "SECONDARY", "S_MAIN"),
                List.of());
        when(port.findStudySubjectEvents(11)).thenReturn(dto);

        AuditStudySubjectEventsDTO result = new AuditStudySubjectEventService(port).listStudySubjectEvents(11);

        assertSame(dto, result);
    }
}
