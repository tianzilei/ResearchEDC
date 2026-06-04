package org.researchedc.module.dataset.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.researchedc.bean.core.DatasetItemStatus;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.repository.DatasetRepository;

@ExtendWith(MockitoExtension.class)
class DatasetDaoAdapterTest {

    @Mock
    private DatasetRepository datasetRepository;

    private DatasetDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DatasetDaoAdapter(datasetRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime created = LocalDateTime.now().minusDays(2);
        DatasetEntity entity = dataset(7, "Safety", 3, Status.AVAILABLE.getId());
        entity.setDescription("safety extracts");
        entity.setSqlStatement("select * from x");
        entity.setNumRuns(4);
        entity.setDateCreated(created);
        entity.setDateStart(created.minusDays(1));
        entity.setDateEnd(created.plusDays(1));
        entity.setDateLastRun(created.plusHours(2));
        entity.setOwnerId(22);
        entity.setApproverId(23);
        entity.setUpdateId(24);
        entity.setDatasetItemStatusId(DatasetItemStatus.COMPLETED_AND_NONCOMPLETED.getId());
        entity.setShowEventLocation(true);
        entity.setShowCrfStatus(true);
        entity.setShowSecondaryId(true);
        entity.setOdmMetaDataVersionOid("MDV1");
        when(datasetRepository.findById(7)).thenReturn(Optional.of(entity));

        DatasetBean bean = (DatasetBean) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals("Safety", bean.getName());
        assertEquals(3, bean.getStudyId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals("select * from x", bean.getSQLStatement());
        assertEquals(4, bean.getNumRuns());
        assertEquals(22, bean.getOwnerId());
        assertEquals(23, bean.getApproverId());
        assertEquals(24, bean.getUpdaterId());
        assertEquals(DatasetItemStatus.COMPLETED_AND_NONCOMPLETED, bean.getDatasetItemStatus());
        assertTrue(bean.isShowEventLocation());
        assertTrue(bean.isShowCRFstatus());
        assertTrue(bean.isShowSubjectSecondaryId());
        assertEquals("MDV1", bean.getODMMetaDataVersionOid());
        assertTrue(bean.isActive());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyDatasetBean() {
        when(datasetRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(DatasetBean.class, bean);
        assertEquals(0, ((DatasetBean) bean).getId());
    }

    @Test
    void findAllByStudyId_returnsAvailableDatasetsSortedById() {
        when(datasetRepository.findByStudyIdAndStatusId(5, Status.AVAILABLE.getId()))
                .thenReturn(List.of(dataset(3, "C", 5, 1), dataset(1, "A", 5, 1)));

        ArrayList datasets = adapter.findAllByStudyId(5);

        assertEquals(2, datasets.size());
        assertEquals(1, ((DatasetBean) datasets.get(0)).getId());
        assertEquals(3, ((DatasetBean) datasets.get(1)).getId());
    }

    @Test
    void findByNameAndStudy_returnsFirstMatchingDataset() {
        StudyBean study = new StudyBean();
        study.setId(8);
        when(datasetRepository.findByNameAndStudyId("Vitals", 8))
                .thenReturn(List.of(dataset(9, "Vitals", 8, Status.AVAILABLE.getId())));

        DatasetBean bean = (DatasetBean) adapter.findByNameAndStudy("Vitals", study);

        assertEquals(9, bean.getId());
        verify(datasetRepository).findByNameAndStudyId("Vitals", 8);
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        DatasetEntity saved = dataset(11, "Created", 6, Status.AVAILABLE.getId());
        saved.setSqlStatement("where a = 1");
        saved.setDatasetItemStatusId(DatasetItemStatus.COMPLETED.getId());
        when(datasetRepository.save(argThat(e -> {
            assertEquals("Created", e.getName());
            assertEquals("created desc", e.getDescription());
            assertEquals(6, e.getStudyId());
            assertEquals("where a = 1", e.getSqlStatement());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(13, e.getOwnerId());
            assertEquals(14, e.getUpdateId());
            assertEquals(DatasetItemStatus.COMPLETED.getId(), e.getDatasetItemStatusId());
            assertEquals(true, e.getShowEventLocation());
            assertEquals(true, e.getShowCrfVersion());
            return true;
        }))).thenReturn(saved);

        DatasetBean input = new DatasetBean();
        input.setName("Created");
        input.setDescription("created desc");
        input.setStudyId(6);
        input.setSQLStatement("where a = 1");
        input.setStatus(Status.AVAILABLE);
        input.setOwnerId(13);
        input.setUpdaterId(14);
        input.setDatasetItemStatus(DatasetItemStatus.COMPLETED);
        input.setShowEventLocation(true);
        input.setShowCRFversion(true);

        DatasetBean result = (DatasetBean) adapter.create(input);

        assertEquals(11, result.getId());
        verify(datasetRepository).save(argThat(e -> e.getDateCreated() != null));
    }

    @Test
    void updateGroupMap_updatesOnlyDatasetNameAndTimestamp() {
        DatasetEntity existing = dataset(21, "Old", 1, Status.AVAILABLE.getId());
        when(datasetRepository.findById(21)).thenReturn(Optional.of(existing));
        when(datasetRepository.save(argThat(e -> {
            assertEquals(21, e.getDatasetId());
            assertEquals("New", e.getName());
            return e.getDateUpdated() != null;
        }))).thenAnswer(invocation -> invocation.getArgument(0));

        DatasetBean input = new DatasetBean();
        input.setId(21);
        input.setName("New");

        DatasetBean result = (DatasetBean) adapter.updateGroupMap(input);

        assertEquals("New", result.getName());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("dataset_id", 30);
        row.put("name", "From row");
        row.put("description", "row desc");
        row.put("study_id", 31);
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("sql_statement", "row sql");
        row.put("owner_id", 32);
        row.put("update_id", 33);
        row.put("approver_id", 34);
        row.put("num_runs", 35);
        row.put("date_created", now);
        row.put("date_start", now);
        row.put("dataset_item_status_id", DatasetItemStatus.NONCOMPLETED.getId());
        row.put("show_event_location", true);
        row.put("show_crf_status", true);
        row.put("show_secondary_id", true);
        row.put("odm_meta_data_version_name", "MDV");

        DatasetBean bean = (DatasetBean) adapter.getEntityFromHashMap(row);

        assertEquals(30, bean.getId());
        assertEquals("From row", bean.getName());
        assertEquals(31, bean.getStudyId());
        assertEquals("row sql", bean.getSQLStatement());
        assertEquals(DatasetItemStatus.NONCOMPLETED, bean.getDatasetItemStatus());
        assertTrue(bean.isShowEventLocation());
        assertTrue(bean.isShowCRFstatus());
        assertTrue(bean.isShowSubjectSecondaryId());
        assertEquals("MDV", bean.getODMMetaDataVersionName());
    }

    private static DatasetEntity dataset(Integer id, String name, Integer studyId, Integer statusId) {
        DatasetEntity entity = new DatasetEntity();
        entity.setDatasetId(id);
        entity.setName(name);
        entity.setStudyId(studyId);
        entity.setStatusId(statusId);
        return entity;
    }
}
