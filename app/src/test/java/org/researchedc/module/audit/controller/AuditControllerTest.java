package org.researchedc.module.audit.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.audit.service.DatabaseChangeLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuditControllerTest {

    @Test
    void listDatabaseChangeLog_requiresSysAdminRole() throws Exception {
        Method method = AuditController.class.getMethod("listDatabaseChangeLog");

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('SYSADMIN')", preAuthorize.value());
    }

    @Test
    void listDatabaseChangeLog_returnsLegacyFieldParity() throws Exception {
        AuditService auditService = org.mockito.Mockito.mock(AuditService.class);
        DatabaseChangeLogService databaseChangeLogService =
                org.mockito.Mockito.mock(DatabaseChangeLogService.class);
        when(databaseChangeLogService.listChangeLogs()).thenReturn(List.of(
                new DatabaseChangeLogDTO(
                        "2026-06-07-phase-b",
                        "codex",
                        "migration.xml",
                        "2026-06-07T12:00:00Z",
                        "abc123",
                        "createTable",
                        "phase b",
                        "v1",
                        "4.3.5")));

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new AuditController(auditService, databaseChangeLogService))
                .build();

        mvc.perform(get("/api/v1/audit/database-changelog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("2026-06-07-phase-b"))
                .andExpect(jsonPath("$[0].author").value("codex"))
                .andExpect(jsonPath("$[0].fileName").value("migration.xml"))
                .andExpect(jsonPath("$[0].dateExecuted").value("2026-06-07T12:00:00Z"))
                .andExpect(jsonPath("$[0].md5Sum").value("abc123"))
                .andExpect(jsonPath("$[0].description").value("createTable"))
                .andExpect(jsonPath("$[0].comments").value("phase b"))
                .andExpect(jsonPath("$[0].tag").value("v1"))
                .andExpect(jsonPath("$[0].liquibase").value("4.3.5"));
    }
}
