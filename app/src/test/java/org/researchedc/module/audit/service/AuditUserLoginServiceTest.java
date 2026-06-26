package org.researchedc.module.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserLoginQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AuditUserLoginServiceTest {

    @Test
    void listUserLogins_delegatesToPort() {
        AuditUserLoginPort port = org.mockito.Mockito.mock(AuditUserLoginPort.class);
        AuditUserLoginQuery query = new AuditUserLoginQuery(
                "admin", "2026-06-07", "SUCCESSFUL_LOGIN", "ok", PageRequest.of(0, 20));
        AuditUserLoginDTO dto = new AuditUserLoginDTO(
                1, "admin", 1, "2026-06-07T12:00:00Z", "SUCCESSFUL_LOGIN", "1", "ok");
        Page<AuditUserLoginDTO> page = new PageImpl<>(List.of(dto), query.pageable(), 1);
        when(port.findUserLogins(query)).thenReturn(page);

        Page<AuditUserLoginDTO> result = new AuditUserLoginService(port).listUserLogins(query);

        assertEquals(page, result);
        verify(port).findUserLogins(query);
    }
}
