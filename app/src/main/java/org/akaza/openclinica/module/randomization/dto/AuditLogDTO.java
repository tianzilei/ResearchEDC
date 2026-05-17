package org.akaza.openclinica.module.randomization.dto;

import java.time.LocalDateTime;
import org.akaza.openclinica.module.randomization.enums.AuditAction;

public class AuditLogDTO {

    private Long id;
    private Long schemeId;
    private Integer studyId;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private Integer performedBy;
    private LocalDateTime performedDate;
    private String details;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public AuditAction getAction() { return action; }
    public void setAction(AuditAction action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

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
}
