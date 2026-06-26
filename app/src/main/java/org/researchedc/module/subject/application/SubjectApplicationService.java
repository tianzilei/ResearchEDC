package org.researchedc.module.subject.application;

import java.time.LocalDateTime;
import java.util.List;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.subject.application.command.CreateSubjectCommand;
import org.researchedc.module.subject.application.command.EnrollSubjectCommand;
import org.researchedc.module.subject.domain.SubjectPolicy;
import org.researchedc.module.subject.dto.CreateSubjectRequest;
import org.researchedc.module.subject.dto.EnrollSubjectRequest;
import org.researchedc.app.dto.StudySubjectDTO;
import org.researchedc.module.subject.dto.SubjectDTO;
import org.researchedc.module.subject.event.SubjectEnrolledEvent;
import org.researchedc.module.subject.service.SubjectService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubjectApplicationService {

    private final SubjectService subjectService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public SubjectApplicationService(SubjectService subjectService,
                                      ApplicationEventPublisher eventPublisher,
                                      AuditService auditService) {
        this.subjectService = subjectService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    public List<SubjectDTO> searchSubjects(String query) {
        return subjectService.searchSubjects(query);
    }

    public SubjectDTO getSubject(Integer subjectId) {
        return subjectService.getSubject(subjectId);
    }

    public List<StudySubjectDTO> listStudySubjects(Integer studyId) {
        return subjectService.listStudySubjects(studyId);
    }

    public StudySubjectDTO getStudySubject(Integer studySubjectId) {
        return subjectService.getStudySubject(studySubjectId);
    }

    @Transactional
    public SubjectDTO createSubject(CreateSubjectCommand command, Integer ownerId) {
        SubjectPolicy.validateCreateSubject(command);

        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setUniqueIdentifier(command.getUniqueIdentifier());
        request.setDateOfBirth(command.getDateOfBirth());
        request.setGender(command.getGender());
        request.setDobCollected(command.getDobCollected());

        SubjectDTO result = subjectService.createSubject(request, ownerId);

        auditService.recordAudit(
                null, AuditEventType.CREATE, "Subject",
                result.getSubjectId().longValue(), result.getUniqueIdentifier(),
                null, null, ownerId, null, "subject");

        return result;
    }

    @Transactional
    public StudySubjectDTO enrollSubject(EnrollSubjectCommand command, Integer ownerId) {
        SubjectPolicy.validateEnrollSubject(command);

        EnrollSubjectRequest request = new EnrollSubjectRequest();
        request.setStudyId(command.getStudyId());
        request.setSubjectId(command.getSubjectId());
        request.setLabel(command.getLabel());
        request.setSecondaryLabel(command.getSecondaryLabel());
        request.setEnrollmentDate(command.getEnrollmentDate());
        request.setOcOid(command.getOcOid());

        StudySubjectDTO result = subjectService.enrollSubject(request, ownerId);

        eventPublisher.publishEvent(new SubjectEnrolledEvent(
            this,
            result.getStudySubjectId(),
            result.getSubjectId(),
            result.getStudyId(),
            LocalDateTime.now()
        ));

        auditService.recordAudit(
                result.getStudyId(), AuditEventType.ASSIGN, "StudySubject",
                result.getStudySubjectId().longValue(), result.getLabel(),
                null, null, ownerId, null, "subject");

        return result;
    }
}
