package org.researchedc.module.event.application;

import java.util.List;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.event.application.command.ScheduleEventCommand;
import org.researchedc.module.event.application.command.UpdateEventCommand;
import org.researchedc.module.event.domain.EventDomainService;
import org.researchedc.module.event.domain.EventPolicy;
import org.researchedc.module.event.dto.EventCrfDTO;
import org.researchedc.module.event.dto.EventDefinitionDTO;
import org.researchedc.module.event.dto.ScheduleEventRequest;
import org.researchedc.module.event.dto.StudyEventDTO;
import org.researchedc.module.event.dto.UpdateEventRequest;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.event.EventCompletedEvent;
import org.researchedc.module.event.event.EventScheduledEvent;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.module.event.service.EventService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventApplicationService {

    private final EventService eventService;
    private final EventDomainService eventDomainService;
    private final StudyEventRepository studyEventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public EventApplicationService(EventService eventService,
                                    EventDomainService eventDomainService,
                                    StudyEventRepository studyEventRepository,
                                    ApplicationEventPublisher eventPublisher,
                                    AuditService auditService) {
        this.eventService = eventService;
        this.eventDomainService = eventDomainService;
        this.studyEventRepository = studyEventRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    public List<EventDefinitionDTO> listEventDefinitions(Integer studyId) {
        return eventService.listEventDefinitions(studyId);
    }

    public List<StudyEventDTO> listSubjectEvents(Integer studySubjectId) {
        return eventService.listSubjectEvents(studySubjectId);
    }

    public StudyEventDTO getStudyEvent(Integer studyEventId) {
        return eventService.getStudyEvent(studyEventId);
    }

    public List<EventCrfDTO> listEventCrfs(Integer studyEventId) {
        return eventService.listEventCrfs(studyEventId);
    }

    @Transactional
    public StudyEventDTO scheduleEvent(ScheduleEventCommand command, Integer ownerId) {
        EventPolicy.validateScheduling(
            command.getStudySubjectId(), command.getStudyEventDefinitionId());

        ScheduleEventRequest request = new ScheduleEventRequest();
        request.setStudySubjectId(command.getStudySubjectId());
        request.setStudyEventDefinitionId(command.getStudyEventDefinitionId());
        request.setLocation(command.getLocation());
        request.setStartDate(command.getStartDate());
        request.setEndDate(command.getEndDate());
        request.setStatusId(command.getStatusId());
        request.setSubjectEventStatusId(command.getSubjectEventStatusId());

        StudyEventDTO result = eventService.scheduleEvent(request, ownerId);

        eventPublisher.publishEvent(new EventScheduledEvent(
            this,
            result.getStudyEventId(),
            result.getStudySubjectId(),
            result.getStudyEventDefinitionId()));

        auditService.recordAudit(
                result.getStudySubjectId(), AuditEventType.CREATE, "StudyEvent",
                result.getStudyEventId().longValue(), "Event #" + result.getStudyEventId(),
                null, null, ownerId, null, "event");

        return result;
    }

    @Transactional
    public StudyEventDTO updateEvent(Integer eventId, UpdateEventCommand command,
                                     Integer updaterId) {
        UpdateEventRequest request = new UpdateEventRequest();
        request.setStudySubjectId(command.getStudySubjectId());
        request.setStudyEventDefinitionId(command.getStudyEventDefinitionId());
        request.setLocation(command.getLocation());
        request.setStartDate(command.getStartDate());
        request.setEndDate(command.getEndDate());
        request.setStatusId(command.getStatusId());
        request.setSubjectEventStatusId(command.getSubjectEventStatusId());

        StudyEventDTO result = eventService.updateEvent(eventId, request, updaterId);

        auditService.recordAudit(
                result.getStudySubjectId(), AuditEventType.UPDATE, "StudyEvent",
                result.getStudyEventId().longValue(), "Event #" + result.getStudyEventId(),
                null, null, updaterId, null, "event");

        return result;
    }

    @Transactional
    public void completeEvent(Integer eventId, Integer userId) {
        StudyEventEntity entity = studyEventRepository.findById(eventId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "StudyEvent not found: " + eventId));

        eventDomainService.validateEventCompletion(entity);

        eventService.completeEvent(eventId, userId);

        eventPublisher.publishEvent(new EventCompletedEvent(
            this, eventId, userId));

        auditService.recordAudit(
                null, AuditEventType.UPDATE, "StudyEvent",
                eventId.longValue(), "Event #" + eventId,
                null, "Event completed", userId, null, "event");
    }
}
