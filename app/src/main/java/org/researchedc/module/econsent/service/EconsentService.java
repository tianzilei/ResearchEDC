package org.researchedc.module.econsent.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.econsent.dto.AssignConsentRequest;
import org.researchedc.module.econsent.dto.ConsentArtifactDTO;
import org.researchedc.module.econsent.dto.ConsentAssignmentDTO;
import org.researchedc.module.econsent.dto.ConsentAssignmentResultDTO;
import org.researchedc.module.econsent.dto.ConsentTemplateDTO;
import org.researchedc.module.econsent.dto.ConsentVersionDTO;
import org.researchedc.module.econsent.dto.CountersignConsentRequest;
import org.researchedc.module.econsent.dto.CreateConsentTemplateRequest;
import org.researchedc.module.econsent.dto.CreateConsentVersionRequest;
import org.researchedc.module.econsent.dto.ParticipantConsentDTO;
import org.researchedc.module.econsent.dto.SignConsentRequest;
import org.researchedc.module.econsent.entity.ConsentAssignment;
import org.researchedc.module.econsent.entity.ConsentTemplate;
import org.researchedc.module.econsent.entity.ConsentVersion;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;
import org.researchedc.module.econsent.enums.ConsentVersionStatus;
import org.researchedc.module.econsent.repository.ConsentAssignmentRepository;
import org.researchedc.module.econsent.repository.ConsentTemplateRepository;
import org.researchedc.module.econsent.repository.ConsentVersionRepository;
import org.researchedc.module.participantaccess.dto.IssueParticipantTokenRequest;
import org.researchedc.module.participantaccess.dto.IssuedParticipantTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccountDTO;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.service.ParticipantAccessService;
import org.researchedc.module.task.dto.CreateTaskRequest;
import org.researchedc.module.task.enums.TaskTargetType;
import org.researchedc.module.task.service.TaskService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class EconsentService {

    private static final Set<ConsentAssignmentStatus> ACTIVE_ASSIGNMENTS =
            Set.of(ConsentAssignmentStatus.ASSIGNED, ConsentAssignmentStatus.PARTICIPANT_SIGNED);

    private final ConsentTemplateRepository templateRepository;
    private final ConsentVersionRepository versionRepository;
    private final ConsentAssignmentRepository assignmentRepository;
    private final ParticipantAccessService participantAccessService;
    private final TaskService taskService;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;

    public EconsentService(ConsentTemplateRepository templateRepository,
                           ConsentVersionRepository versionRepository,
                           ConsentAssignmentRepository assignmentRepository,
                           ParticipantAccessService participantAccessService,
                           TaskService taskService,
                           CurrentStudyAccessService currentStudyAccessService,
                           AuditService auditService) {
        this.templateRepository = templateRepository;
        this.versionRepository = versionRepository;
        this.assignmentRepository = assignmentRepository;
        this.participantAccessService = participantAccessService;
        this.taskService = taskService;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
    }

    public List<ConsentTemplate> listTemplates(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return templateRepository.findByStudyIdOrderByNameAsc(studyId);
    }

    public List<ConsentVersion> listVersions(Long templateId, Integer currentUserId) {
        ConsentTemplate template = findTemplate(templateId);
        requireReadAccess(currentUserId, template.getStudyId());
        return versionRepository.findByTemplateIdOrderByCreatedDateDesc(templateId);
    }

    public List<ConsentAssignment> listAssignments(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return assignmentRepository.findByStudyIdOrderByCreatedDateDesc(studyId);
    }

    @Transactional
    public ConsentTemplate createTemplate(CreateConsentTemplateRequest request, Integer currentUserId) {
        if (request.getStudyId() == null) {
            throw new IllegalArgumentException("studyId is required");
        }
        requireWriteAccess(currentUserId, request.getStudyId());
        requireText(request.getCode(), "code is required");
        requireText(request.getName(), "name is required");

        ConsentTemplate template = new ConsentTemplate();
        template.setStudyId(request.getStudyId());
        template.setCode(request.getCode().trim());
        template.setName(request.getName().trim());
        template.setDescription(defaultText(request.getDescription()));
        template.setActive(true);
        template.setCreatedBy(currentUserId);
        ConsentTemplate saved = templateRepository.save(template);
        record(saved.getStudyId(), AuditEventType.CREATE, "consent_template", saved.getId(),
                saved.getName(), currentUserId, "Consent template created");
        return saved;
    }

    @Transactional
    public ConsentVersion createVersion(Long templateId, CreateConsentVersionRequest request,
                                        Integer currentUserId) {
        ConsentTemplate template = findTemplate(templateId);
        requireWriteAccess(currentUserId, template.getStudyId());
        requireText(request.getVersionLabel(), "versionLabel is required");
        requireText(request.getBodyText(), "bodyText is required");

        ConsentVersion version = new ConsentVersion();
        version.setTemplateId(template.getId());
        version.setStudyId(template.getStudyId());
        version.setVersionLabel(request.getVersionLabel().trim());
        version.setBodyText(request.getBodyText().trim());
        version.setStatus(ConsentVersionStatus.DRAFT);
        version.setCreatedBy(currentUserId);
        ConsentVersion saved = versionRepository.save(version);
        record(saved.getStudyId(), AuditEventType.CREATE, "consent_version", saved.getId(),
                saved.getVersionLabel(), currentUserId, "Consent version created");
        return saved;
    }

    @Transactional
    public ConsentVersion publishVersion(Long versionId, Integer currentUserId) {
        ConsentVersion version = findVersion(versionId);
        requireWriteAccess(currentUserId, version.getStudyId());
        if (version.getStatus() == ConsentVersionStatus.RETIRED) {
            throw new IllegalStateException("Retired consent versions cannot be published");
        }
        version.setStatus(ConsentVersionStatus.PUBLISHED);
        version.setPublishedDate(LocalDateTime.now());
        ConsentVersion saved = versionRepository.save(version);
        record(saved.getStudyId(), AuditEventType.UPDATE, "consent_version", saved.getId(),
                saved.getVersionLabel(), currentUserId, "Consent version published");
        return saved;
    }

    @Transactional
    public ConsentAssignmentResultDTO assignConsent(AssignConsentRequest request, Integer currentUserId) {
        if (request.getStudySubjectId() == null) {
            throw new IllegalArgumentException("studySubjectId is required");
        }
        if (request.getConsentVersionId() == null) {
            throw new IllegalArgumentException("consentVersionId is required");
        }
        ConsentVersion version = findVersion(request.getConsentVersionId());
        requireWriteAccess(currentUserId, version.getStudyId());
        if (version.getStatus() != ConsentVersionStatus.PUBLISHED) {
            throw new IllegalStateException("Consent version must be published before assignment");
        }
        List<ConsentAssignment> existing = assignmentRepository.findByStudySubjectIdAndConsentVersionIdAndStatusIn(
                request.getStudySubjectId(), version.getId(), ACTIVE_ASSIGNMENTS);
        if (!existing.isEmpty()) {
            throw new IllegalStateException("Active consent assignment already exists for this subject and version");
        }

        ParticipantAccountDTO account =
                participantAccessService.findOrCreateAccountForStudySubject(request.getStudySubjectId(), currentUserId);
        IssuedParticipantTokenDTO issuedToken =
                participantAccessService.issueToken(tokenRequest(account.getId(), request.getDueAt()), currentUserId);

        ConsentAssignment assignment = new ConsentAssignment();
        assignment.setStudyId(version.getStudyId());
        assignment.setStudySubjectId(request.getStudySubjectId());
        assignment.setConsentVersionId(version.getId());
        assignment.setParticipantAccountId(account.getId());
        assignment.setParticipantTokenId(issuedToken.getToken().getId());
        assignment.setStatus(ConsentAssignmentStatus.ASSIGNED);
        assignment.setDueAt(request.getDueAt());
        assignment.setEntryUrl(issuedToken.getEntryUrl());
        assignment.setCreatedBy(currentUserId);
        ConsentAssignment saved = assignmentRepository.save(assignment);

        Long taskId = taskService.createTaskId(taskRequest(version, saved), currentUserId);
        saved.setTaskInstanceId(taskId);
        saved.setUpdatedDate(LocalDateTime.now());
        saved = assignmentRepository.save(saved);

        record(saved.getStudyId(), AuditEventType.ASSIGN, "consent_assignment", saved.getId(),
                "consent:" + saved.getId(), currentUserId, "Consent assigned");

        ConsentAssignmentResultDTO result = new ConsentAssignmentResultDTO();
        result.setAssignment(toAssignmentDto(saved));
        result.setParticipantEntryUrl(issuedToken.getEntryUrl());
        return result;
    }

    public List<ParticipantConsentDTO> listParticipantConsents(String rawToken) {
        ParticipantBootstrapDTO bootstrap = participantAccessService.verifyToken(rawToken);
        return listParticipantConsentsForAccount(bootstrap.getParticipantAccountId());
    }

    public List<ParticipantConsentDTO> listParticipantConsentsForAccount(Long participantAccountId) {
        return assignmentRepository.findByParticipantAccountIdAndStatusInOrderByCreatedDateDesc(
                participantAccountId, ACTIVE_ASSIGNMENTS).stream()
                .map(this::toParticipantConsentDto)
                .toList();
    }

    @Transactional
    public ConsentAssignment signParticipantConsent(Long assignmentId, String rawToken, SignConsentRequest request) {
        ParticipantBootstrapDTO bootstrap = participantAccessService.verifyToken(rawToken);
        ConsentAssignment assignment = findAssignment(assignmentId);
        if (!assignment.getParticipantAccountId().equals(bootstrap.getParticipantAccountId())) {
            throw new AccessDeniedException("Consent assignment does not belong to this participant");
        }
        if (assignment.getStatus() != ConsentAssignmentStatus.ASSIGNED) {
            throw new IllegalStateException("Consent assignment is not waiting for participant signature");
        }
        requireText(request.getParticipantName(), "participantName is required");
        requireText(request.getSignature(), "signature is required");

        assignment.setParticipantName(request.getParticipantName().trim());
        assignment.setParticipantSignature(request.getSignature().trim());
        assignment.setParticipantEvidence(defaultText(request.getEvidence()));
        assignment.setParticipantSignedAt(LocalDateTime.now());
        assignment.setStatus(ConsentAssignmentStatus.PARTICIPANT_SIGNED);
        assignment.setUpdatedDate(LocalDateTime.now());
        ConsentAssignment saved = assignmentRepository.save(assignment);
        record(saved.getStudyId(), AuditEventType.UPDATE, "consent_assignment", saved.getId(),
                "consent:" + saved.getId(), null, "Consent signed by participant");
        return saved;
    }

    @Transactional
    public ConsentAssignment countersign(Long assignmentId, CountersignConsentRequest request,
                                         Integer currentUserId) {
        ConsentAssignment assignment = findAssignment(assignmentId);
        requireWriteAccess(currentUserId, assignment.getStudyId());
        if (assignment.getStatus() != ConsentAssignmentStatus.PARTICIPANT_SIGNED) {
            throw new IllegalStateException("Consent must be participant-signed before countersignature");
        }
        requireText(request.getCountersignature(), "countersignature is required");

        assignment.setCountersignedBy(currentUserId);
        assignment.setCountersignature(request.getCountersignature().trim());
        assignment.setCountersignedAt(LocalDateTime.now());
        assignment.setStatus(ConsentAssignmentStatus.COUNTERSIGNED);
        assignment.setArtifactName("consent-" + assignment.getId() + ".txt");
        assignment.setArtifactText(generateArtifact(assignment));
        assignment.setUpdatedDate(LocalDateTime.now());
        ConsentAssignment saved = assignmentRepository.save(assignment);
        if (saved.getTaskInstanceId() != null) {
            taskService.completeTask(saved.getTaskInstanceId(), currentUserId);
        }
        record(saved.getStudyId(), AuditEventType.UPDATE, "consent_assignment", saved.getId(),
                saved.getArtifactName(), currentUserId, "Consent countersigned and artifact generated");
        return saved;
    }

    public ConsentArtifactDTO artifact(Long assignmentId, Integer currentUserId) {
        ConsentAssignment assignment = findAssignment(assignmentId);
        requireReadAccess(currentUserId, assignment.getStudyId());
        if (!StringUtils.hasText(assignment.getArtifactText())) {
            throw new NoSuchElementException("Signed consent artifact not found: " + assignmentId);
        }
        ConsentArtifactDTO dto = new ConsentArtifactDTO();
        dto.setArtifactName(assignment.getArtifactName());
        dto.setContentType("text/plain;charset=UTF-8");
        dto.setContent(assignment.getArtifactText());
        return dto;
    }

    public ConsentTemplateDTO toTemplateDto(ConsentTemplate template) {
        ConsentTemplateDTO dto = new ConsentTemplateDTO();
        dto.setId(template.getId());
        dto.setStudyId(template.getStudyId());
        dto.setCode(template.getCode());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setActive(template.getActive());
        dto.setCreatedBy(template.getCreatedBy());
        dto.setCreatedDate(template.getCreatedDate());
        return dto;
    }

    public ConsentVersionDTO toVersionDto(ConsentVersion version) {
        ConsentVersionDTO dto = new ConsentVersionDTO();
        dto.setId(version.getId());
        dto.setTemplateId(version.getTemplateId());
        dto.setStudyId(version.getStudyId());
        dto.setVersionLabel(version.getVersionLabel());
        dto.setBodyText(version.getBodyText());
        dto.setStatus(version.getStatus());
        dto.setPublishedDate(version.getPublishedDate());
        dto.setCreatedBy(version.getCreatedBy());
        dto.setCreatedDate(version.getCreatedDate());
        return dto;
    }

    public ConsentAssignmentDTO toAssignmentDto(ConsentAssignment assignment) {
        ConsentAssignmentDTO dto = new ConsentAssignmentDTO();
        dto.setId(assignment.getId());
        dto.setStudyId(assignment.getStudyId());
        dto.setStudySubjectId(assignment.getStudySubjectId());
        dto.setConsentVersionId(assignment.getConsentVersionId());
        dto.setParticipantAccountId(assignment.getParticipantAccountId());
        dto.setParticipantTokenId(assignment.getParticipantTokenId());
        dto.setTaskInstanceId(assignment.getTaskInstanceId());
        dto.setStatus(assignment.getStatus());
        dto.setDueAt(assignment.getDueAt());
        dto.setEntryUrl(assignment.getEntryUrl());
        dto.setParticipantName(assignment.getParticipantName());
        dto.setParticipantSignedAt(assignment.getParticipantSignedAt());
        dto.setCountersignedBy(assignment.getCountersignedBy());
        dto.setCountersignedAt(assignment.getCountersignedAt());
        dto.setArtifactName(assignment.getArtifactName());
        dto.setCreatedBy(assignment.getCreatedBy());
        dto.setCreatedDate(assignment.getCreatedDate());
        dto.setUpdatedDate(assignment.getUpdatedDate());
        return dto;
    }

    private ParticipantConsentDTO toParticipantConsentDto(ConsentAssignment assignment) {
        ConsentVersion version = findVersion(assignment.getConsentVersionId());
        ConsentTemplate template = findTemplate(version.getTemplateId());
        ParticipantConsentDTO dto = new ParticipantConsentDTO();
        dto.setAssignment(toAssignmentDto(assignment));
        dto.setVersion(toVersionDto(version));
        dto.setTemplate(toTemplateDto(template));
        return dto;
    }

    private ConsentTemplate findTemplate(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consent template not found: " + id));
    }

    private ConsentVersion findVersion(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consent version not found: " + id));
    }

    private ConsentAssignment findAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Consent assignment not found: " + id));
    }

    private IssueParticipantTokenRequest tokenRequest(Long participantAccountId, LocalDateTime dueAt) {
        IssueParticipantTokenRequest request = new IssueParticipantTokenRequest();
        request.setParticipantAccountId(participantAccountId);
        request.setScope("econsent");
        request.setExpiresInHours(resolveTokenExpiryHours(dueAt));
        return request;
    }

    private Integer resolveTokenExpiryHours(LocalDateTime dueAt) {
        if (dueAt == null) {
            return 24 * 14;
        }
        long hours = Duration.between(LocalDateTime.now(), dueAt.plusDays(1)).toHours();
        if (hours < 1) {
            return 1;
        }
        return (int) Math.min(hours, 24L * 90L);
    }

    private CreateTaskRequest taskRequest(ConsentVersion version, ConsentAssignment assignment) {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setStudyId(version.getStudyId());
        request.setTitle("Consent " + version.getVersionLabel());
        request.setDescription("Review and sign consent version " + version.getVersionLabel());
        request.setTargetType(TaskTargetType.CONSENT_ASSIGNMENT);
        request.setTargetId(assignment.getId());
        request.setDueDate(assignment.getDueAt());
        return request;
    }

    private String generateArtifact(ConsentAssignment assignment) {
        ConsentVersion version = findVersion(assignment.getConsentVersionId());
        ConsentTemplate template = findTemplate(version.getTemplateId());
        return "ResearchEDC Signed Consent\n"
                + "Template: " + template.getName() + " (" + template.getCode() + ")\n"
                + "Version: " + version.getVersionLabel() + "\n"
                + "Study ID: " + assignment.getStudyId() + "\n"
                + "Study Subject ID: " + assignment.getStudySubjectId() + "\n"
                + "Participant: " + assignment.getParticipantName() + "\n"
                + "Participant signed at: " + assignment.getParticipantSignedAt() + "\n"
                + "Participant signature: " + assignment.getParticipantSignature() + "\n"
                + "Participant evidence: " + defaultText(assignment.getParticipantEvidence()) + "\n"
                + "Countersigned by user: " + assignment.getCountersignedBy() + "\n"
                + "Countersigned at: " + assignment.getCountersignedAt() + "\n"
                + "Countersignature: " + assignment.getCountersignature() + "\n\n"
                + "Consent Body\n"
                + version.getBodyText() + "\n";
    }

    private void requireReadAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canReadStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have read access to this study");
        }
    }

    private void requireWriteAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canWriteStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have write access to this study");
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "econsent");
    }
}
