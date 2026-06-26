package org.researchedc.app.dto;

import java.util.Date;

public class StudyEventDto extends AuditableEntity {
    public static final int SUBJECT_EVENT_STATUS_SCHEDULED = 1;

    private int studyEventDefinitionId;
    private int studySubjectId;
    private String location;
    private int sampleOrdinal;
    private Date dateStarted;
    private Date dateEnded;
    private int subjectEventStatusId;
    private boolean startTimeFlag;
    private boolean endTimeFlag;

    public StudyEventDto() {
        location = "";
        subjectEventStatusId = SUBJECT_EVENT_STATUS_SCHEDULED;
    }

    public int getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(int v) { this.studyEventDefinitionId = v; }
    public int getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(int v) { this.studySubjectId = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { this.location = v; }
    public int getSampleOrdinal() { return sampleOrdinal; }
    public void setSampleOrdinal(int v) { this.sampleOrdinal = v; }
    public Date getDateStarted() { return dateStarted; }
    public void setDateStarted(Date v) { this.dateStarted = v; }
    public Date getDateEnded() { return dateEnded; }
    public void setDateEnded(Date v) { this.dateEnded = v; }
    public int getSubjectEventStatusId() { return subjectEventStatusId; }
    public void setSubjectEventStatusId(int v) { this.subjectEventStatusId = v; }
    public boolean getStartTimeFlag() { return startTimeFlag; }
    public void setStartTimeFlag(boolean v) { this.startTimeFlag = v; }
    public boolean getEndTimeFlag() { return endTimeFlag; }
    public void setEndTimeFlag(boolean v) { this.endTimeFlag = v; }
}
