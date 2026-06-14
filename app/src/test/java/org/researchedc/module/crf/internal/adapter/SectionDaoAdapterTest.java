package org.researchedc.module.crf.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.module.crf.entity.SectionEntity;
import org.researchedc.module.crf.repository.SectionRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class SectionDaoAdapterTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private DataSource dataSource;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private SectionDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SectionDaoAdapter(sectionRepository, dataSource);
    }

    @Test
    void setTypesExpected_doesNotThrow() {
        adapter.setTypesExpected();
    }

    @Test
    void getEntityFromHashMap_mapsRowToSectionBean() {
        HashMap row = new HashMap();
        row.put("section_id", 10);
        row.put("crf_version_id", 5);
        row.put("label", "Section A");
        row.put("title", "Title A");
        row.put("subtitle", "Sub A");
        row.put("instructions", "Do this");
        row.put("page_number_label", "1");
        row.put("ordinal", 2);
        row.put("parent_id", 0);
        row.put("borders", 1);

        SectionBean bean = (SectionBean) adapter.getEntityFromHashMap(row);

        assertEquals(10, bean.getId());
        assertEquals(5, bean.getCRFVersionId());
        assertEquals("Section A", bean.getLabel());
        assertEquals("Title A", bean.getTitle());
        assertEquals("Sub A", bean.getSubtitle());
        assertEquals("Do this", bean.getInstructions());
        assertEquals("1", bean.getPageNumberLabel());
        assertEquals(2, bean.getOrdinal());
        assertEquals(0, bean.getParentId());
        assertEquals(1, bean.getBorders());
    }

    @Test
    void findByPK_whenFound_returnsSectionBean() {
        SectionEntity entity = section(7, 5, "Sec", "Title");
        when(sectionRepository.findById(7)).thenReturn(Optional.of(entity));

        SectionBean bean = (SectionBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("Sec", bean.getLabel());
    }

    @Test
    void findByPK_whenMissing_returnsEmptySectionBean() {
        when(sectionRepository.findById(404)).thenReturn(Optional.empty());

        SectionBean bean = (SectionBean) adapter.findByPK(404);

        assertEquals(0, bean.getId());
    }

    @Test
    void findAll_returnsAllSections() {
        when(sectionRepository.findAll()).thenReturn(
                List.of(section(1, 5, "A", "T1"), section(2, 5, "B", "T2")));

        ArrayList sections = (ArrayList) adapter.findAll();

        assertEquals(2, sections.size());
    }

    @Test
    void create_savesAndReturnsSectionBean() {
        SectionEntity saved = section(11, 5, "New", "Title");
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(saved);

        SectionBean input = new SectionBean();
        input.setCRFVersionId(5);
        input.setLabel("New");
        input.setTitle("Title");

        SectionBean result = (SectionBean) adapter.create(input);

        assertEquals(11, result.getId());
        verify(sectionRepository).save(any(SectionEntity.class));
    }

    @Test
    void update_savesUpdatedSectionBean() {
        SectionEntity existing = section(7, 5, "Old", "Title");
        when(sectionRepository.findById(7)).thenReturn(Optional.of(existing));
        SectionEntity saved = section(7, 5, "Updated", "Title");
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(saved);

        SectionBean input = new SectionBean();
        input.setId(7);
        input.setLabel("Updated");

        SectionBean result = (SectionBean) adapter.update(input);

        assertEquals(7, result.getId());
        verify(sectionRepository).save(any(SectionEntity.class));
    }

    @Test
    void findByVersionId_delegatesToRepository() {
        when(sectionRepository.findByCrfVersionIdOrderByOrdinal(5)).thenReturn(
                List.of(section(1, 5, "A", "T1")));

        ArrayList sections = (ArrayList) adapter.findByVersionId(5);

        assertEquals(1, sections.size());
        verify(sectionRepository).findByCrfVersionIdOrderByOrdinal(5);
    }

    private static SectionEntity section(Integer id, Integer crfVersionId, String label, String title) {
        SectionEntity entity = new SectionEntity();
        entity.setSectionId(id);
        entity.setCrfVersionId(crfVersionId);
        entity.setLabel(label);
        entity.setTitle(title);
        return entity;
    }
}
