package org.researchedc.module.event.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
import org.researchedc.app.dto.Status;
import org.researchedc.app.dto.StudyEventDto;
import org.researchedc.app.dto.StudyEventDefinitionDto;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.StudyEventRepository;

@ExtendWith(MockitoExtension.class)
class StudyEventDaoAdapterTest {
    private static final int SUBJECT_EVENT_STATUS_SCHEDULED = 1;
    private static final int SUBJECT_EVENT_STATUS_COMPLETED = 4;

    @Mock
    private StudyEventRepository studyEventRepository;

    private StudyEventDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StudyEventDaoAdapter(studyEventRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        StudyEventEntity entity = event(7, 3, 4, Status.AVAILABLE.getId(), SUBJECT_EVENT_STATUS_SCHEDULED, 2);
        entity.setLocation("Clinic A");
        entity.setDateStart(start);
        entity.setDateEnd(start.plusHours(1));
        entity.setStartTimeFlag(true);
        entity.setEndTimeFlag(true);
        entity.setDateCreated(start.minusDays(1));
        entity.setOwnerId(20);
        entity.setUpdateId(21);
        when(studyEventRepository.findById(7)).thenReturn(Optional.of(entity));

        StudyEventDto bean = (StudyEventDto) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(3, bean.getStudySubjectId());
        assertEquals(4, bean.getStudyEventDefinitionId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(SUBJECT_EVENT_STATUS_SCHEDULED, bean.getSubjectEventStatusId());
        assertEquals("Clinic A", bean.getLocation());
        assertEquals(2, bean.getSampleOrdinal());
        assertEquals(true, bean.getStartTimeFlag());
        assertEquals(true, bean.getEndTimeFlag());
        assertEquals(20, bean.getOwnerId());
        assertEquals(21, bean.getUpdaterId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyStudyEventDto() {
        when(studyEventRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(StudyEventDto.class, bean);
        assertEquals(0, ((StudyEventDto) bean).getId());
    }

    @Test
    void findByStudySubjectIdDefinitionIdAndOrdinal_returnsFirstMatch() {
        when(studyEventRepository.findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(10, 20, 2))
                .thenReturn(List.of(event(8, 10, 20, 1, 1, 2)));

        StudyEventDto bean = (StudyEventDto) adapter.findByStudySubjectIdAndDefinitionIdAndOrdinal(10, 20, 2);

        assertEquals(8, bean.getId());
        verify(studyEventRepository).findByStudySubjectIdAndStudyEventDefinitionIdAndSampleOrdinal(10, 20, 2);
    }

    @Test
    void findAllByDefinitionAndSubjectOrderByOrdinal_delegatesToOrderedRepositoryMethod() {
        StudyEventDefinitionDto definition = new StudyEventDefinitionDto();
        definition.setId(20);
        StudySubjectDTO subject = new StudySubjectDTO();
        subject.setId(10);
        when(studyEventRepository.findByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinal(20, 10))
                .thenReturn(List.of(event(2, 10, 20, 1, 1, 1), event(3, 10, 20, 1, 1, 2)));

        ArrayList events = adapter.findAllByDefinitionAndSubjectOrderByOrdinal(definition, subject);

        assertEquals(2, events.size());
        verify(studyEventRepository).findByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinal(20, 10);
    }

    @Test
    void getMaxSampleOrdinal_returnsTopOrdinalOrZero() {
        StudyEventDefinitionDto definition = new StudyEventDefinitionDto();
        definition.setId(20);
        StudySubjectDTO subject = new StudySubjectDTO();
        subject.setId(10);
        when(studyEventRepository.findTopByStudyEventDefinitionIdAndStudySubjectIdOrderBySampleOrdinalDesc(20, 10))
                .thenReturn(Optional.of(event(5, 10, 20, 1, 1, 7)));

        assertEquals(7, adapter.getMaxSampleOrdinal(definition, subject));
    }

    @Test
    void findAllBySubjectIdOrdered_usesDateStartRepositoryOrdering() {
        when(studyEventRepository.findByStudySubjectIdOrderByDateStart(12))
                .thenReturn(List.of(event(1, 12, 20, 1, 1, 1)));

        ArrayList events = adapter.findAllBySubjectIdOrdered(12);

        assertEquals(1, events.size());
        verify(studyEventRepository).findByStudySubjectIdOrderByDateStart(12);
    }

    @Test
    void getDefinitionIdFromStudyEventId_returnsZeroWhenMissing() {
        when(studyEventRepository.findById(404)).thenReturn(Optional.empty());

        assertEquals(0, adapter.getDefinitionIdFromStudyEventId(404));
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        StudyEventEntity saved = event(11, 30, 40, Status.AVAILABLE.getId(), SUBJECT_EVENT_STATUS_SCHEDULED, 1);
        when(studyEventRepository.save(argThat(e -> {
            assertEquals(30, e.getStudySubjectId());
            assertEquals(40, e.getStudyEventDefinitionId());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals(SUBJECT_EVENT_STATUS_SCHEDULED, e.getSubjectEventStatusId());
            assertEquals("Clinic", e.getLocation());
            assertEquals(1, e.getSampleOrdinal());
            assertEquals(31, e.getOwnerId());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        StudyEventDto input = new StudyEventDto();
        input.setStudySubjectId(30);
        input.setStudyEventDefinitionId(40);
        input.setStatus(Status.AVAILABLE);
        input.setSubjectEventStatusId(SUBJECT_EVENT_STATUS_SCHEDULED);
        input.setLocation("Clinic");
        input.setSampleOrdinal(1);
        input.setOwnerId(31);

        StudyEventDto result = (StudyEventDto) adapter.create(input);

        assertEquals(11, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("study_event_id", 50);
        row.put("study_subject_id", 51);
        row.put("study_event_definition_id", 52);
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("subject_event_status_id", SUBJECT_EVENT_STATUS_COMPLETED);
        row.put("location", "Row clinic");
        row.put("sample_ordinal", 3);
        row.put("date_start", now);
        row.put("date_end", now);
        row.put("start_time_flag", true);
        row.put("end_time_flag", false);
        row.put("date_created", now);
        row.put("owner_id", 53);
        row.put("update_id", 54);

        StudyEventDto bean = (StudyEventDto) adapter.getEntityFromHashMap(row);

        assertEquals(50, bean.getId());
        assertEquals(51, bean.getStudySubjectId());
        assertEquals(52, bean.getStudyEventDefinitionId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals(SUBJECT_EVENT_STATUS_COMPLETED, bean.getSubjectEventStatusId());
        assertEquals("Row clinic", bean.getLocation());
        assertEquals(3, bean.getSampleOrdinal());
        assertEquals(true, bean.getStartTimeFlag());
        assertEquals(false, bean.getEndTimeFlag());
        assertEquals(53, bean.getOwnerId());
        assertEquals(54, bean.getUpdaterId());
    }

    private static StudyEventEntity event(Integer id, Integer studySubjectId, Integer definitionId,
                                          Integer statusId, Integer subjectEventStatusId, Integer ordinal) {
        StudyEventEntity entity = new StudyEventEntity();
        entity.setStudyEventId(id);
        entity.setStudySubjectId(studySubjectId);
        entity.setStudyEventDefinitionId(definitionId);
        entity.setStatusId(statusId);
        entity.setSubjectEventStatusId(subjectEventStatusId);
        entity.setSampleOrdinal(ordinal);
        return entity;
    }
}
