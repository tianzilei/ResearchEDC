package org.researchedc.module.recruit.dto;

import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;

public class ConvertCandidateResultDTO {
    private CandidateDTO candidate;
    private SubjectDTO subject;
    private StudySubjectDTO studySubject;

    public CandidateDTO getCandidate() { return candidate; }
    public void setCandidate(CandidateDTO candidate) { this.candidate = candidate; }

    public SubjectDTO getSubject() { return subject; }
    public void setSubject(SubjectDTO subject) { this.subject = subject; }

    public StudySubjectDTO getStudySubject() { return studySubject; }
    public void setStudySubject(StudySubjectDTO studySubject) { this.studySubject = studySubject; }
}
