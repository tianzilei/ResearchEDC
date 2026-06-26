package org.researchedc.module.event.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.dto.EventDefinitionDTO;
import org.researchedc.module.event.dto.ScheduleEventRequest;
import org.researchedc.module.event.dto.StudyEventDTO;
import org.researchedc.module.event.dto.UpdateEventRequest;
import org.researchedc.module.event.dto.EventCrfDTO;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private StudyEventRepository studyEventRepository;
    @Mock private StudyEventDefinitionRepository eventDefinitionRepository;
    @Mock private EventCrfRepository eventCrfRepository;
    @Mock private AuditService auditService;

    private EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService(studyEventRepository,
                eventDefinitionRepository, eventCrfRepository, auditService);
    }

    private static StudyEventDefinitionEntity createDef(Integer id, Integer studyId, String name) {
        StudyEventDefinitionEntity e = new StudyEventDefinitionEntity();
        e.setStudyEventDefinitionId(id);
        e.setStudyId(studyId);
        e.setName(name);
        e.setRepeating(false);
        e.setType("common");
        e.setCategory("scheduled");
        return e;
    }

    private static StudyEventEntity createEvent(Integer id, Integer ssId, Integer defId) {
        StudyEventEntity e = new StudyEventEntity();
        e.setStudyEventId(id);
        e.setStudySubjectId(ssId);
        e.setStudyEventDefinitionId(defId);
        e.setStatusId(1);
        e.setSubjectEventStatusId(1);
        return e;
    }

    private static EventCrfEntity createCrf(Integer id, Integer eventId, Integer crfVersionId) {
        EventCrfEntity e = new EventCrfEntity();
        e.setEventCrfId(id);
        e.setStudyEventId(eventId);
        e.setCrfVersionId(crfVersionId);
        e.setStatusId(1);
        return e;
    }

    @Test
    void listEventDefinitions_returnsByStudyId() {
        when(eventDefinitionRepository.findByStudyIdOrderByName(1))
                .thenReturn(List.of(createDef(1, 1, "Screening")));

        List<EventDefinitionDTO> result = service.listEventDefinitions(1);

        assertEquals(1, result.size());
        assertEquals("Screening", result.getFirst().getName());
    }

    @Test
    void listSubjectEvents_returnsBySubjectId() {
        when(studyEventRepository.findByStudySubjectIdOrderByDateStart(100))
                .thenReturn(List.of(createEvent(1, 100, 1)));

        List<StudyEventDTO> result = service.listSubjectEvents(100);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getStudyEventDefinitionId());
    }

    @Test
    void getStudyEvent_whenFound_returnsDto() {
        when(studyEventRepository.findById(1)).thenReturn(
                Optional.of(createEvent(1, 100, 1)));

        StudyEventDTO result = service.getStudyEvent(1);

        assertEquals(1, result.getStudyEventId());
        assertEquals(100, result.getStudySubjectId());
    }

    @Test
    void getStudyEvent_whenNotFound_throwsException() {
        when(studyEventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.getStudyEvent(99));
    }

    @Test
    void listEventCrfs_returnsByStudyEventId() {
        when(eventCrfRepository.findByStudyEventId(1))
                .thenReturn(List.of(createCrf(1, 1, 5)));

        List<EventCrfDTO> result = service.listEventCrfs(1);

        assertEquals(1, result.size());
        assertEquals(5, result.getFirst().getCrfVersionId());
    }

    @Test
    void scheduleEvent_withValidRequest_savesAndReturns() {
        when(studyEventRepository.save(any(StudyEventEntity.class)))
                .thenAnswer(i -> {
                    StudyEventEntity e = i.getArgument(0);
                    if (e.getStudyEventId() == null) e.setStudyEventId(1);
                    return e;
                });

        ScheduleEventRequest request = new ScheduleEventRequest();
        request.setStudySubjectId(100);
        request.setStudyEventDefinitionId(1);
        request.setLocation("Clinic A");
        request.setStartDate(LocalDateTime.now());
        request.setStatusId(1);
        request.setSubjectEventStatusId(1);

        StudyEventDTO result = service.scheduleEvent(request, 42);

        assertEquals(1, result.getStudyEventId());
        assertEquals("Clinic A", result.getLocation());
        verify(studyEventRepository).save(any(StudyEventEntity.class));
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void scheduleEvent_withNullSubjectId_throwsException() {
        ScheduleEventRequest request = new ScheduleEventRequest();
        request.setStudyEventDefinitionId(1);

        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleEvent(request, 42));
    }

    @Test
    void scheduleEvent_withNullDefId_throwsException() {
        ScheduleEventRequest request = new ScheduleEventRequest();
        request.setStudySubjectId(100);

        assertThrows(IllegalArgumentException.class,
                () -> service.scheduleEvent(request, 42));
    }

    @Test
    void updateEvent_updatesFieldsAndReturns() {
        StudyEventEntity existing = createEvent(1, 100, 1);
        existing.setDateCreated(LocalDateTime.now().minusDays(1));
        when(studyEventRepository.findById(1)).thenReturn(Optional.of(existing));
        when(studyEventRepository.save(any(StudyEventEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        UpdateEventRequest request = new UpdateEventRequest();
        request.setLocation("Updated Location");
        request.setStatusId(2);

        StudyEventDTO result = service.updateEvent(1, request, 99);

        assertEquals("Updated Location", result.getLocation());
        assertEquals(2, result.getStatusId());
    }

    @Test
    void updateEvent_whenNotFound_throwsException() {
        when(studyEventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.updateEvent(99, new UpdateEventRequest(), 1));
    }

    @Test
    void completeEvent_setsStatusAndSaves() {
        StudyEventEntity existing = createEvent(1, 100, 1);
        when(studyEventRepository.findById(1)).thenReturn(Optional.of(existing));

        service.completeEvent(1, 42);

        assertEquals(7, existing.getStatusId());
        assertEquals(7, existing.getSubjectEventStatusId());
        verify(studyEventRepository).save(existing);
    }

    @Test
    void completeEvent_whenNotFound_throwsException() {
        when(studyEventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.completeEvent(99, 1));
    }
    @Test
    void removeStudyEvent_setsRemovedStatusAndAudits() {
        StudyEventEntity existing = createEvent(1, 100, 1);
        when(studyEventRepository.findById(1)).thenReturn(Optional.of(existing));

        service.removeStudyEvent(1, 42);

        assertEquals(5, existing.getStatusId());
        assertNotNull(existing.getDateUpdated());
        assertEquals(42, existing.getUpdateId());
        verify(studyEventRepository).save(existing);
        verify(auditService).recordAudit(isNull(), any(), eq("StudyEvent"), eq(1L), eq("Event #1"),
                isNull(), isNull(), eq(42), eq("Event removed (status=5)"), eq("event"));
    }

    @Test
    void restoreStudyEvent_setsAvailableStatusAndAudits() {
        StudyEventEntity existing = createEvent(1, 100, 1);
        existing.setStatusId(5);
        when(studyEventRepository.findById(1)).thenReturn(Optional.of(existing));

        service.restoreStudyEvent(1, 42);

        assertEquals(1, existing.getStatusId());
        assertNotNull(existing.getDateUpdated());
        assertEquals(42, existing.getUpdateId());
        verify(studyEventRepository).save(existing);
        verify(auditService).recordAudit(isNull(), any(), eq("StudyEvent"), eq(1L), eq("Event #1"),
                isNull(), isNull(), eq(42), eq("Event restored (status=1)"), eq("event"));
    }

    @Test
    void removeEventCrf_setsRemovedStatusAndAudits() {
        EventCrfEntity existing = createCrf(2, 1, 5);
        when(eventCrfRepository.findById(2)).thenReturn(Optional.of(existing));

        service.removeEventCrf(2, 42);

        assertEquals(5, existing.getStatusId());
        assertNotNull(existing.getDateUpdated());
        assertEquals(42, existing.getUpdateId());
        verify(eventCrfRepository).save(existing);
        verify(auditService).recordAudit(isNull(), any(), eq("EventCrf"), eq(2L), eq("EventCrf #2"),
                isNull(), isNull(), eq(42), eq("Event CRF removed (status=5)"), eq("event"));
    }

    @Test
    void restoreEventCrf_setsAvailableStatusAndAudits() {
        EventCrfEntity existing = createCrf(2, 1, 5);
        existing.setStatusId(5);
        when(eventCrfRepository.findById(2)).thenReturn(Optional.of(existing));

        service.restoreEventCrf(2, 42);

        assertEquals(1, existing.getStatusId());
        assertNotNull(existing.getDateUpdated());
        assertEquals(42, existing.getUpdateId());
        verify(eventCrfRepository).save(existing);
        verify(auditService).recordAudit(isNull(), any(), eq("EventCrf"), eq(2L), eq("EventCrf #2"),
                isNull(), isNull(), eq(42), eq("Event CRF restored (status=1)"), eq("event"));
    }
}
