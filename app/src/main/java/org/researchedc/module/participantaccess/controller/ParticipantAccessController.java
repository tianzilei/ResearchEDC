package org.researchedc.module.participantaccess.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.participantaccess.dto.CreateParticipantAccountRequest;
import org.researchedc.module.participantaccess.dto.IssueParticipantTokenRequest;
import org.researchedc.module.participantaccess.dto.IssuedParticipantTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccessTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccountDTO;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.dto.RevokeParticipantTokenRequest;
import org.researchedc.module.participantaccess.service.ParticipantAccessService;
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
@RequestMapping("/api/v1/participant-access")
public class ParticipantAccessController {

    private final ParticipantAccessService participantAccessService;
    private final CurrentUserUtils currentUserUtils;

    public ParticipantAccessController(ParticipantAccessService participantAccessService,
                                       CurrentUserUtils currentUserUtils) {
        this.participantAccessService = participantAccessService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/accounts")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<ParticipantAccountDTO>> listAccounts(@RequestParam Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(participantAccessService.listAccounts(studyId, currentUserId));
    }

    @PostMapping("/accounts")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ParticipantAccountDTO> createAccount(
            @RequestBody CreateParticipantAccountRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participantAccessService.createAccount(request, currentUserId));
    }

    @GetMapping("/accounts/{accountId}/tokens")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<ParticipantAccessTokenDTO>> listTokens(@PathVariable Long accountId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.ok(participantAccessService.listTokens(accountId, currentUserId));
    }

    @PostMapping("/tokens")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<IssuedParticipantTokenDTO> issueToken(
            @RequestBody IssueParticipantTokenRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(participantAccessService.issueToken(request, currentUserId));
    }

    @PostMapping("/tokens/{tokenId}/revoke")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<ParticipantAccessTokenDTO> revokeToken(
            @PathVariable Long tokenId,
            @RequestBody(required = false) RevokeParticipantTokenRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        String reason = request == null ? null : request.getReason();
        return ResponseEntity.ok(participantAccessService.revokeToken(tokenId, reason, currentUserId));
    }

    @PostMapping("/tokens/expire")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Integer> expireTokens() {
        return ResponseEntity.ok(participantAccessService.expireTokens(LocalDateTime.now()));
    }

    @GetMapping("/public/bootstrap")
    public ResponseEntity<ParticipantBootstrapDTO> bootstrap(@RequestParam String token) {
        return ResponseEntity.ok(participantAccessService.verifyToken(token));
    }
}
