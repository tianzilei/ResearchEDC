package org.akaza.openclinica.module.event.event;

import java.time.Instant;
import org.springframework.context.ApplicationEvent;

public class EventCompletedEvent extends ApplicationEvent {

    private final Integer studyEventId;
    private final Integer completedBy;
    private final Instant completedAt;

    public EventCompletedEvent(Object source, Integer studyEventId, Integer completedBy) {
        super(source);
        this.studyEventId = studyEventId;
        this.completedBy = completedBy;
        this.completedAt = Instant.now();
    }

    public Integer getStudyEventId() { return studyEventId; }
    public Integer getCompletedBy() { return completedBy; }
    public Instant getCompletedAt() { return completedAt; }
}
