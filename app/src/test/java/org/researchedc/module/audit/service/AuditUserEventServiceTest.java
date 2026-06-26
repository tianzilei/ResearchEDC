package org.researchedc.module.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.researchedc.module.audit.dto.AuditUserSummaryDTO;

class AuditUserEventServiceTest {

    @Test
    void listUserEvents_delegatesToPort() {
        AuditUserEventPort port = org.mockito.Mockito.mock(AuditUserEventPort.class);
        AuditUserEventsDTO dto = new AuditUserEventsDTO(
                new AuditUserSummaryDTO(7, "sysadmin", "sysadmin", "System", "Admin"), List.of());
        when(port.findUserEvents(7)).thenReturn(dto);

        AuditUserEventsDTO result = new AuditUserEventService(port).listUserEvents(7);

        assertEquals(dto, result);
        verify(port).findUserEvents(7);
    }
}
