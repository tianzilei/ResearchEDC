package org.akaza.openclinica.module.audit.dto;

import java.time.LocalDateTime;
import org.akaza.openclinica.module.audit.enums.AuditEventType;

public class AuditLogDTO {

    private Long id;
    private Integer studyId;
    private AuditEventType eventType;
    private String entityType;
    private Long entityId;
    private String entityLabel;
    private String oldValue;
    private String newValue;
    private Integer performedBy;
    private LocalDateTime performedDate;
    private String details;
    private String sourceModule;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public AuditEventType getEventType() { return eventType; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityLabel() { return entityLabel; }
    public void setEntityLabel(String entityLabel) { this.entityLabel = entityLabel; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public Integer getPerformedBy() { return performedBy; }
    public void setPerformedBy(Integer performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedDate() { return performedDate; }
    public void setPerformedDate(LocalDateTime performedDate) { this.performedDate = performedDate; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }
}
