package org.researchedc.module.audit.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.module.audit.dto.AuditUserEventDTO;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;
import org.researchedc.module.audit.dto.AuditUserLoginDTO;
import org.researchedc.module.audit.dto.AuditUserSummaryDTO;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.audit.service.AuditUserEventService;
import org.researchedc.module.audit.service.AuditUserLoginService;
import org.researchedc.module.audit.service.DatabaseChangeLogService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuditControllerTest {

    @Test
    void listUserLogins_requiresSysAdminRole() throws Exception {
        Method method = AuditController.class.getMethod(
                "listUserLogins", String.class, String.class, String.class, String.class,
                org.springframework.data.domain.Pageable.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('SYSADMIN')", preAuthorize.value());
    }

    @Test
    void listUserEvents_requiresSysAdminRole() throws Exception {
        Method method = AuditController.class.getMethod("listUserEvents", int.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('SYSADMIN')", preAuthorize.value());
    }

    @Test
    void listDatabaseChangeLog_requiresSysAdminRole() throws Exception {
        Method method = AuditController.class.getMethod("listDatabaseChangeLog");

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertNotNull(preAuthorize);
        assertEquals("hasRole('SYSADMIN')", preAuthorize.value());
    }

    @Test
    void listUserLogins_returnsLegacyFieldParity() throws Exception {
        AuditService auditService = org.mockito.Mockito.mock(AuditService.class);
        AuditUserEventService auditUserEventService = org.mockito.Mockito.mock(AuditUserEventService.class);
        AuditUserLoginService auditUserLoginService = org.mockito.Mockito.mock(AuditUserLoginService.class);
        DatabaseChangeLogService databaseChangeLogService =
                org.mockito.Mockito.mock(DatabaseChangeLogService.class);
        AuditUserLoginDTO dto = new AuditUserLoginDTO(
                42, "sysadmin", 7, "2026-06-07T12:00:00Z", "SUCCESSFUL_LOGIN", "1", "login ok");
        when(auditUserLoginService.listUserLogins(any()))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1));

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new AuditController(
                        auditService, auditUserEventService, auditUserLoginService, databaseChangeLogService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        mvc.perform(get("/api/v1/audit/user-logins")
                        .param("userName", "sys")
                        .param("loginStatus", "SUCCESSFUL_LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(42))
                .andExpect(jsonPath("$.content[0].userName").value("sysadmin"))
                .andExpect(jsonPath("$.content[0].userAccountId").value(7))
                .andExpect(jsonPath("$.content[0].loginAttemptDate").value("2026-06-07T12:00:00Z"))
                .andExpect(jsonPath("$.content[0].loginStatus").value("SUCCESSFUL_LOGIN"))
                .andExpect(jsonPath("$.content[0].loginStatusCode").value("1"))
                .andExpect(jsonPath("$.content[0].details").value("login ok"));
    }

    @Test
    void listUserEvents_returnsLegacyFieldParity() throws Exception {
        AuditService auditService = org.mockito.Mockito.mock(AuditService.class);
        AuditUserEventService auditUserEventService = org.mockito.Mockito.mock(AuditUserEventService.class);
        AuditUserLoginService auditUserLoginService = org.mockito.Mockito.mock(AuditUserLoginService.class);
        DatabaseChangeLogService databaseChangeLogService =
                org.mockito.Mockito.mock(DatabaseChangeLogService.class);
        AuditUserEventsDTO dto = new AuditUserEventsDTO(
                new AuditUserSummaryDTO(7, "sysadmin", "sysadmin", "System", "Admin"),
                List.of(new AuditUserEventDTO(
                        42,
                        "2026-06-07T12:00:00Z",
                        "user_account",
                        7,
                        99,
                        "updated",
                        "updated",
                        "user_updated",
                        "user_updated",
                        "email",
                        "old.test",
                        "new.test",
                        11,
                        "Main Study",
                        12,
                        "SUBJ-001",
                        java.util.Map.of("email", "new.test"),
                        java.util.Map.of())));
        when(auditUserEventService.listUserEvents(7)).thenReturn(dto);

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new AuditController(
                        auditService, auditUserEventService, auditUserLoginService, databaseChangeLogService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        mvc.perform(get("/api/v1/audit/users/7/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.id").value(7))
                .andExpect(jsonPath("$.user.userName").value("sysadmin"))
                .andExpect(jsonPath("$.user.name").value("sysadmin"))
                .andExpect(jsonPath("$.user.firstName").value("System"))
                .andExpect(jsonPath("$.user.lastName").value("Admin"))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id").value(42))
                .andExpect(jsonPath("$.events[0].auditDate").value("2026-06-07T12:00:00Z"))
                .andExpect(jsonPath("$.events[0].auditTable").value("user_account"))
                .andExpect(jsonPath("$.events[0].userId").value(7))
                .andExpect(jsonPath("$.events[0].entityId").value(99))
                .andExpect(jsonPath("$.events[0].reasonForChange").value("updated"))
                .andExpect(jsonPath("$.events[0].reasonForChangeKey").value("updated"))
                .andExpect(jsonPath("$.events[0].actionMessage").value("user_updated"))
                .andExpect(jsonPath("$.events[0].actionMessageKey").value("user_updated"))
                .andExpect(jsonPath("$.events[0].columnName").value("email"))
                .andExpect(jsonPath("$.events[0].oldValue").value("old.test"))
                .andExpect(jsonPath("$.events[0].newValue").value("new.test"))
                .andExpect(jsonPath("$.events[0].studyId").value(11))
                .andExpect(jsonPath("$.events[0].studyName").value("Main Study"))
                .andExpect(jsonPath("$.events[0].subjectId").value(12))
                .andExpect(jsonPath("$.events[0].subjectName").value("SUBJ-001"))
                .andExpect(jsonPath("$.events[0].changes.email").value("new.test"));
    }

    @Test
    void listDatabaseChangeLog_returnsLegacyFieldParity() throws Exception {
        AuditService auditService = org.mockito.Mockito.mock(AuditService.class);
        AuditUserEventService auditUserEventService = org.mockito.Mockito.mock(AuditUserEventService.class);
        AuditUserLoginService auditUserLoginService = org.mockito.Mockito.mock(AuditUserLoginService.class);
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
                .standaloneSetup(new AuditController(
                        auditService, auditUserEventService, auditUserLoginService, databaseChangeLogService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
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
