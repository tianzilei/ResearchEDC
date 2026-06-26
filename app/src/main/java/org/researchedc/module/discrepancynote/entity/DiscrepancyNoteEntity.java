package org.researchedc.module.discrepancynote.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleDiscrepancyNote")
@Table(name = "module_discrepancy_note")
public class DiscrepancyNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_discrepancy_note_seq")
    @SequenceGenerator(name = "module_discrepancy_note_seq", sequenceName = "module_discrepancy_note_id_seq", allocationSize = 1)
    @Column(name = "discrepancy_note_id")
    private Integer discrepancyNoteId;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "discrepancy_note_type_id")
    private Integer discrepancyNoteTypeId;

    @Column(name = "resolution_status_id")
    private Integer resolutionStatusId;

    @Column(name = "detailed_notes", length = 1000)
    private String detailedNotes;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "parent_dn_id")
    private Integer parentDnId;

    @Column(name = "entity_type", length = 30)
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "assigned_user_id")
    private Integer assignedUserId;

    @PrePersist
    protected void onCreate() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }

    public Integer getDiscrepancyNoteId() { return discrepancyNoteId; }
    public void setDiscrepancyNoteId(Integer discrepancyNoteId) { this.discrepancyNoteId = discrepancyNoteId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getDiscrepancyNoteTypeId() { return discrepancyNoteTypeId; }
    public void setDiscrepancyNoteTypeId(Integer discrepancyNoteTypeId) { this.discrepancyNoteTypeId = discrepancyNoteTypeId; }

    public Integer getResolutionStatusId() { return resolutionStatusId; }
    public void setResolutionStatusId(Integer resolutionStatusId) { this.resolutionStatusId = resolutionStatusId; }

    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String detailedNotes) { this.detailedNotes = detailedNotes; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public Integer getParentDnId() { return parentDnId; }
    public void setParentDnId(Integer parentDnId) { this.parentDnId = parentDnId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Integer assignedUserId) { this.assignedUserId = assignedUserId; }
}
