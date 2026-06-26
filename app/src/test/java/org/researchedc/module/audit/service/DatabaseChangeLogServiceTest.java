package org.researchedc.module.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;

class DatabaseChangeLogServiceTest {

    @Test
    void listChangeLogs_delegatesToPort() {
        DatabaseChangeLogPort port = org.mockito.Mockito.mock(DatabaseChangeLogPort.class);
        DatabaseChangeLogDTO dto = new DatabaseChangeLogDTO(
                "id", "author", "file.xml", "1970-01-01T00:00:00Z",
                "md5", "description", "comments", "tag", "liquibase");
        when(port.findChangeLogs()).thenReturn(List.of(dto));

        DatabaseChangeLogService service = new DatabaseChangeLogService(port);

        List<DatabaseChangeLogDTO> result = service.listChangeLogs();

        assertEquals(List.of(dto), result);
        verify(port).findChangeLogs();
    }
}
