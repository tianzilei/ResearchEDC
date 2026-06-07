package org.researchedc.module.audit.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.researchedc.domain.technicaladmin.DatabaseChangeLogBean;
import org.researchedc.module.audit.dto.DatabaseChangeLogDTO;

class DatabaseChangeLogAdapterTest {

    @Test
    void findAll_mapsLegacyBeansToDtos() {
        org.researchedc.dao.spi.DatabaseChangeLogDao dao =
                org.mockito.Mockito.mock(org.researchedc.dao.spi.DatabaseChangeLogDao.class);
        DatabaseChangeLogBean bean = new DatabaseChangeLogBean();
        bean.setId("2026-06-07-phase-b");
        bean.setAuthor("codex");
        bean.setFileName("migration.xml");
        bean.setDataExecuted(Date.from(Instant.parse("2026-06-07T12:00:00Z")));
        bean.setMd5Sum("abc123");
        bean.setDescription("createTable");
        bean.setComments("phase b");
        bean.setTag("v1");
        bean.setLiquibase("4.3.5");
        when(dao.findAll()).thenReturn(new ArrayList<>(List.of(bean)));

        DatabaseChangeLogAdapter adapter = new DatabaseChangeLogAdapter(dao);

        List<DatabaseChangeLogDTO> result = adapter.findAll();

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
