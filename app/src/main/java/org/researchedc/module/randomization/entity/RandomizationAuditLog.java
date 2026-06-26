package org.researchedc.module.randomization.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.researchedc.module.randomization.enums.AuditAction;

@Entity
@Table(name = "randomization_audit_log")
public class RandomizationAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheme_id")
    private Long schemeId;

    @Column(name = "study_id")
    private Integer studyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", length = 4000)
    private String oldValue;

    @Column(name = "new_value", length = 4000)
    private String newValue;

    @Column(name = "performed_by")
    private Integer performedBy;

    @Column(name = "performed_date", nullable = false)
    private LocalDateTime performedDate;

    @Column(length = 4000)
    private String details;

    @PrePersist
    protected void onCreate() {
        performedDate = LocalDateTime.now();
    }

    public Long getId() { return id; }

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

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
