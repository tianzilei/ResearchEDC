package org.researchedc.module.randomization.controller;

import java.util.List;
import org.researchedc.module.randomization.dto.*;
import org.researchedc.module.randomization.enums.UnblindingStatus;
import org.researchedc.module.randomization.service.RandomizationService;
import org.researchedc.module.randomization.service.UnblindingService;
import org.researchedc.config.CurrentUserUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<SchemeSummaryDTO>> listSchemes(
            @RequestParam Integer studyId) {
        return ResponseEntity.ok(randomizationService.listSchemes(studyId));
    }

    @GetMapping("/schemes/{id}")
    public ResponseEntity<SchemeDTO> getScheme(@PathVariable("id") Long id) {
        return ResponseEntity.ok(randomizationService.getScheme(id));
    }

    @PostMapping("/schemes")
    public ResponseEntity<SchemeDTO> createScheme(
            @RequestBody SchemeDTO dto,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = userId != null ? userId : currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(randomizationService.createScheme(dto, ownerId));
    }

    @PutMapping("/schemes/{id}")
    public ResponseEntity<SchemeDTO> updateScheme(
            @PathVariable("id") Long id,
            @RequestBody SchemeDTO dto,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = userId != null ? userId : currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.updateScheme(id, dto, ownerId));
    }

    @PostMapping("/schemes/{id}/activate")
    public ResponseEntity<Void> activateScheme(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = userId != null ? userId : currentUserUtils.getCurrentUserId();
        randomizationService.activateScheme(id, ownerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schemes/{id}/close")
    public ResponseEntity<Void> closeScheme(
            @PathVariable("id") Long id,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = userId != null ? userId : currentUserUtils.getCurrentUserId();
        randomizationService.closeScheme(id, ownerId);
        return ResponseEntity.ok().build();
    }

    // === Randomization Endpoints ===

    @PostMapping("/randomize")
    public ResponseEntity<AssignmentDTO> randomize(
            @RequestBody RandomizeRequest request,
            @RequestParam(required = false) Integer userId) {
        Integer ownerId = userId != null ? userId : currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(randomizationService.randomize(request, ownerId));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentDTO>> listAssignments(
            @RequestParam Long schemeId) {
        return ResponseEntity.ok(randomizationService.listAssignments(schemeId));
    }

    @GetMapping("/assignments/{schemeId}/subject/{studySubjectId}")
    public ResponseEntity<AssignmentDTO> getAssignment(
            @PathVariable("schemeId") Long schemeId,
            @PathVariable("studySubjectId") Integer studySubjectId) {
        return ResponseEntity.ok(randomizationService.getAssignment(schemeId, studySubjectId));
    }

    // === Unblinding Endpoints ===

    @PostMapping("/unblinding/request")
    public ResponseEntity<UnblindingRequestDTO> requestUnblinding(
            @RequestParam Long assignmentId,
            @RequestParam(required = false) Integer requestedBy,
            @RequestParam(required = false) String reason) {
        Integer ownerId = requestedBy != null ? requestedBy : currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unblindingService.requestUnblinding(assignmentId, ownerId, reason));
    }

    @PostMapping("/unblinding/{requestId}/review")
    public ResponseEntity<UnblindingRequestDTO> reviewUnblinding(
            @PathVariable("requestId") Long requestId,
            @RequestParam UnblindingStatus decision,
            @RequestParam(required = false) Integer reviewedBy,
            @RequestParam(required = false) String reviewNotes) {
        Integer ownerId = reviewedBy != null ? reviewedBy : currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(
                unblindingService.reviewUnblinding(requestId, decision, ownerId, reviewNotes));
    }

    @GetMapping("/unblinding/requests")
    public ResponseEntity<List<UnblindingRequestDTO>> listUnblindingRequests(
            @RequestParam Long schemeId) {
        return ResponseEntity.ok(unblindingService.listRequests(schemeId));
    }

    @GetMapping("/unblinding/pending")
    public ResponseEntity<List<UnblindingRequestDTO>> listPendingRequests() {
        return ResponseEntity.ok(unblindingService.listPendingRequests());
    }

    // === Audit Endpoints ===

    @GetMapping("/audit")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) Long schemeId,
            @RequestParam(required = false) Integer studyId) {
        if (schemeId != null) {
            return ResponseEntity.ok(randomizationService.getAuditLogs(schemeId));
        }
        if (studyId != null) {
            return ResponseEntity.ok(randomizationService.getStudyAuditLogs(studyId));
        }
        return ResponseEntity.badRequest().build();
    }
}
