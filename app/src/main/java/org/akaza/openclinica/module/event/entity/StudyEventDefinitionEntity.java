package org.akaza.openclinica.module.event.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleStudyEventDef")
@Table(name = "study_event_definition")
public class StudyEventDefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sed_seq")
    @SequenceGenerator(name = "sed_seq", sequenceName = "study_event_definition_study_event_definition_id_seq", allocationSize = 1)
    @Column(name = "study_event_definition_id")
    private Integer studyEventDefinitionId;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(length = 255)
    private String name;

    @Column(length = 4000)
    private String description;

    @Column
    private Boolean repeating;

    @Column(length = 30)
    private String type;

    @Column(length = 30)
    private String category;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column
    private Integer ordinal;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "status_id")
    private Integer statusId;

    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Boolean getRepeating() { return repeating; }
    public void setRepeating(Boolean v) { this.repeating = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { this.category = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
}
