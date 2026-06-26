package org.researchedc.module.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.researchedc.module.audit.enums.AuditEventType;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_id")
    private Integer studyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private AuditEventType eventType;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_label", length = 500)
    private String entityLabel;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "performed_by")
    private Integer performedBy;

    @Column(name = "performed_date", nullable = false)
    private LocalDateTime performedDate;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "source_module", length = 100)
    private String sourceModule;

    @PrePersist
    protected void onCreate() {
        if (performedDate == null) {
            performedDate = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

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

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getSourceModule() { return sourceModule; }
    public void setSourceModule(String sourceModule) { this.sourceModule = sourceModule; }
}
