package org.researchedc.module.study.event;

import java.time.Instant;
import org.springframework.context.ApplicationEvent;

public class StudyChangedEvent extends ApplicationEvent {

    public enum ChangeType {
        CREATED,
        UPDATED,
        DELETED,
        STATUS_CHANGED
    }

    private final Integer studyId;
    private final ChangeType changeType;
    private final Integer changedBy;
    private final Instant occurredAt;

    public StudyChangedEvent(Object source, Integer studyId, ChangeType changeType, Integer changedBy) {
        super(source);
        this.studyId = studyId;
        this.changeType = changeType;
        this.changedBy = changedBy;
        this.occurredAt = Instant.now();
    }

    public Integer getStudyId() {
        return studyId;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Integer getChangedBy() {
        return changedBy;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    @Override
    public String toString() {
        return "StudyChangedEvent{studyId=" + studyId
                + ", changeType=" + changeType
                + ", changedBy=" + changedBy
                + ", occurredAt=" + occurredAt + '}';
    }
}
