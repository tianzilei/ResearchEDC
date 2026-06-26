package org.researchedc.module.event.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleStudyEvent")
@Table(name = "module_study_event")
public class StudyEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_seq")
    @SequenceGenerator(name = "se_seq", sequenceName = "module_study_event_id_seq", allocationSize = 1)
    @Column(name = "study_event_id")
    private Integer studyEventId;

    @Column(name = "study_subject_id")
    private Integer studySubjectId;

    @Column(name = "study_event_definition_id")
    private Integer studyEventDefinitionId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "subject_event_status_id")
    private Integer subjectEventStatusId;

    @Column(length = 2000)
    private String location;

    @Column(name = "sample_ordinal")
    private Integer sampleOrdinal;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @Column(name = "start_time_flag")
    private Boolean startTimeFlag;

    @Column(name = "end_time_flag")
    private Boolean endTimeFlag;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "sed_ordinal")
    private Integer sedOrdinal;

    public Integer getStudyEventId() { return studyEventId; }
    public void setStudyEventId(Integer v) { this.studyEventId = v; }
    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer v) { this.studySubjectId = v; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getSubjectEventStatusId() { return subjectEventStatusId; }
    public void setSubjectEventStatusId(Integer v) { this.subjectEventStatusId = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public Integer getSampleOrdinal() { return sampleOrdinal; }
    public void setSampleOrdinal(Integer v) { this.sampleOrdinal = v; }
    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime v) { this.dateStart = v; }
    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime v) { this.dateEnd = v; }
    public Boolean getStartTimeFlag() { return startTimeFlag; }
    public void setStartTimeFlag(Boolean v) { this.startTimeFlag = v; }
    public Boolean getEndTimeFlag() { return endTimeFlag; }
    public void setEndTimeFlag(Boolean v) { this.endTimeFlag = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
    public Integer getSedOrdinal() { return sedOrdinal; }
    public void setSedOrdinal(Integer v) { this.sedOrdinal = v; }
}
