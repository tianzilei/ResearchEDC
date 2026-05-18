package org.akaza.openclinica.module.event.service;

import java.util.List;
import org.akaza.openclinica.module.event.dto.EventCrfDTO;
import org.akaza.openclinica.module.event.dto.EventDefinitionDTO;
import org.akaza.openclinica.module.event.dto.StudyEventDTO;
import org.akaza.openclinica.module.event.entity.EventCrfEntity;
import org.akaza.openclinica.module.event.entity.StudyEventDefinitionEntity;
import org.akaza.openclinica.module.event.entity.StudyEventEntity;
import org.akaza.openclinica.module.event.repository.EventCrfRepository;
import org.akaza.openclinica.module.event.repository.StudyEventDefinitionRepository;
import org.akaza.openclinica.module.event.repository.StudyEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EventService {

    private final StudyEventRepository studyEventRepository;
    private final StudyEventDefinitionRepository eventDefinitionRepository;
    private final EventCrfRepository eventCrfRepository;

    public EventService(StudyEventRepository studyEventRepository,
                        StudyEventDefinitionRepository eventDefinitionRepository,
                        EventCrfRepository eventCrfRepository) {
        this.studyEventRepository = studyEventRepository;
        this.eventDefinitionRepository = eventDefinitionRepository;
        this.eventCrfRepository = eventCrfRepository;
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
