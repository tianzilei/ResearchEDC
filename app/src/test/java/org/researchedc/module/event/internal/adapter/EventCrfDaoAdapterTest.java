package org.researchedc.module.event.internal.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.researchedc.app.dto.Status;
import org.researchedc.app.dto.StudyEventDto;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.app.dto.CrfVersionDTO;
import org.researchedc.app.dto.EventCrfDto;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.repository.EventCrfRepository;

@ExtendWith(MockitoExtension.class)
class EventCrfDaoAdapterTest {

    @Mock
    private EventCrfRepository eventCrfRepository;

    private EventCrfDaoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EventCrfDaoAdapter(eventCrfRepository);
    }

    @Test
    void findByPK_whenFound_mapsEntityToLegacyBean() {
        LocalDateTime interviewed = LocalDateTime.now().minusDays(2);
        EventCrfEntity entity = eventCrf(7, 3, 4, 5, Status.AVAILABLE.getId());
        entity.setDateInterviewed(interviewed);
        entity.setInterviewerName("Nurse");
        entity.setAnnotations("notes");
        entity.setDateCompleted(interviewed.plusHours(1));
        entity.setValidatorId(20);
        entity.setValidatorAnnotations("valid");
        entity.setDateCreated(interviewed.minusDays(1));
        entity.setOwnerId(21);
        entity.setUpdateId(22);
        entity.setElectronicSignatureStatus(true);
        entity.setSdvStatus(true);
        entity.setSdvUpdateId(23);
        when(eventCrfRepository.findById(7)).thenReturn(Optional.of(entity));

        EventCrfDto bean = (EventCrfDto) adapter.findByPK(7);

        assertEquals(7, bean.getId());
        assertEquals(3, bean.getStudyEventId());
        assertEquals(4, bean.getStudySubjectId());
        assertEquals(5, bean.getCRFVersionId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals("Nurse", bean.getInterviewerName());
        assertEquals("notes", bean.getAnnotations());
        assertEquals(20, bean.getValidatorId());
        assertEquals("valid", bean.getValidatorAnnotations());
        assertEquals(21, bean.getOwnerId());
        assertEquals(22, bean.getUpdaterId());
        assertTrue(bean.isElectronicSignatureStatus());
        assertTrue(bean.isSdvStatus());
        assertEquals(23, bean.getSdvUpdateId());
    }

    @Test
    void findByPK_whenMissing_returnsEmptyEventCrfBean() {
        when(eventCrfRepository.findById(404)).thenReturn(Optional.empty());

        Object bean = adapter.findByPK(404);

        assertInstanceOf(EventCrfDto.class, bean);
        assertEquals(0, ((EventCrfDto) bean).getId());
    }

    @Test
    void findAllByStudyEventAndStatus_usesStudyEventAndStatusRepositoryLookup() {
        StudyEventDto studyEvent = new StudyEventDto();
        studyEvent.setId(3);
        when(eventCrfRepository.findByStudyEventIdAndStatusId(3, Status.AVAILABLE.getId()))
                .thenReturn(List.of(eventCrf(8, 3, 4, 5, Status.AVAILABLE.getId())));

        ArrayList result = adapter.findAllByStudyEventAndStatus(studyEvent, Status.AVAILABLE);

        assertEquals(1, result.size());
        verify(eventCrfRepository).findByStudyEventIdAndStatusId(3, Status.AVAILABLE.getId());
    }

    @Test
    void findAllByStudyEventAndCrfOrCrfVersionOid_parsesNumericVersionId() {
        StudyEventDto studyEvent = new StudyEventDto();
        studyEvent.setId(3);
        when(eventCrfRepository.findByStudyEventIdAndCrfVersionId(3, 5))
                .thenReturn(List.of(eventCrf(8, 3, 4, 5, Status.AVAILABLE.getId())));

        ArrayList result = adapter.findAllByStudyEventAndCrfOrCrfVersionOid(studyEvent, "5");

        assertEquals(1, result.size());
        verify(eventCrfRepository).findByStudyEventIdAndCrfVersionId(3, 5);
    }

    @Test
    void findAllByStudyEventAndCrfOrCrfVersionOid_returnsEmptyForNonNumericOid() {
        StudyEventDto studyEvent = new StudyEventDto();
        studyEvent.setId(3);

        ArrayList result = adapter.findAllByStudyEventAndCrfOrCrfVersionOid(studyEvent, "CV_1");

        assertEquals(0, result.size());
    }

    @Test
    void findByEventSubjectVersion_usesCompositeRepositoryLookup() {
        StudyEventDto studyEvent = new StudyEventDto();
        studyEvent.setId(3);
        StudySubjectDTO studySubject = new StudySubjectDTO();
        studySubject.setId(4);
        CrfVersionDTO version = new CrfVersionDTO();
        version.setId(5);
        when(eventCrfRepository.findByStudyEventIdAndStudySubjectIdAndCrfVersionId(3, 4, 5))
                .thenReturn(List.of(eventCrf(9, 3, 4, 5, Status.AVAILABLE.getId())));

        ArrayList result = adapter.findByEventSubjectVersion(studyEvent, studySubject, version);

        assertEquals(1, result.size());
        verify(eventCrfRepository).findByStudyEventIdAndStudySubjectIdAndCrfVersionId(3, 4, 5);
    }

    @Test
    void findByEventCrfVersion_returnsFirstMatchOrNull() {
        StudyEventDto studyEvent = new StudyEventDto();
        studyEvent.setId(3);
        CrfVersionDTO version = new CrfVersionDTO();
        version.setId(5);
        when(eventCrfRepository.findByStudyEventIdAndCrfVersionId(3, 5))
                .thenReturn(List.of(eventCrf(10, 3, 4, 5, Status.AVAILABLE.getId())));

        EventCrfDto bean = adapter.findByEventCrfVersion(studyEvent, version);

        assertEquals(10, bean.getId());
    }

    @Test
    void findByEventCrfVersion_whenMissing_returnsNullLikeLegacyDao() {
        StudyEventDto studyEvent = new StudyEventDto();
        studyEvent.setId(3);
        CrfVersionDTO version = new CrfVersionDTO();
        version.setId(5);
        when(eventCrfRepository.findByStudyEventIdAndCrfVersionId(3, 5)).thenReturn(List.of());

        assertNull(adapter.findByEventCrfVersion(studyEvent, version));
    }

    @Test
    void findUndeletedWithStudySubjectsByCRFVersion_filtersDeletedStatuses() {
        when(eventCrfRepository.findByCrfVersionId(5)).thenReturn(List.of(
                eventCrf(1, 3, 4, 5, Status.AVAILABLE.getId()),
                eventCrf(2, 3, 4, 5, Status.DELETED.getId()),
                eventCrf(3, 3, 4, 5, Status.AUTO_DELETED.getId())));

        ArrayList result = adapter.findUndeletedWithStudySubjectsByCRFVersion(5);

        assertEquals(1, result.size());
        assertEquals(1, ((EventCrfDto) result.get(0)).getId());
    }

    @Test
    void create_mapsLegacyBeanToModuleEntity() {
        EventCrfEntity saved = eventCrf(11, 3, 4, 5, Status.AVAILABLE.getId());
        when(eventCrfRepository.save(argThat(e -> {
            assertEquals(3, e.getStudyEventId());
            assertEquals(4, e.getStudySubjectId());
            assertEquals(5, e.getCrfVersionId());
            assertEquals(Status.AVAILABLE.getId(), e.getStatusId());
            assertEquals("Nurse", e.getInterviewerName());
            assertEquals("notes", e.getAnnotations());
            assertEquals(31, e.getOwnerId());
            assertEquals(32, e.getUpdateId());
            assertEquals(true, e.getElectronicSignatureStatus());
            assertEquals(true, e.getSdvStatus());
            return e.getDateCreated() != null;
        }))).thenReturn(saved);

        EventCrfDto input = new EventCrfDto();
        input.setStudyEventId(3);
        input.setStudySubjectId(4);
        input.setCRFVersionId(5);
        input.setStatus(Status.AVAILABLE);
        input.setInterviewerName("Nurse");
        input.setAnnotations("notes");
        input.setOwnerId(31);
        input.setUpdaterId(32);
        input.setElectronicSignatureStatus(true);
        input.setSdvStatus(true);

        EventCrfDto result = (EventCrfDto) adapter.create(input);

        assertEquals(11, result.getId());
    }

    @Test
    void getEntityFromHashMap_mapsLegacyRow() {
        Date now = new Date();
        HashMap row = new HashMap();
        row.put("event_crf_id", 50);
        row.put("study_event_id", 51);
        row.put("study_subject_id", 52);
        row.put("crf_version_id", 53);
        row.put("status_id", Status.AVAILABLE.getId());
        row.put("date_interviewed", now);
        row.put("interviewer_name", "Row nurse");
        row.put("annotations", "Row notes");
        row.put("date_completed", now);
        row.put("validator_id", 54);
        row.put("validator_annotations", "Row valid");
        row.put("date_created", now);
        row.put("owner_id", 55);
        row.put("update_id", 56);
        row.put("electronic_signature_status", true);
        row.put("sdv_status", true);
        row.put("sdv_update_id", 57);

        EventCrfDto bean = (EventCrfDto) adapter.getEntityFromHashMap(row);

        assertEquals(50, bean.getId());
        assertEquals(51, bean.getStudyEventId());
        assertEquals(52, bean.getStudySubjectId());
        assertEquals(53, bean.getCRFVersionId());
        assertEquals(Status.AVAILABLE, bean.getStatus());
        assertEquals("Row nurse", bean.getInterviewerName());
        assertEquals("Row notes", bean.getAnnotations());
        assertEquals(54, bean.getValidatorId());
        assertEquals("Row valid", bean.getValidatorAnnotations());
        assertEquals(55, bean.getOwnerId());
        assertEquals(56, bean.getUpdaterId());
        assertTrue(bean.isElectronicSignatureStatus());
        assertTrue(bean.isSdvStatus());
        assertEquals(57, bean.getSdvUpdateId());
    }

    private static EventCrfEntity eventCrf(Integer id, Integer studyEventId, Integer studySubjectId,
                                           Integer versionId, Integer statusId) {
        EventCrfEntity entity = new EventCrfEntity();
        entity.setEventCrfId(id);
        entity.setStudyEventId(studyEventId);
        entity.setStudySubjectId(studySubjectId);
        entity.setCrfVersionId(versionId);
        entity.setStatusId(statusId);
        return entity;
    }
}
