package org.akaza.openclinica.module.audit.event;

import java.time.Instant;
import org.akaza.openclinica.module.audit.enums.AuditEventType;
import org.springframework.context.ApplicationEvent;

public class AuditRecordedEvent extends ApplicationEvent {

    private final Long auditLogId;
    private final Integer studyId;
    private final AuditEventType eventType;
    private final String entityType;
    private final Long entityId;
    private final Integer performedBy;
    private final Instant occurredAt;

    public AuditRecordedEvent(Object source, Long auditLogId, Integer studyId,
                              AuditEventType eventType, String entityType,
                              Long entityId, Integer performedBy) {
        super(source);
        this.auditLogId = auditLogId;
        this.studyId = studyId;
        this.eventType = eventType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.occurredAt = Instant.now();
    }

    public Long getAuditLogId() { return auditLogId; }
    public Integer getStudyId() { return studyId; }
    public AuditEventType getEventType() { return eventType; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public Integer getPerformedBy() { return performedBy; }
    public Instant getOccurredAt() { return occurredAt; }
}
