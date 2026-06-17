package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.researchedc.i18n.util.ResourceBundleProvider;
import org.researchedc.module.audit.dto.AuditUserEventDTO;
import org.researchedc.module.audit.dto.AuditUserEventsDTO;

class AuditUserEventAdapterTest {

    @BeforeEach
    void setUpLocale() {
        ResourceBundleProvider.updateLocale(Locale.of("us"));
    }

    @Test
    void findUserEvents_mapsNativeUserAndAuditRows() {
        EntityManager entityManager = org.mockito.Mockito.mock(EntityManager.class);
        Query userQuery = org.mockito.Mockito.mock(Query.class);
        Query eventsQuery = org.mockito.Mockito.mock(Query.class);
        when(entityManager.createNativeQuery(startsWith("SELECT user_id"))).thenReturn(userQuery);
        when(userQuery.setParameter(1, 7)).thenReturn(userQuery);
        when(userQuery.getResultList()).thenReturn(java.util.Collections.singletonList(
                new Object[] {7, "sysadmin", "System", "Admin"}));
        when(entityManager.createNativeQuery(startsWith("SELECT ae.audit_id"))).thenReturn(eventsQuery);
        when(eventsQuery.setParameter(1, 7)).thenReturn(eventsQuery);
        when(eventsQuery.getResultList()).thenReturn(java.util.Collections.singletonList(new Object[] {
                42,
                Timestamp.from(Instant.parse("2026-06-07T12:00:00Z")),
                "user_account",
                7,
                99,
                "updated",
                "user_updated",
                "old-phone",
                "new-phone",
                "phone",
                11,
                12
        }));

        AuditUserEventAdapter adapter = new AuditUserEventAdapter();
        adapter.setEntityManager(entityManager);

        AuditUserEventsDTO result = adapter.findUserEvents(7);

        assertEquals(7, result.user().id());
        assertEquals("sysadmin", result.user().userName());
        assertEquals("System", result.user().firstName());
        AuditUserEventDTO dto = result.events().getFirst();
        assertEquals(42, dto.id());
        assertEquals("2026-06-07T12:00:00Z", dto.auditDate());
        assertEquals("user_account", dto.auditTable());
        assertEquals(7, dto.userId());
        assertEquals(99, dto.entityId());
        assertEquals("updated", dto.reasonForChange());
        assertEquals("updated", dto.reasonForChangeKey());
        assertEquals("user_updated", dto.actionMessage());
        assertEquals("user_updated", dto.actionMessageKey());
        assertEquals("phone", dto.columnName());
        assertEquals("old-phone", dto.oldValue());
        assertEquals("new-phone", dto.newValue());
        assertEquals(11, dto.studyId());
        assertEquals("NULL", dto.studyName());
        assertEquals(12, dto.subjectId());
        assertEquals("NULL", dto.subjectName());
        assertTrue(dto.changes().isEmpty());
        assertTrue(dto.otherInfo().isEmpty());
    }
}
