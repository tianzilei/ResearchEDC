package org.akaza.openclinica.module.subject.event;

import java.time.LocalDateTime;
import org.springframework.context.ApplicationEvent;

public class SubjectEnrolledEvent extends ApplicationEvent {

    private final Integer studySubjectId;
    private final Integer subjectId;
    private final Integer studyId;
    private final LocalDateTime enrolledAt;

    public SubjectEnrolledEvent(Object source, Integer studySubjectId, Integer subjectId,
                                Integer studyId, LocalDateTime enrolledAt) {
        super(source);
        this.studySubjectId = studySubjectId;
        this.subjectId = subjectId;
        this.studyId = studyId;
        this.enrolledAt = enrolledAt;
    }

    public Integer getStudySubjectId() { return studySubjectId; }
    public Integer getSubjectId() { return subjectId; }
    public Integer getStudyId() { return studyId; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
}
