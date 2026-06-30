package org.researchedc.module.randomization.controller;

import java.util.List;
import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.randomization.dto.*;
import org.researchedc.module.randomization.enums.UnblindingStatus;
import org.researchedc.module.randomization.service.RandomizationService;
import org.researchedc.module.randomization.service.UnblindingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/randomization")
public class RandomizationController {

    private final RandomizationService randomizationService;
    private final UnblindingService unblindingService;
    private final CurrentUserUtils currentUserUtils;

    public RandomizationController(
            RandomizationService randomizationService,
            UnblindingService unblindingService,
            CurrentUserUtils currentUserUtils) {
        this.randomizationService = randomizationService;
        this.unblindingService = unblindingService;
        this.currentUserUtils = currentUserUtils;
    }

    // === Scheme Endpoints ===

    @GetMapping("/schemes")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<SchemeSummaryDTO>> listSchemes(
            @RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.listSchemes(studyId, currentUserId));
    }

    @GetMapping("/schemes/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<SchemeDTO> getScheme(@PathVariable("id") Long id) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.getScheme(id, currentUserId));
    }

    @PostMapping("/schemes")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SchemeDTO> createScheme(
            @RequestBody SchemeDTO dto,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(randomizationService.createScheme(dto, ownerId));
    }

    @PutMapping("/schemes/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<SchemeDTO> updateScheme(
            @PathVariable("id") Long id,
            @RequestBody SchemeDTO dto,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.updateScheme(id, dto, ownerId));
    }

    @PostMapping("/schemes/{id}/activate")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> activateScheme(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        randomizationService.activateScheme(id, ownerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schemes/{id}/close")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> closeScheme(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        randomizationService.closeScheme(id, ownerId);
        return ResponseEntity.ok().build();
    }

    // === Randomization Endpoints ===

    @PostMapping("/randomize")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<AssignmentDTO> randomize(
            @RequestBody RandomizeRequest request,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.randomize(request, ownerId));
    }

    @GetMapping("/assignments")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<AssignmentDTO>> listAssignments(
            @RequestParam Long schemeId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.listAssignments(schemeId, currentUserId));
    }

    @GetMapping("/assignments/{schemeId}/subject/{studySubjectId}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<AssignmentDTO> getAssignment(
            @PathVariable("schemeId") Long schemeId,
            @PathVariable("studySubjectId") Integer studySubjectId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.getAssignment(schemeId, studySubjectId, currentUserId));
    }

    // === Unblinding Endpoints ===

    @PostMapping("/unblinding/request")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<UnblindingRequestDTO> requestUnblinding(
            @RequestParam Long assignmentId,
            @RequestParam(required = false) Integer requestedBy,
            @RequestParam(required = false) String reason) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unblindingService.requestUnblinding(assignmentId, ownerId, reason));
    }

    @PostMapping("/unblinding/{requestId}/review")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<UnblindingRequestDTO> reviewUnblinding(
            @PathVariable("requestId") Long requestId,
            @RequestParam UnblindingStatus decision,
            @RequestParam(required = false) Integer reviewedBy,
            @RequestParam(required = false) String reviewNotes) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(
                unblindingService.reviewUnblinding(requestId, decision, ownerId, reviewNotes));
    }

    @GetMapping("/unblinding/requests")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<UnblindingRequestDTO>> listUnblindingRequests(
            @RequestParam Long schemeId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(unblindingService.listRequests(schemeId, currentUserId));
    }

    @GetMapping("/unblinding/pending")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<UnblindingRequestDTO>> listPendingRequests() {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(unblindingService.listPendingRequests(currentUserId));
    }

    // === Audit Endpoints ===

    @GetMapping("/audit")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) Long schemeId,
            @RequestParam(required = false) Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        if (schemeId != null) {
            return ResponseEntity.ok(randomizationService.getAuditLogs(schemeId, currentUserId));
        }
        if (studyId != null) {
            return ResponseEntity.ok(randomizationService.getStudyAuditLogs(studyId, currentUserId));
        }
        return ResponseEntity.badRequest().build();
    }
}
