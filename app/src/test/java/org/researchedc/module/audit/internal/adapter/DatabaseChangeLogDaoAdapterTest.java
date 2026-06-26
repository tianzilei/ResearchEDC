package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;
import org.researchedc.module.audit.entity.DatabaseChangeLogEntry;

class DatabaseChangeLogDaoAdapterTest {

    @Test
    void findChangeLogs_mapsJpaBeansToDtos() {
        DatabaseChangeLogEntry bean = new DatabaseChangeLogEntry();
        bean.setId("2026-06-07-phase-b");
        bean.setAuthor("codex");
        bean.setFileName("migration.xml");
        bean.setDataExecuted(Date.from(Instant.parse("2026-06-07T12:00:00Z")));
        bean.setMd5Sum("abc123");
        bean.setDescription("createTable");
        bean.setComments("phase b");
        bean.setTag("v1");
        bean.setLiquibase("4.3.5");
        EntityManager entityManager = Mockito.mock(EntityManager.class);
        @SuppressWarnings("unchecked")
        TypedQuery<DatabaseChangeLogEntry> query = Mockito.mock(TypedQuery.class);
        Mockito.when(entityManager.createQuery(
                "FROM DatabaseChangeLogEntry dcl ORDER BY dcl.id DESC",
                DatabaseChangeLogEntry.class)).thenReturn(query);
        Mockito.when(query.getResultList()).thenReturn(List.of(bean));

        DatabaseChangeLogDaoAdapter adapter = new DatabaseChangeLogDaoAdapter();
        adapter.setEntityManager(entityManager);

        List<DatabaseChangeLogDTO> result = adapter.findChangeLogs();

        assertEquals(1, result.size());
        DatabaseChangeLogDTO dto = result.getFirst();
        assertEquals("2026-06-07-phase-b", dto.id());
        assertEquals("codex", dto.author());
        assertEquals("migration.xml", dto.fileName());
        assertEquals("2026-06-07T12:00:00Z", dto.dateExecuted());
        assertEquals("abc123", dto.md5Sum());
        assertEquals("createTable", dto.description());
        assertEquals("phase b", dto.comments());
        assertEquals("v1", dto.tag());
        assertEquals("4.3.5", dto.liquibase());
    }
}
