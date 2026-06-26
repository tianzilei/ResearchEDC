package org.researchedc.module.subject.dto;

import java.time.LocalDateTime;

public class EnrollSubjectRequest {
    private Integer studyId;
    private Integer subjectId;
    private String label;
    private String secondaryLabel;
    private LocalDateTime enrollmentDate;
    private String ocOid;
    private Integer eventDefinitionId;
    private String eventLocation;
    private LocalDateTime eventStartDate;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer v) { this.subjectId = v; }
    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }
    public String getSecondaryLabel() { return secondaryLabel; }
    public void setSecondaryLabel(String v) { this.secondaryLabel = v; }
    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime v) { this.enrollmentDate = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public Integer getEventDefinitionId() { return eventDefinitionId; }
    public void setEventDefinitionId(Integer v) { this.eventDefinitionId = v; }
    public String getEventLocation() { return eventLocation; }
    public void setEventLocation(String v) { this.eventLocation = v; }
    public LocalDateTime getEventStartDate() { return eventStartDate; }
    public void setEventStartDate(LocalDateTime v) { this.eventStartDate = v; }
}
