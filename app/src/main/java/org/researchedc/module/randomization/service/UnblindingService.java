package org.researchedc.module.randomization.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.researchedc.module.randomization.dto.UnblindingRequestDTO;
import org.researchedc.module.randomization.entity.RandomizationAssignment;
import org.researchedc.module.randomization.entity.RandomizationAuditLog;
import org.researchedc.module.randomization.entity.UnblindingRequest;
import org.researchedc.module.randomization.enums.AuditAction;
import org.researchedc.module.randomization.enums.UnblindingStatus;
import org.researchedc.module.randomization.repository.RandomizationAssignmentRepository;
import org.researchedc.module.randomization.repository.RandomizationAuditLogRepository;
import org.researchedc.module.randomization.repository.UnblindingRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnblindingService {

    private static final Logger log = LoggerFactory.getLogger(UnblindingService.class);

    private final UnblindingRequestRepository requestRepository;
    private final RandomizationAssignmentRepository assignmentRepository;
    private final RandomizationAuditLogRepository auditLogRepository;

    public UnblindingService(
            UnblindingRequestRepository requestRepository,
            RandomizationAssignmentRepository assignmentRepository,
            RandomizationAuditLogRepository auditLogRepository) {
        this.requestRepository = requestRepository;
        this.assignmentRepository = assignmentRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public UnblindingRequestDTO requestUnblinding(Long assignmentId, Integer requestedBy, String reason) {
        RandomizationAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found: " + assignmentId));

        UnblindingRequest request = new UnblindingRequest();
        request.setAssignment(assignment);
        request.setRequestedBy(requestedBy);
        request.setReason(reason);
        request.setStatus(UnblindingStatus.PENDING);
        request = requestRepository.save(request);

        // Audit
        RandomizationAuditLog audit = new RandomizationAuditLog();
        audit.setSchemeId(assignment.getScheme().getId());
        audit.setAction(AuditAction.UNBLINDING_REQUESTED);
        audit.setEntityType("UnblindingRequest");
        audit.setEntityId(request.getId());
        audit.setPerformedBy(requestedBy);
        audit.setDetails("Unblinding requested for assignment " + assignmentId);
        auditLogRepository.save(audit);

        log.info("Unblinding requested for assignment {} by user {}", assignmentId, requestedBy);

        return toDTO(request, assignment);
    }

    public UnblindingRequestDTO reviewUnblinding(Long requestId, UnblindingStatus decision,
                                                  Integer reviewedBy, String reviewNotes) {
        UnblindingRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException("Request not found: " + requestId));

        if (request.getStatus() != UnblindingStatus.PENDING) {
            throw new IllegalStateException("Request already " + request.getStatus());
        }

        request.setStatus(decision);
        request.setReviewedBy(reviewedBy);
        request.setReviewedDate(LocalDateTime.now());
        request.setReviewNotes(reviewNotes);
        request = requestRepository.save(request);

        if (decision == UnblindingStatus.APPROVED) {
            RandomizationAssignment assignment = request.getAssignment();
            assignment.setStatus(
                    org.researchedc.module.randomization.enums.AssignmentStatus.UNBLINDED);
            assignmentRepository.save(assignment);
        }

        // Audit
        RandomizationAuditLog audit = new RandomizationAuditLog();
        audit.setSchemeId(request.getAssignment().getScheme().getId());
        audit.setAction(decision == UnblindingStatus.APPROVED
                ? AuditAction.UNBLINDING_APPROVED : AuditAction.UNBLINDING_REJECTED);
        audit.setEntityType("UnblindingRequest");
        audit.setEntityId(request.getId());
        audit.setPerformedBy(reviewedBy);
        audit.setDetails("Unblinding " + decision + " for request " + requestId);
        auditLogRepository.save(audit);

        log.info("Unblinding request {} {} by user {}", requestId, decision, reviewedBy);

        return toDTO(request, request.getAssignment());
    }

    public List<UnblindingRequestDTO> listRequests(Long schemeId) {
        return requestRepository.findByAssignmentSchemeIdOrderByRequestedDateDesc(schemeId)
                .stream().map(r -> toDTO(r, r.getAssignment())).toList();
    }

    public List<UnblindingRequestDTO> listPendingRequests() {
        return requestRepository.findByStatusOrderByRequestedDateAsc(UnblindingStatus.PENDING)
                .stream().map(r -> toDTO(r, r.getAssignment())).toList();
    }

    private UnblindingRequestDTO toDTO(UnblindingRequest r, RandomizationAssignment a) {
        UnblindingRequestDTO dto = new UnblindingRequestDTO();
        dto.setId(r.getId());
        dto.setAssignmentId(a.getId());
        dto.setArmName(a.getArm().getName());
        dto.setRequestedBy(r.getRequestedBy());
        dto.setRequestedDate(r.getRequestedDate());
        dto.setReason(r.getReason());
        dto.setStatus(r.getStatus());
        dto.setReviewedBy(r.getReviewedBy());
        dto.setReviewedDate(r.getReviewedDate());
        dto.setReviewNotes(r.getReviewNotes());
        return dto;
    }
}
