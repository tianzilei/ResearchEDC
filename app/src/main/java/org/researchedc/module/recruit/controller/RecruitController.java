package org.researchedc.module.recruit.controller;

import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.recruit.dto.CandidateDTO;
import org.researchedc.module.recruit.dto.ConvertCandidateRequest;
import org.researchedc.module.recruit.dto.ConvertCandidateResultDTO;
import org.researchedc.module.recruit.dto.CreateCandidateRequest;
import org.researchedc.module.recruit.dto.PrescreenResultDTO;
import org.researchedc.module.recruit.dto.RecordPrescreenRequest;
import org.researchedc.module.recruit.dto.RejectCandidateRequest;
import org.researchedc.module.recruit.enums.CandidateStatus;
import org.researchedc.module.recruit.service.RecruitService;
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
@RequestMapping("/api/v1/recruit")
public class RecruitController {

    private final RecruitService recruitService;
    private final CurrentUserUtils currentUserUtils;

    public RecruitController(RecruitService recruitService, CurrentUserUtils currentUserUtils) {
        this.recruitService = recruitService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/candidates")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<CandidateDTO>> listCandidates(
            @RequestParam Integer studyId,
            @RequestParam(required = false) CandidateStatus status) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(recruitService.listCandidates(studyId, status, currentUserId));
    }

    @PostMapping("/candidates")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<CandidateDTO> createCandidate(@RequestBody CreateCandidateRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(recruitService.createCandidate(request, currentUserId));
    }

    @GetMapping("/candidates/{candidateId}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<CandidateDTO> getCandidate(@PathVariable Long candidateId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(recruitService.getCandidate(candidateId, currentUserId));
    }

    @GetMapping("/candidates/{candidateId}/prescreens")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<PrescreenResultDTO>> listPrescreens(@PathVariable Long candidateId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(recruitService.listPrescreenResults(candidateId, currentUserId));
    }

    @PostMapping("/candidates/{candidateId}/prescreens")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<PrescreenResultDTO> recordPrescreen(
            @PathVariable Long candidateId,
            @RequestBody RecordPrescreenRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recruitService.recordPrescreen(candidateId, request, currentUserId));
    }

    @PostMapping("/candidates/{candidateId}/reject")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<CandidateDTO> rejectCandidate(
            @PathVariable Long candidateId,
            @RequestBody(required = false) RejectCandidateRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(recruitService.rejectCandidate(candidateId, reason, currentUserId));
    }

    @PostMapping("/candidates/{candidateId}/convert")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ConvertCandidateResultDTO> convertCandidate(
            @PathVariable Long candidateId,
            @RequestBody ConvertCandidateRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recruitService.convertCandidate(candidateId, request, currentUserId));
    }
}
