package org.researchedc.module.event.dto;

import java.time.LocalDateTime;

public class StudyEventDTO {
    private Integer studyEventId;
    private Integer studySubjectId;
    private Integer studyEventDefinitionId;
    private String eventDefinitionName;
    private String location;
    private LocalDateTime dateStart;
    private LocalDateTime dateEnd;
    private Integer statusId;
    private Integer subjectEventStatusId;
    private LocalDateTime dateCreated;
    private Integer sedOrdinal;

    public Integer getStudyEventId() { return studyEventId; }
    public void setStudyEventId(Integer v) { this.studyEventId = v; }
    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer v) { this.studySubjectId = v; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public String getEventDefinitionName() { return eventDefinitionName; }
    public void setEventDefinitionName(String v) { this.eventDefinitionName = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime v) { this.dateStart = v; }
    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime v) { this.dateEnd = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getSubjectEventStatusId() { return subjectEventStatusId; }
    public void setSubjectEventStatusId(Integer v) { this.subjectEventStatusId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public Integer getSedOrdinal() { return sedOrdinal; }
    public void setSedOrdinal(Integer v) { this.sedOrdinal = v; }
}
