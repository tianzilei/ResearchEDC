package org.researchedc.module.participantaccess.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.participantaccess.dto.CreateParticipantAccountRequest;
import org.researchedc.module.participantaccess.dto.IssueParticipantTokenRequest;
import org.researchedc.module.participantaccess.dto.IssuedParticipantTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccessTokenDTO;
import org.researchedc.module.participantaccess.dto.ParticipantAccountDTO;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.entity.ParticipantAccessToken;
import org.researchedc.module.participantaccess.entity.ParticipantAccount;
import org.researchedc.module.participantaccess.enums.ParticipantAccessTokenStatus;
import org.researchedc.module.participantaccess.enums.ParticipantAccountStatus;
import org.researchedc.module.participantaccess.repository.ParticipantAccessTokenRepository;
import org.researchedc.module.participantaccess.repository.ParticipantAccountRepository;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ParticipantAccessService {

    private static final int DEFAULT_EXPIRY_HOURS = 168;
    private static final Set<ParticipantAccessTokenStatus> EXPIRABLE_STATUSES =
            Set.of(ParticipantAccessTokenStatus.ACTIVE, ParticipantAccessTokenStatus.USED);

    private final ParticipantAccountRepository accountRepository;
    private final ParticipantAccessTokenRepository tokenRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;
    private final ParticipantTokenGenerator tokenGenerator;

    public ParticipantAccessService(ParticipantAccountRepository accountRepository,
                                    ParticipantAccessTokenRepository tokenRepository,
                                    StudySubjectRepository studySubjectRepository,
                                    CurrentStudyAccessService currentStudyAccessService,
                                    AuditService auditService,
                                    ParticipantTokenGenerator tokenGenerator) {
        this.accountRepository = accountRepository;
        this.tokenRepository = tokenRepository;
        this.studySubjectRepository = studySubjectRepository;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
        this.tokenGenerator = tokenGenerator;
    }

    public List<ParticipantAccountDTO> listAccounts(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return accountRepository.findByStudyIdOrderByDisplayLabelAsc(studyId).stream()
                .map(this::toAccountDto)
                .toList();
    }

    @Transactional
    public ParticipantAccountDTO findOrCreateAccountForStudySubject(Integer studySubjectId, Integer currentUserId) {
        StudySubjectEntity studySubject = studySubjectRepository.findById(studySubjectId)
                .orElseThrow(() -> new NoSuchElementException("Study subject not found: " + studySubjectId));
        requireWriteAccess(currentUserId, studySubject.getStudyId());
        return accountRepository.findByStudySubjectId(studySubjectId)
                .map(this::toAccountDto)
                .orElseGet(() -> createAccount(defaultCreateRequest(studySubject), currentUserId));
    }

    @Transactional
    public ParticipantAccountDTO createAccount(CreateParticipantAccountRequest request, Integer currentUserId) {
        if (request.getStudySubjectId() == null) {
            throw new IllegalArgumentException("studySubjectId is required");
        }
        StudySubjectEntity studySubject = studySubjectRepository.findById(request.getStudySubjectId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Study subject not found: " + request.getStudySubjectId()));
        requireWriteAccess(currentUserId, studySubject.getStudyId());

        ParticipantAccount account = accountRepository.findByStudySubjectId(studySubject.getStudySubjectId())
                .orElseGet(ParticipantAccount::new);
        account.setStudyId(studySubject.getStudyId());
        account.setStudySubjectId(studySubject.getStudySubjectId());
        account.setSubjectId(studySubject.getSubjectId());
        account.setDisplayLabel(resolveDisplayLabel(request.getDisplayLabel(), studySubject));
        account.setPreferredLocale(request.getPreferredLocale());
        account.setStatus(ParticipantAccountStatus.ACTIVE);
        account.setCreatedBy(account.getCreatedBy() != null ? account.getCreatedBy() : currentUserId);
        account.setUpdatedDate(LocalDateTime.now());
        ParticipantAccount saved = accountRepository.save(account);
        record(saved.getStudyId(), AuditEventType.CREATE, "participant_account", saved.getId(),
                saved.getDisplayLabel(), currentUserId, "Participant account created");
        return toAccountDto(saved);
    }

    public List<ParticipantAccessTokenDTO> listTokens(Long participantAccountId, Integer currentUserId) {
        ParticipantAccount account = findAccount(participantAccountId);
        requireReadAccess(currentUserId, account.getStudyId());
        return tokenRepository.findByParticipantAccountIdOrderByIssuedDateDesc(participantAccountId)
                .stream()
                .map(this::toTokenDto)
                .toList();
    }

    @Transactional
    public IssuedParticipantTokenDTO issueToken(IssueParticipantTokenRequest request, Integer currentUserId) {
        ParticipantAccount account = findAccount(request.getParticipantAccountId());
        requireWriteAccess(currentUserId, account.getStudyId());
        if (account.getStatus() != ParticipantAccountStatus.ACTIVE) {
            throw new IllegalStateException("Participant account is not active");
        }

        String rawToken = tokenGenerator.generateToken();
        ParticipantAccessToken token = new ParticipantAccessToken();
        token.setParticipantAccountId(account.getId());
        token.setStudyId(account.getStudyId());
        token.setStudySubjectId(account.getStudySubjectId());
        token.setTokenHash(tokenGenerator.hashToken(rawToken));
        token.setScope(StringUtils.hasText(request.getScope()) ? request.getScope().trim() : "participant");
        token.setStatus(ParticipantAccessTokenStatus.ACTIVE);
        token.setIssuedBy(currentUserId);
        token.setExpiresAt(LocalDateTime.now().plusHours(resolveExpiryHours(request.getExpiresInHours())));
        ParticipantAccessToken saved = tokenRepository.save(token);
        record(saved.getStudyId(), AuditEventType.ASSIGN, "participant_access_token", saved.getId(),
                account.getDisplayLabel(), currentUserId, "Participant access token issued");

        IssuedParticipantTokenDTO dto = new IssuedParticipantTokenDTO();
        dto.setToken(toTokenDto(saved));
        dto.setRawToken(rawToken);
        dto.setEntryUrl("/participant/access/" + rawToken);
        return dto;
    }

    @Transactional
    public ParticipantAccessTokenDTO revokeToken(Long tokenId, String reason, Integer currentUserId) {
        ParticipantAccessToken token = findToken(tokenId);
        requireWriteAccess(currentUserId, token.getStudyId());
        if (token.getStatus() == ParticipantAccessTokenStatus.REVOKED) {
            return toTokenDto(token);
        }
        token.setStatus(ParticipantAccessTokenStatus.REVOKED);
        token.setRevokedBy(currentUserId);
        token.setRevokedDate(LocalDateTime.now());
        token.setRevocationReason(reason == null ? "" : reason);
        ParticipantAccessToken saved = tokenRepository.save(token);
        record(saved.getStudyId(), AuditEventType.UPDATE, "participant_access_token", saved.getId(),
                "token:" + saved.getId(), currentUserId, "Participant access token revoked");
        return toTokenDto(saved);
    }

    @Transactional
    public ParticipantBootstrapDTO verifyToken(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            throw new AccessDeniedException("Invalid participant access token");
        }
        ParticipantAccessToken token = tokenRepository.findByTokenHash(tokenGenerator.hashToken(rawToken))
                .orElseThrow(() -> new AccessDeniedException("Invalid participant access token"));
        if (token.getStatus() == ParticipantAccessTokenStatus.REVOKED
                || token.getStatus() == ParticipantAccessTokenStatus.EXPIRED) {
            throw new AccessDeniedException("Participant access token is not active");
        }
        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            token.setStatus(ParticipantAccessTokenStatus.EXPIRED);
            tokenRepository.save(token);
            record(token.getStudyId(), AuditEventType.SYSTEM, "participant_access_token", token.getId(),
                    "token:" + token.getId(), null, "Participant access token expired");
            throw new AccessDeniedException("Participant access token expired");
        }
        ParticipantAccount account = findAccount(token.getParticipantAccountId());
        if (account.getStatus() != ParticipantAccountStatus.ACTIVE) {
            throw new AccessDeniedException("Participant account is not active");
        }
        token.setStatus(ParticipantAccessTokenStatus.USED);
        token.setLastUsedAt(now);
        tokenRepository.save(token);
        record(token.getStudyId(), AuditEventType.VIEW, "participant_access_token", token.getId(),
                account.getDisplayLabel(), null, "Participant access token used");
        return toBootstrapDto(account, token);
    }

    @Transactional
    public int expireTokens(LocalDateTime now) {
        List<ParticipantAccessToken> expired = tokenRepository.findByStatusInAndExpiresAtBefore(
                EXPIRABLE_STATUSES, now);
        expired.forEach(token -> token.setStatus(ParticipantAccessTokenStatus.EXPIRED));
        tokenRepository.saveAll(expired);
        return expired.size();
    }

    private ParticipantAccount findAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Participant account not found: " + id));
    }

    private ParticipantAccessToken findToken(Long id) {
        return tokenRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Participant token not found: " + id));
    }

    private CreateParticipantAccountRequest defaultCreateRequest(StudySubjectEntity studySubject) {
        CreateParticipantAccountRequest request = new CreateParticipantAccountRequest();
        request.setStudySubjectId(studySubject.getStudySubjectId());
        request.setDisplayLabel(resolveDisplayLabel(null, studySubject));
        return request;
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

    private String resolveDisplayLabel(String requestedLabel, StudySubjectEntity studySubject) {
        if (StringUtils.hasText(requestedLabel)) {
            return requestedLabel.trim();
        }
        if (StringUtils.hasText(studySubject.getLabel())) {
            return studySubject.getLabel();
        }
        if (StringUtils.hasText(studySubject.getOcOid())) {
            return studySubject.getOcOid();
        }
        return "Study Subject " + studySubject.getStudySubjectId();
    }

    private int resolveExpiryHours(Integer requestedHours) {
        if (requestedHours == null) {
            return DEFAULT_EXPIRY_HOURS;
        }
        if (requestedHours <= 0 || requestedHours > 24 * 90) {
            throw new IllegalArgumentException("expiresInHours must be between 1 and 2160");
        }
        return requestedHours;
    }

    private ParticipantBootstrapDTO toBootstrapDto(ParticipantAccount account, ParticipantAccessToken token) {
        ParticipantBootstrapDTO dto = new ParticipantBootstrapDTO();
        dto.setTokenId(token.getId());
        dto.setParticipantAccountId(account.getId());
        dto.setStudyId(account.getStudyId());
        dto.setStudySubjectId(account.getStudySubjectId());
        dto.setDisplayLabel(account.getDisplayLabel());
        dto.setPreferredLocale(account.getPreferredLocale());
        dto.setScope(token.getScope());
        dto.setExpiresAt(token.getExpiresAt());
        return dto;
    }

    private ParticipantAccountDTO toAccountDto(ParticipantAccount account) {
        ParticipantAccountDTO dto = new ParticipantAccountDTO();
        dto.setId(account.getId());
        dto.setStudyId(account.getStudyId());
        dto.setStudySubjectId(account.getStudySubjectId());
        dto.setSubjectId(account.getSubjectId());
        dto.setDisplayLabel(account.getDisplayLabel());
        dto.setPreferredLocale(account.getPreferredLocale());
        dto.setStatus(account.getStatus());
        dto.setCreatedBy(account.getCreatedBy());
        dto.setCreatedDate(account.getCreatedDate());
        dto.setUpdatedDate(account.getUpdatedDate());
        return dto;
    }

    private ParticipantAccessTokenDTO toTokenDto(ParticipantAccessToken token) {
        ParticipantAccessTokenDTO dto = new ParticipantAccessTokenDTO();
        dto.setId(token.getId());
        dto.setParticipantAccountId(token.getParticipantAccountId());
        dto.setStudyId(token.getStudyId());
        dto.setStudySubjectId(token.getStudySubjectId());
        dto.setScope(token.getScope());
        dto.setStatus(token.getStatus());
        dto.setIssuedBy(token.getIssuedBy());
        dto.setIssuedDate(token.getIssuedDate());
        dto.setExpiresAt(token.getExpiresAt());
        dto.setLastUsedAt(token.getLastUsedAt());
        dto.setRevokedBy(token.getRevokedBy());
        dto.setRevokedDate(token.getRevokedDate());
        dto.setRevocationReason(token.getRevocationReason());
        return dto;
    }

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "participantaccess");
    }
}
