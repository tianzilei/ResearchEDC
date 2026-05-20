package org.researchedc.module.event.application.command;

import java.time.LocalDateTime;

public class UpdateEventCommand {

    private final Integer studySubjectId;
    private final Integer studyEventDefinitionId;
    private final String location;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer statusId;
    private final Integer subjectEventStatusId;

    public UpdateEventCommand(Integer studySubjectId, Integer studyEventDefinitionId,
                              String location, LocalDateTime startDate,
                              LocalDateTime endDate, Integer statusId,
                              Integer subjectEventStatusId) {
        this.studySubjectId = studySubjectId;
        this.studyEventDefinitionId = studyEventDefinitionId;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statusId = statusId;
        this.subjectEventStatusId = subjectEventStatusId;
    }

    public Integer getStudySubjectId() { return studySubjectId; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public String getLocation() { return location; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public Integer getStatusId() { return statusId; }
    public Integer getSubjectEventStatusId() { return subjectEventStatusId; }
}
