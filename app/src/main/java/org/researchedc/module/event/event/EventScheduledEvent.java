package org.researchedc.module.event.event;

import java.time.Instant;
import org.springframework.context.ApplicationEvent;

public class EventScheduledEvent extends ApplicationEvent {

    private final Integer studyEventId;
    private final Integer studySubjectId;
    private final Integer definitionId;
    private final Instant occurredAt;

    public EventScheduledEvent(Object source, Integer studyEventId,
                               Integer studySubjectId, Integer definitionId) {
        super(source);
        this.studyEventId = studyEventId;
        this.studySubjectId = studySubjectId;
        this.definitionId = definitionId;
        this.occurredAt = Instant.now();
    }

    public Integer getStudyEventId() { return studyEventId; }
    public Integer getStudySubjectId() { return studySubjectId; }
    public Integer getDefinitionId() { return definitionId; }
    public Instant getOccurredAt() { return occurredAt; }
}
