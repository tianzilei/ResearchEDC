package org.researchedc.module.study.application;

import java.util.List;
import java.util.NoSuchElementException;
import org.researchedc.module.study.application.command.CreateStudyCommand;
import org.researchedc.module.study.application.command.UpdateStudyCommand;
import org.researchedc.module.study.domain.StudyDomainService;
import org.researchedc.module.study.domain.StudyPolicy;
import org.researchedc.module.study.dto.CreateStudyRequest;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.dto.StudySummaryDTO;
import org.researchedc.module.study.dto.UpdateStudyRequest;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.study.event.StudyChangedEvent;

import org.researchedc.module.study.service.StudyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudyApplicationService {

    private static final Logger log = LoggerFactory.getLogger(StudyApplicationService.class);

    private final StudyService studyService;
    private final StudyPolicy studyPolicy;
    private final StudyDomainService studyDomainService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public StudyApplicationService(StudyService studyService,
                                    StudyPolicy studyPolicy,
                                    StudyDomainService studyDomainService,
                                    ApplicationEventPublisher eventPublisher,
                                    AuditService auditService) {
        this.studyService = studyService;
        this.studyPolicy = studyPolicy;
        this.studyDomainService = studyDomainService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    public List<StudySummaryDTO> listStudies() {
        return studyService.listStudies();
    }

    public List<StudySummaryDTO> listSites(Integer parentStudyId) {
        return studyService.listSites(parentStudyId);
    }

    public List<StudySummaryDTO> searchByName(String name) {
        return studyService.searchByName(name);
    }

    public StudyDetailDTO getStudy(Integer studyId) {
        return studyService.getStudy(studyId);
    }

    @Transactional
    public StudyDetailDTO createStudy(CreateStudyCommand command, Integer ownerId) {
        List<String> errors = studyPolicy.validateCreate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        CreateStudyRequest request = toCreateRequest(command);
        StudyDetailDTO result = studyService.createStudy(request, ownerId);

        eventPublisher.publishEvent(
                new StudyChangedEvent(this, result.getStudyId(),
                        StudyChangedEvent.ChangeType.CREATED, ownerId));

        auditService.recordAudit(
                result.getStudyId(), AuditEventType.CREATE, "Study",
                result.getStudyId().longValue(), result.getName(),
                null, null, ownerId, null, "study");

        return result;
    }

    @Transactional
    public StudyDetailDTO updateStudy(UpdateStudyCommand command) {
        List<String> errors = studyPolicy.validateUpdate(command);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        UpdateStudyRequest request = toUpdateRequest(command);
        StudyDetailDTO result = studyService.updateStudy(
                command.getStudyId(), request, command.getUpdatedBy());

        eventPublisher.publishEvent(
                new StudyChangedEvent(this, command.getStudyId(),
                        StudyChangedEvent.ChangeType.UPDATED, command.getUpdatedBy()));

        auditService.recordAudit(
                result.getStudyId(), AuditEventType.UPDATE, "Study",
                result.getStudyId().longValue(), result.getName(),
                null, null, command.getUpdatedBy(), null, "study");

        return result;
    }

    @Transactional
    public void deleteStudy(Integer studyId, Integer userId) {
        studyService.deleteStudy(studyId, userId);

        eventPublisher.publishEvent(
                new StudyChangedEvent(this, studyId,
                        StudyChangedEvent.ChangeType.DELETED, userId));

        auditService.recordAudit(
                studyId, AuditEventType.DELETE, "Study",
                studyId.longValue(), "Study #" + studyId,
                null, null, userId, null, "study");
    }

    @Transactional
    public void updateStudyStatus(Integer studyId, Integer statusId, Integer userId) {
        studyService.updateStudyStatus(studyId, statusId, userId);

        eventPublisher.publishEvent(
                new StudyChangedEvent(this, studyId,
                        StudyChangedEvent.ChangeType.STATUS_CHANGED, userId));

        auditService.recordAudit(
                studyId, AuditEventType.UPDATE, "Study",
                studyId.longValue(), "Study #" + studyId,
                null, "Status changed to: " + statusId, userId, null, "study");
    }

    private CreateStudyRequest toCreateRequest(CreateStudyCommand cmd) {
        CreateStudyRequest r = new CreateStudyRequest();
        r.setName(cmd.getName());
        r.setUniqueIdentifier(cmd.getUniqueIdentifier());
        r.setSecondaryIdentifier(cmd.getSecondaryIdentifier());
        r.setSummary(cmd.getSummary());
        r.setDatePlannedStart(cmd.getDatePlannedStart());
        r.setDatePlannedEnd(cmd.getDatePlannedEnd());
        r.setTypeId(cmd.getTypeId());
        r.setStatusId(cmd.getStatusId());
        r.setPrincipalInvestigator(cmd.getPrincipalInvestigator());
        r.setFacilityName(cmd.getFacilityName());
        r.setFacilityCity(cmd.getFacilityCity());
        r.setFacilityState(cmd.getFacilityState());
        r.setFacilityCountry(cmd.getFacilityCountry());
        r.setFacilityRecruitmentStatus(cmd.getFacilityRecruitmentStatus());
        r.setFacilityContactName(cmd.getFacilityContactName());
        r.setFacilityContactDegree(cmd.getFacilityContactDegree());
        r.setFacilityContactPhone(cmd.getFacilityContactPhone());
        r.setFacilityContactEmail(cmd.getFacilityContactEmail());
        r.setProtocolType(cmd.getProtocolType());
        r.setProtocolDescription(cmd.getProtocolDescription());
        r.setPhase(cmd.getPhase());
        r.setExpectedTotalEnrollment(cmd.getExpectedTotalEnrollment());
        r.setSponsor(cmd.getSponsor());
        r.setCollaborators(cmd.getCollaborators());
        r.setOfficialTitle(cmd.getOfficialTitle());
        r.setConditions(cmd.getConditions());
        r.setKeywords(cmd.getKeywords());
        r.setEligibility(cmd.getEligibility());
        r.setGender(cmd.getGender());
        r.setPurpose(cmd.getPurpose());
        r.setAllocation(cmd.getAllocation());
        r.setMasking(cmd.getMasking());
        r.setControl(cmd.getControl());
        r.setAssignment(cmd.getAssignment());
        r.setEndpoint(cmd.getEndpoint());
        r.setInterventions(cmd.getInterventions());
        r.setDuration(cmd.getDuration());
        r.setSelection(cmd.getSelection());
        r.setTiming(cmd.getTiming());
        return r;
    }

    private UpdateStudyRequest toUpdateRequest(UpdateStudyCommand cmd) {
        UpdateStudyRequest r = new UpdateStudyRequest();
        r.setName(cmd.getName());
        r.setUniqueIdentifier(cmd.getUniqueIdentifier());
        r.setSecondaryIdentifier(cmd.getSecondaryIdentifier());
        r.setSummary(cmd.getSummary());
        r.setDatePlannedStart(cmd.getDatePlannedStart());
        r.setDatePlannedEnd(cmd.getDatePlannedEnd());
        r.setTypeId(cmd.getTypeId());
        r.setStatusId(cmd.getStatusId());
        r.setPrincipalInvestigator(cmd.getPrincipalInvestigator());
        r.setFacilityName(cmd.getFacilityName());
        r.setFacilityCity(cmd.getFacilityCity());
        r.setFacilityState(cmd.getFacilityState());
        r.setFacilityCountry(cmd.getFacilityCountry());
        r.setFacilityRecruitmentStatus(cmd.getFacilityRecruitmentStatus());
        r.setFacilityContactName(cmd.getFacilityContactName());
        r.setFacilityContactDegree(cmd.getFacilityContactDegree());
        r.setFacilityContactPhone(cmd.getFacilityContactPhone());
        r.setFacilityContactEmail(cmd.getFacilityContactEmail());
        r.setProtocolType(cmd.getProtocolType());
        r.setProtocolDescription(cmd.getProtocolDescription());
        r.setPhase(cmd.getPhase());
        r.setExpectedTotalEnrollment(cmd.getExpectedTotalEnrollment());
        r.setSponsor(cmd.getSponsor());
        r.setCollaborators(cmd.getCollaborators());
        r.setOfficialTitle(cmd.getOfficialTitle());
        r.setConditions(cmd.getConditions());
        r.setKeywords(cmd.getKeywords());
        r.setEligibility(cmd.getEligibility());
        r.setGender(cmd.getGender());
        r.setPurpose(cmd.getPurpose());
        r.setAllocation(cmd.getAllocation());
        r.setMasking(cmd.getMasking());
        r.setControl(cmd.getControl());
        r.setAssignment(cmd.getAssignment());
        r.setEndpoint(cmd.getEndpoint());
        r.setInterventions(cmd.getInterventions());
        r.setDuration(cmd.getDuration());
        r.setSelection(cmd.getSelection());
        r.setTiming(cmd.getTiming());
        return r;
    }
}
