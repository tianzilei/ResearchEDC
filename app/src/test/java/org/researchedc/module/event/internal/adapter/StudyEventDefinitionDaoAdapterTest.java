package org.researchedc.module.event.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.bean.managestudy.StudyEventDefinitionBean;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;

@ExtendWith(MockitoExtension.class)
class StudyEventDefinitionDaoAdapterTest {

    @Mock
    private StudyEventDefinitionRepository studyEventDefinitionRepository;

    private StudyEventDefinitionDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StudyEventDefinitionDaoAdapter(studyEventDefinitionRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusDays(3);
        StudyEventDefinitionEntity entity = definition(7, 3, "Screening", Status.AVAILABLE.getId());
        entity.setDescription("screening visit");
        entity.setRepeating(true);
        entity.setType("scheduled");
        entity.setCategory("clinic");
        entity.setOcOid("SE_SCREEN");
        entity.setOrdinal(2);
        entity.setDateCreated(created);
        entity.setDateUpdated(created.plusDays(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        when(studyEventDefinitionRepository.findById(7)).thenReturn(Optional.of(entity));

        StudyEventDefinitionBean bean = (StudyEventDefinitionBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(3, bean.getStudyId());
        assertEquals("Screening", bean.getName());
        assertEquals("screening visit", bean.getDescription());
        assertEquals(true, bean.isRepeating());
        assertEquals("scheduled", bean.getType());
        assertEquals("clinic", bean.getCategory());
        assertEquals("SE_SCREEN", bean.getOid());
        assertEquals(2, bean.getOrdinal());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyStudyEventDefinitionBean() {
        when(studyEventDefinitionRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(StudyEventDefinitionBean.class, bean);
        assertEquals(0, ((StudyEventDefinitionBean) bean).getId());
    }

    @Test
    void findByPKAndStudy_requiresMatchingStudyId() {
        StudyBean study = new StudyBean();
        study.setId(5);
        when(studyEventDefinitionRepository.findById(7))
                .thenReturn(Optional.of(definition(7, 6, "Mismatch", Status.AVAILABLE.getId())));

        StudyEventDefinitionBean bean = (StudyEventDefinitionBean) adapter.findByPKAndStudy(7, study);

        assertEquals(0, bean.getId());
    }

    @Test
    void findByOidAndStudy_fallsBackToParentStudy() {
        when(studyEventDefinitionRepository.findByOcOidAndStudyId("SE1", 11)).thenReturn(Optional.empty());
        when(studyEventDefinitionRepository.findByOcOidAndStudyId("SE1", 10))
                .thenReturn(Optional.of(definition(8, 10, "Parent", Status.AVAILABLE.getId())));

        StudyEventDefinitionBean bean = adapter.findByOidAndStudy("SE1", 11, 10);

        assertEquals(8, bean.getId());
        verify(studyEventDefinitionRepository).findByOcOidAndStudyId("SE1", 11);
        verify(studyEventDefinitionRepository).findByOcOidAndStudyId("SE1", 10);
    }

    @Test
    void findByOid_whenMissing_returnsNullLikeLegacyDao() {
        when(studyEventDefinitionRepository.findByOcOid("UNKNOWN")).thenReturn(Optional.empty());

        assertNull(adapter.findByOid("UNKNOWN"));
    }

    @Test
    void findAllByStudy_usesParentStudyForSitesAndSortsById() {
        StudyBean site = new StudyBean();
        site.setId(11);
        site.setParentStudyId(10);
        when(studyEventDefinitionRepository.findByStudyIdOrderByName(10))
                .thenReturn(List.of(definition(3, 10, "C", 1), definition(1, 10, "A", 1)));

        ArrayList definitions = adapter.findAllByStudy(site);

        assertEquals(2, definitions.size());
        assertEquals(1, ((StudyEventDefinitionBean) definitions.get(0)).getId());
        assertEquals(3, ((StudyEventDefinitionBean) definitions.get(1)).getId());
        verify(studyEventDefinitionRepository).findByStudyIdOrderByName(10);
    }

    @Test
    void findAllActiveByStudy_usesAvailableStatusAndParentStudyForSites() {
        StudyBean site = new StudyBean();
        site.setId(11);
        site.setParentStudyId(10);
        when(studyEventDefinitionRepository.findByStatusIdAndStudyId(Status.AVAILABLE.getId(), 10))
                .thenReturn(List.of(definition(3, 10, "Active", Status.AVAILABLE.getId())));

        ArrayList definitions = adapter.findAllActiveByStudy(site);

        assertEquals(1, definitions.size());
        verify(studyEventDefinitionRepository).findByStatusIdAndStudyId(Status.AVAILABLE.getId(), 10);
    }

    @Test
    void findByName_returnsFirstMatch() {
        when(studyEventDefinitionRepository.findByName("Visit"))
                .thenReturn(List.of(definition(12, 4, "Visit", Status.AVAILABLE.getId())));

        StudyEventDefinitionBean bean = (StudyEventDefinitionBean) adapter.findByName("Visit");

        assertEquals(12, bean.getId());
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        StudyEventDefinitionEntity saved = definition(15, 4, "Created", Status.AVAILABLE.getId());
        when(studyEventDefinitionRepository.save(argThat(e -> {
            assertEquals(4, e.getStudyId());
            assertEquals("Created", e.getName());
            assertEquals("created desc", e.getDescription());
            assertEquals(true, e.getRepeating());
            assertEquals("scheduled", e.getType());
            assertEquals("clinic", e.getCategory());
            assertEquals("SE_CREATED", e.getOcOid());
            assertEquals(5, e.getOrdinal());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(40, e.getOwnerId());
            assertEquals(41, e.getUpdateId());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        StudyEventDefinitionBean input = new StudyEventDefinitionBean();
        input.setStudyId(4);
        input.setName("Created");
        input.setDescription("created desc");
        input.setRepeating(true);
        input.setType("scheduled");
        input.setCategory("clinic");
        input.setOid("SE_CREATED");
        input.setOrdinal(5);
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(40);
        input.setUpdaterId(41);

        StudyEventDefinitionBean result = (StudyEventDefinitionBean) adapter.create(input);

        assertEquals(15, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("study_event_definition_id", 50);
        row.put("study_id", 51);
        row.put("name", "Row visit");
        row.put("description", "row desc");
        row.put("repeating", true);
        row.put("type", "row type");
        row.put("category", "row category");
        row.put("oc_oid", "SE_ROW");
        row.put("ordinal", 3);
        row.put("date_created", now);
        row.put("date_updated", now);
        row.put("owner_id", 52);
        row.put("update_id", 53);
        row.put("status_id", Status.AVAILABLE.getId());

        StudyEventDefinitionBean bean = (StudyEventDefinitionBean) adapter.getEntityFromHashMap(row);

        assertEquals(50, bean.getId());
        assertEquals(51, bean.getStudyId());
        assertEquals("Row visit", bean.getName());
        assertEquals("row desc", bean.getDescription());
        assertEquals(true, bean.isRepeating());
        assertEquals("row type", bean.getType());
        assertEquals("row category", bean.getCategory());
        assertEquals("SE_ROW", bean.getOid());
        assertEquals(3, bean.getOrdinal());
        assertEquals(52, bean.getOwnerId());
        assertEquals(53, bean.getUpdaterId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
    }

    private static StudyEventDefinitionEntity definition(Integer id, Integer studyId, String name, Integer statusId) {
        StudyEventDefinitionEntity entity = new StudyEventDefinitionEntity();
        entity.setStudyEventDefinitionId(id);
        entity.setStudyId(studyId);
        entity.setName(name);
        entity.setStatusId(statusId);
        return entity;
    }
}
