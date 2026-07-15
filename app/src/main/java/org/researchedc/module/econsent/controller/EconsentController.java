package org.researchedc.module.econsent.controller;

import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
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
import org.researchedc.module.econsent.service.EconsentService;
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
@RequestMapping("/api/v1/econsent")
public class EconsentController {

    private final EconsentService econsentService;
    private final CurrentUserUtils currentUserUtils;

    public EconsentController(EconsentService econsentService, CurrentUserUtils currentUserUtils) {
        this.econsentService = econsentService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/templates")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<ConsentTemplateDTO>> listTemplates(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(econsentService.listTemplates(studyId, currentUserId).stream()
                .map(econsentService::toTemplateDto)
                .toList());
    }

    @PostMapping("/templates")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ConsentTemplateDTO> createTemplate(@RequestBody CreateConsentTemplateRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(econsentService.toTemplateDto(econsentService.createTemplate(request, currentUserId)));
    }

    @GetMapping("/templates/{templateId}/versions")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<ConsentVersionDTO>> listVersions(@PathVariable Long templateId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(econsentService.listVersions(templateId, currentUserId).stream()
                .map(econsentService::toVersionDto)
                .toList());
    }

    @PostMapping("/templates/{templateId}/versions")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ConsentVersionDTO> createVersion(
            @PathVariable Long templateId,
            @RequestBody CreateConsentVersionRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(econsentService.toVersionDto(econsentService.createVersion(templateId, request, currentUserId)));
    }

    @PostMapping("/versions/{versionId}/publish")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ConsentVersionDTO> publishVersion(@PathVariable Long versionId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(econsentService.toVersionDto(
                econsentService.publishVersion(versionId, currentUserId)));
    }

    @GetMapping("/assignments")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<ConsentAssignmentDTO>> listAssignments(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(econsentService.listAssignments(studyId, currentUserId).stream()
                .map(econsentService::toAssignmentDto)
                .toList());
    }

    @PostMapping("/assignments")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ConsentAssignmentResultDTO> assignConsent(@RequestBody AssignConsentRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(econsentService.assignConsent(request, currentUserId));
    }

    @PostMapping("/assignments/{assignmentId}/countersign")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ConsentAssignmentDTO> countersign(
            @PathVariable Long assignmentId,
            @RequestBody CountersignConsentRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(econsentService.toAssignmentDto(
                econsentService.countersign(assignmentId, request, currentUserId)));
    }

    @GetMapping("/assignments/{assignmentId}/artifact")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<ConsentArtifactDTO> artifact(@PathVariable Long assignmentId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(econsentService.artifact(assignmentId, currentUserId));
    }

    @GetMapping("/public/consents")
    public ResponseEntity<List<ParticipantConsentDTO>> listParticipantConsents(@RequestParam String token) {
        return ResponseEntity.ok(econsentService.listParticipantConsents(token));
    }

    @PostMapping("/public/assignments/{assignmentId}/sign")
    public ResponseEntity<ConsentAssignmentDTO> signParticipantConsent(
            @PathVariable Long assignmentId,
            @RequestParam String token,
            @RequestBody SignConsentRequest request) {
        return ResponseEntity.ok(econsentService.toAssignmentDto(
                econsentService.signParticipantConsent(assignmentId, token, request)));
    }
}
