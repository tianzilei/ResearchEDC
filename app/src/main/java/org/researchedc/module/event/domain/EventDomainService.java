package org.researchedc.module.event.domain;

import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.springframework.stereotype.Component;

@Component
public class EventDomainService {

    private final StudyEventRepository studyEventRepository;

    public EventDomainService(StudyEventRepository studyEventRepository) {
        this.studyEventRepository = studyEventRepository;
    }

    public int calculateEventOrdinal(Integer studySubjectId, Integer studyEventDefinitionId) {
        java.util.List<StudyEventEntity> existing = studyEventRepository
            .findByStudySubjectIdOrderByDateStart(studySubjectId);
        long count = existing.stream()
            .filter(e -> studyEventDefinitionId.equals(e.getStudyEventDefinitionId()))
            .count();
        return (int) count + 1;
    }

    public void validateEventCompletion(StudyEventEntity event) {
        EventPolicy.validateCompletion(event.getStatusId());
    }
}
