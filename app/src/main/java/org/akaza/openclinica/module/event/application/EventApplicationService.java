package org.akaza.openclinica.module.event.application;

import java.util.List;
import org.akaza.openclinica.module.event.application.command.ScheduleEventCommand;
import org.akaza.openclinica.module.event.application.command.UpdateEventCommand;
import org.akaza.openclinica.module.event.domain.EventDomainService;
import org.akaza.openclinica.module.event.domain.EventPolicy;
import org.akaza.openclinica.module.event.dto.EventCrfDTO;
import org.akaza.openclinica.module.event.dto.EventDefinitionDTO;
import org.akaza.openclinica.module.event.dto.ScheduleEventRequest;
import org.akaza.openclinica.module.event.dto.StudyEventDTO;
import org.akaza.openclinica.module.event.dto.UpdateEventRequest;
import org.akaza.openclinica.module.event.entity.StudyEventEntity;
import org.akaza.openclinica.module.event.event.EventCompletedEvent;
import org.akaza.openclinica.module.event.event.EventScheduledEvent;
import org.akaza.openclinica.module.event.repository.StudyEventRepository;
import org.akaza.openclinica.module.event.service.EventService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventApplicationService {

    private final EventService eventService;
    private final EventDomainService eventDomainService;
    private final StudyEventRepository studyEventRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EventApplicationService(EventService eventService,
                                   EventDomainService eventDomainService,
                                   StudyEventRepository studyEventRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.eventService = eventService;
        this.eventDomainService = eventDomainService;
        this.studyEventRepository = studyEventRepository;
        this.eventPublisher = eventPublisher;
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

        return eventService.updateEvent(eventId, request, updaterId);
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
    }
}
