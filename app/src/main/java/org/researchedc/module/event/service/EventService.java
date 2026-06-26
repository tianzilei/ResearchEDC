package org.researchedc.module.event.service;

import java.time.LocalDateTime;
import java.util.List;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.dto.EventCrfDTO;
import org.researchedc.module.event.dto.EventDefinitionDTO;
import org.researchedc.module.event.dto.ScheduleEventRequest;
import org.researchedc.module.event.dto.StudyEventDTO;
import org.researchedc.module.event.dto.UpdateEventRequest;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.entity.StudyEventDefinitionEntity;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventDefinitionRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final StudyEventRepository studyEventRepository;
    private final StudyEventDefinitionRepository eventDefinitionRepository;
    private final EventCrfRepository eventCrfRepository;
    private final AuditService auditService;

    public EventService(StudyEventRepository studyEventRepository,
                        StudyEventDefinitionRepository eventDefinitionRepository,
                        EventCrfRepository eventCrfRepository,
                        AuditService auditService) {
        this.studyEventRepository = studyEventRepository;
        this.eventDefinitionRepository = eventDefinitionRepository;
        this.eventCrfRepository = eventCrfRepository;
        this.auditService = auditService;
    }

    public List<EventDefinitionDTO> listEventDefinitions(Integer studyId) {
        return eventDefinitionRepository.findByStudyIdOrderByName(studyId)
            .stream()
            .map(this::toDefDto)
            .toList();
    }

    public List<StudyEventDTO> listSubjectEvents(Integer studySubjectId) {
        return studyEventRepository.findByStudySubjectIdOrderByDateStart(studySubjectId)
            .stream()
            .map(this::toEventDto)
            .toList();
    }

    public StudyEventDTO getStudyEvent(Integer studyEventId) {
        StudyEventEntity entity = studyEventRepository.findById(studyEventId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudyEvent not found: " + studyEventId));
        return toEventDto(entity);
    }

    public List<EventCrfDTO> listEventCrfs(Integer studyEventId) {
        return eventCrfRepository.findByStudyEventId(studyEventId)
            .stream()
            .map(this::toCrfDto)
            .toList();
    }

    public EventCrfDTO getEventCrf(Integer crfId) {
        EventCrfEntity entity = eventCrfRepository.findById(crfId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "EventCrf not found: " + crfId));
        return toCrfDto(entity);
    }

    public long countPendingCrfs() {
        return eventCrfRepository.countByDateCompletedIsNull();
    }

    @Transactional
    public StudyEventDTO scheduleEvent(ScheduleEventRequest request, Integer ownerId) {
        if (request.getStudySubjectId() == null) {
            throw new IllegalArgumentException("studySubjectId is required");
        }
        if (request.getStudyEventDefinitionId() == null) {
            throw new IllegalArgumentException("studyEventDefinitionId is required");
        }

        StudyEventEntity entity = new StudyEventEntity();
        entity.setStudySubjectId(request.getStudySubjectId());
        entity.setStudyEventDefinitionId(request.getStudyEventDefinitionId());
        entity.setLocation(request.getLocation());
        entity.setDateStart(request.getStartDate());
        entity.setDateEnd(request.getEndDate());
        entity.setStatusId(request.getStatusId());
        entity.setSubjectEventStatusId(request.getSubjectEventStatusId());
        entity.setDateCreated(LocalDateTime.now());
        entity.setOwnerId(ownerId);

        StudyEventEntity saved = studyEventRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.CREATE, "StudyEvent",
                saved.getStudyEventId().longValue(), "Event #" + saved.getStudyEventId(),
                null, null, ownerId, "Scheduled for subject " + request.getStudySubjectId(), "event");

        return toEventDto(saved);
    }

    @Transactional
    public StudyEventDTO updateEvent(Integer eventId, UpdateEventRequest request, Integer updaterId) {
        StudyEventEntity entity = studyEventRepository.findById(eventId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudyEvent not found: " + eventId));

        if (request.getStudySubjectId() != null) entity.setStudySubjectId(request.getStudySubjectId());
        if (request.getStudyEventDefinitionId() != null) entity.setStudyEventDefinitionId(request.getStudyEventDefinitionId());
        if (request.getLocation() != null) entity.setLocation(request.getLocation());
        if (request.getStartDate() != null) entity.setDateStart(request.getStartDate());
        if (request.getEndDate() != null) entity.setDateEnd(request.getEndDate());
        if (request.getStatusId() != null) entity.setStatusId(request.getStatusId());
        if (request.getSubjectEventStatusId() != null) entity.setSubjectEventStatusId(request.getSubjectEventStatusId());
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(updaterId);

        StudyEventEntity saved = studyEventRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "StudyEvent",
                saved.getStudyEventId().longValue(), "Event #" + saved.getStudyEventId(),
                null, null, updaterId, "Event updated", "event");

        return toEventDto(saved);
    }

    @Transactional
    public void completeEvent(Integer eventId, Integer userId) {
        StudyEventEntity entity = studyEventRepository.findById(eventId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudyEvent not found: " + eventId));

        entity.setStatusId(7);
        entity.setSubjectEventStatusId(7);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        studyEventRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "StudyEvent",
                entity.getStudyEventId().longValue(), "Event #" + entity.getStudyEventId(),
                null, null, userId, "Event completed (status=7)", "event");
    }

    @Transactional
    public void removeStudyEvent(Integer eventId, Integer userId) {
        StudyEventEntity entity = studyEventRepository.findById(eventId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudyEvent not found: " + eventId));

        entity.setStatusId(5);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        studyEventRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.DELETE, "StudyEvent",
                entity.getStudyEventId().longValue(), "Event #" + entity.getStudyEventId(),
                null, null, userId, "Event removed (status=5)", "event");
    }

    @Transactional
    public void restoreStudyEvent(Integer eventId, Integer userId) {
        StudyEventEntity entity = studyEventRepository.findById(eventId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudyEvent not found: " + eventId));

        entity.setStatusId(1);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        studyEventRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "StudyEvent",
                entity.getStudyEventId().longValue(), "Event #" + entity.getStudyEventId(),
                null, null, userId, "Event restored (status=1)", "event");
    }

    @Transactional
    public void removeEventCrf(Integer crfId, Integer userId) {
        EventCrfEntity entity = eventCrfRepository.findById(crfId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "EventCrf not found: " + crfId));

        entity.setStatusId(5);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        eventCrfRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.DELETE, "EventCrf",
                entity.getEventCrfId().longValue(), "EventCrf #" + entity.getEventCrfId(),
                null, null, userId, "Event CRF removed (status=5)", "event");
    }

    @Transactional
    public void restoreEventCrf(Integer crfId, Integer userId) {
        EventCrfEntity entity = eventCrfRepository.findById(crfId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "EventCrf not found: " + crfId));

        entity.setStatusId(1);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        eventCrfRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "EventCrf",
                entity.getEventCrfId().longValue(), "EventCrf #" + entity.getEventCrfId(),
                null, null, userId, "Event CRF restored (status=1)", "event");
    }

    public EventDefinitionDTO getEventDefinition(Integer id) {
        StudyEventDefinitionEntity entity = eventDefinitionRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "EventDefinition not found: " + id));
        return toDefDto(entity);
    }

    @Transactional
    public void removeEventDefinition(Integer id, Integer userId) {
        StudyEventDefinitionEntity entity = eventDefinitionRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "EventDefinition not found: " + id));

        entity.setStatusId(5);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        eventDefinitionRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.DELETE, "EventDefinition",
                entity.getStudyEventDefinitionId().longValue(), entity.getName(),
                null, null, userId, "Definition removed (status=5)", "event");
    }

    @Transactional
    public void restoreEventDefinition(Integer id, Integer userId) {
        StudyEventDefinitionEntity entity = eventDefinitionRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "EventDefinition not found: " + id));

        entity.setStatusId(1);
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(userId);

        eventDefinitionRepository.save(entity);

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "EventDefinition",
                entity.getStudyEventDefinitionId().longValue(), entity.getName(),
                null, null, userId, "Definition restored (status=1)", "event");
    }

    private StudyEventDTO toEventDto(StudyEventEntity e) {
        StudyEventDTO dto = new StudyEventDTO();
        dto.setStudyEventId(e.getStudyEventId());
        dto.setStudySubjectId(e.getStudySubjectId());
        dto.setStudyEventDefinitionId(e.getStudyEventDefinitionId());
        dto.setLocation(e.getLocation());
        dto.setDateStart(e.getDateStart());
        dto.setDateEnd(e.getDateEnd());
        dto.setStatusId(e.getStatusId());
        dto.setSubjectEventStatusId(e.getSubjectEventStatusId());
        dto.setDateCreated(e.getDateCreated());
        dto.setSedOrdinal(e.getSedOrdinal());
        return dto;
    }

    private EventDefinitionDTO toDefDto(StudyEventDefinitionEntity e) {
        EventDefinitionDTO dto = new EventDefinitionDTO();
        dto.setStudyEventDefinitionId(e.getStudyEventDefinitionId());
        dto.setStudyId(e.getStudyId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setRepeating(e.getRepeating());
        dto.setType(e.getType());
        dto.setCategory(e.getCategory());
        dto.setOcOid(e.getOcOid());
        dto.setOrdinal(e.getOrdinal());
        dto.setStatusId(e.getStatusId());
        dto.setDateCreated(e.getDateCreated());
        return dto;
    }

    private EventCrfDTO toCrfDto(EventCrfEntity e) {
        EventCrfDTO dto = new EventCrfDTO();
        dto.setEventCrfId(e.getEventCrfId());
        dto.setStudyEventId(e.getStudyEventId());
        dto.setStudySubjectId(e.getStudySubjectId());
        dto.setCrfVersionId(e.getCrfVersionId());
        dto.setStatusId(e.getStatusId());
        dto.setDateInterviewed(e.getDateInterviewed());
        dto.setInterviewerName(e.getInterviewerName());
        dto.setDateCompleted(e.getDateCompleted());
        dto.setDateValidate(e.getDateValidate());
        dto.setElectronicSignatureStatus(e.getElectronicSignatureStatus());
        dto.setSdvStatus(e.getSdvStatus());
        dto.setDateCreated(e.getDateCreated());
        return dto;
    }
}
