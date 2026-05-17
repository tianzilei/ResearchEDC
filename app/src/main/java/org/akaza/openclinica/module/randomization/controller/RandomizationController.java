package org.akaza.openclinica.module.randomization.controller;

import java.util.List;
import org.akaza.openclinica.module.randomization.dto.*;
import org.akaza.openclinica.module.randomization.enums.UnblindingStatus;
import org.akaza.openclinica.module.randomization.service.RandomizationService;
import org.akaza.openclinica.module.randomization.service.UnblindingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/randomization")
public class RandomizationController {

    private final RandomizationService randomizationService;
    private final UnblindingService unblindingService;

    public RandomizationController(
            RandomizationService randomizationService,
            UnblindingService unblindingService) {
        this.randomizationService = randomizationService;
        this.unblindingService = unblindingService;
    }

    // === Scheme Endpoints ===

    @GetMapping("/schemes")
    public ResponseEntity<List<SchemeSummaryDTO>> listSchemes(
            @RequestParam Integer studyId) {
        return ResponseEntity.ok(randomizationService.listSchemes(studyId));
    }

    @GetMapping("/schemes/{id}")
    public ResponseEntity<SchemeDTO> getScheme(@PathVariable Long id) {
        return ResponseEntity.ok(randomizationService.getScheme(id));
    }

    @PostMapping("/schemes")
    public ResponseEntity<SchemeDTO> createScheme(
            @RequestBody SchemeDTO dto,
            @RequestParam(defaultValue = "0") Integer userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(randomizationService.createScheme(dto, userId));
    }

    @PutMapping("/schemes/{id}")
    public ResponseEntity<SchemeDTO> updateScheme(
            @PathVariable Long id,
            @RequestBody SchemeDTO dto,
            @RequestParam(defaultValue = "0") Integer userId) {
        return ResponseEntity.ok(randomizationService.updateScheme(id, dto, userId));
    }

    @PostMapping("/schemes/{id}/activate")
    public ResponseEntity<Void> activateScheme(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer userId) {
        randomizationService.activateScheme(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/schemes/{id}/close")
    public ResponseEntity<Void> closeScheme(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer userId) {
        randomizationService.closeScheme(id, userId);
        return ResponseEntity.ok().build();
    }

    // === Randomization Endpoints ===

    @PostMapping("/randomize")
    public ResponseEntity<AssignmentDTO> randomize(
            @RequestBody RandomizeRequest request,
            @RequestParam(defaultValue = "0") Integer userId) {
        return ResponseEntity.ok(randomizationService.randomize(request, userId));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentDTO>> listAssignments(
            @RequestParam Long schemeId) {
        return ResponseEntity.ok(randomizationService.listAssignments(schemeId));
    }

    @GetMapping("/assignments/{schemeId}/subject/{studySubjectId}")
    public ResponseEntity<AssignmentDTO> getAssignment(
            @PathVariable Long schemeId,
            @PathVariable Integer studySubjectId) {
        return ResponseEntity.ok(randomizationService.getAssignment(schemeId, studySubjectId));
    }

    // === Unblinding Endpoints ===

    @PostMapping("/unblinding/request")
    public ResponseEntity<UnblindingRequestDTO> requestUnblinding(
            @RequestParam Long assignmentId,
            @RequestParam Integer requestedBy,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unblindingService.requestUnblinding(assignmentId, requestedBy, reason));
    }

    @PostMapping("/unblinding/{requestId}/review")
    public ResponseEntity<UnblindingRequestDTO> reviewUnblinding(
            @PathVariable Long requestId,
            @RequestParam UnblindingStatus decision,
            @RequestParam Integer reviewedBy,
            @RequestParam(required = false) String reviewNotes) {
        return ResponseEntity.ok(
                unblindingService.reviewUnblinding(requestId, decision, reviewedBy, reviewNotes));
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
