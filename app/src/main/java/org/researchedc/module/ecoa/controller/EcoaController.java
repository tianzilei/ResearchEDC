package org.researchedc.module.ecoa.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.ecoa.dto.CreateEcoaScheduleRequest;
import org.researchedc.module.ecoa.dto.EcoaAdherenceSummaryDTO;
import org.researchedc.module.ecoa.dto.EcoaAssignmentDTO;
import org.researchedc.module.ecoa.dto.EcoaScheduleDTO;
import org.researchedc.module.ecoa.dto.EcoaScheduleResultDTO;
import org.researchedc.module.ecoa.dto.RecordEcoaCompletionRequest;
import org.researchedc.module.ecoa.service.EcoaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ecoa")
public class EcoaController {

    private final EcoaService ecoaService;
    private final CurrentUserUtils currentUserUtils;

    public EcoaController(EcoaService ecoaService, CurrentUserUtils currentUserUtils) {
        this.ecoaService = ecoaService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/schedules")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<EcoaScheduleDTO>> listSchedules(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(ecoaService.listSchedules(studyId, currentUserId).stream()
                .map(ecoaService::toScheduleDto)
                .toList());
    }

    @PostMapping("/schedules")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<EcoaScheduleResultDTO> createSchedule(
            @RequestBody CreateEcoaScheduleRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ecoaService.createSchedule(request, currentUserId));
    }

    @GetMapping("/assignments")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<EcoaAssignmentDTO>> listAssignments(
            @RequestParam(required = false) Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(ecoaService.listAssignments(studyId, currentUserId).stream()
                .map(ecoaService::toAssignmentDto)
                .toList());
    }

    @GetMapping("/adherence")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<EcoaAdherenceSummaryDTO> adherence(@RequestParam(required = false) Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(ecoaService.summarizeAdherence(studyId, currentUserId));
    }

    @PostMapping("/assignments/{assignmentId}/complete")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<EcoaAssignmentDTO> recordCompletion(
            @PathVariable Long assignmentId,
            @RequestBody(required = false) RecordEcoaCompletionRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        RecordEcoaCompletionRequest body = request == null ? new RecordEcoaCompletionRequest() : request;
        return ResponseEntity.ok(ecoaService.toAssignmentDto(
                ecoaService.recordCompletion(assignmentId, body, currentUserId)));
    }

    @PostMapping("/assignments/{assignmentId}/cancel")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<EcoaAssignmentDTO> cancelAssignment(@PathVariable Long assignmentId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(ecoaService.toAssignmentDto(
                ecoaService.cancelAssignment(assignmentId, currentUserId)));
    }

    @PostMapping("/assignments/expire-overdue")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Integer> expireOverdueAssignments() {
        return ResponseEntity.ok(ecoaService.expireOverdueAssignments(LocalDateTime.now()));
    }

    @GetMapping("/public/assignments")
    public ResponseEntity<List<EcoaAssignmentDTO>> listParticipantAssignments(@RequestParam String token) {
        return ResponseEntity.ok(ecoaService.listParticipantAssignments(token).stream()
                .map(ecoaService::toAssignmentDto)
                .toList());
    }

    @PostMapping("/public/assignments/{assignmentId}/complete")
    public ResponseEntity<EcoaAssignmentDTO> completeParticipantAssignment(
            @PathVariable Long assignmentId,
            @RequestParam String token,
            @RequestBody(required = false) RecordEcoaCompletionRequest request) {
        RecordEcoaCompletionRequest body = request == null ? new RecordEcoaCompletionRequest() : request;
        return ResponseEntity.ok(ecoaService.toAssignmentDto(
                ecoaService.completeParticipantAssignment(assignmentId, token, body)));
    }
}
