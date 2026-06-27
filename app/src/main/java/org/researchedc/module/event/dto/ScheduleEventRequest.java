package org.researchedc.module.event.dto;

import java.time.LocalDateTime;

public class ScheduleEventRequest {
    private Integer studyId;
    private Integer studySubjectId;
    private Integer studyEventDefinitionId;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer ordinal;
    private Integer statusId;
    private Integer subjectEventStatusId;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer v) { this.studySubjectId = v; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime v) { this.startDate = v; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime v) { this.endDate = v; }
    public Integer getOrdinal() { return ordinal; }
    public void setOrdinal(Integer v) { this.ordinal = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getSubjectEventStatusId() { return subjectEventStatusId; }
    public void setSubjectEventStatusId(Integer v) { this.subjectEventStatusId = v; }
}
