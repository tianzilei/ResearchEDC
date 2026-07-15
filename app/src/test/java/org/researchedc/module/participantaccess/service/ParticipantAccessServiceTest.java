package org.researchedc.module.participantaccess.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.participantaccess.dto.CreateParticipantAccountRequest;
import org.researchedc.module.participantaccess.dto.IssueParticipantTokenRequest;
import org.researchedc.module.participantaccess.dto.IssuedParticipantTokenDTO;
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

@ExtendWith(MockitoExtension.class)
class ParticipantAccessServiceTest {

    @Mock private ParticipantAccountRepository accountRepository;
    @Mock private ParticipantAccessTokenRepository tokenRepository;
    @Mock private StudySubjectRepository studySubjectRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;
    @Mock private ParticipantTokenGenerator tokenGenerator;

    private ParticipantAccessService service;

    @BeforeEach
    void setUp() {
        service = new ParticipantAccessService(accountRepository, tokenRepository, studySubjectRepository,
                currentStudyAccessService, auditService, tokenGenerator);
    }

    @Test
    void createAccount_whenStudySubjectExists_createsStudyScopedAccount() {
        CreateParticipantAccountRequest request = new CreateParticipantAccountRequest();
        request.setStudySubjectId(100);
        request.setPreferredLocale("zh-CN");
        when(studySubjectRepository.findById(100)).thenReturn(Optional.of(studySubject()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(accountRepository.findByStudySubjectId(100)).thenReturn(Optional.empty());
        when(accountRepository.save(any(ParticipantAccount.class))).thenAnswer(invocation -> {
            ParticipantAccount account = invocation.getArgument(0);
            account.setId(5L);
            return account;
        });

        var result = service.createAccount(request, 42);

        assertEquals(5L, result.getId());
        assertEquals(10, result.getStudyId());
        assertEquals("SS-001", result.getDisplayLabel());
        assertEquals(ParticipantAccountStatus.ACTIVE, result.getStatus());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("participant_account"),
                eq(5L), eq("SS-001"), isNull(), isNull(), eq(42),
                eq("Participant account created"), eq("participantaccess"));
    }

    @Test
    void createAccount_whenAccessDenied_doesNotSave() {
        CreateParticipantAccountRequest request = new CreateParticipantAccountRequest();
        request.setStudySubjectId(100);
        when(studySubjectRepository.findById(100)).thenReturn(Optional.of(studySubject()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.createAccount(request, 42));
        verify(accountRepository, never()).save(any());
    }

    @Test
    void findOrCreateAccountForStudySubject_whenMissing_createsDefaultAccount() {
        when(studySubjectRepository.findById(100)).thenReturn(Optional.of(studySubject()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(accountRepository.findByStudySubjectId(100)).thenReturn(Optional.empty());
        when(accountRepository.save(any(ParticipantAccount.class))).thenAnswer(invocation -> {
            ParticipantAccount account = invocation.getArgument(0);
            account.setId(5L);
            return account;
        });

        var result = service.findOrCreateAccountForStudySubject(100, 42);

        assertEquals(5L, result.getId());
        assertEquals("SS-001", result.getDisplayLabel());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("participant_account"),
                eq(5L), eq("SS-001"), isNull(), isNull(), eq(42),
                eq("Participant account created"), eq("participantaccess"));
    }

    @Test
    void findOrCreateAccountForStudySubject_whenExisting_reusesAccount() {
        when(studySubjectRepository.findById(100)).thenReturn(Optional.of(studySubject()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(accountRepository.findByStudySubjectId(100)).thenReturn(Optional.of(account()));

        var result = service.findOrCreateAccountForStudySubject(100, 42);

        assertEquals(5L, result.getId());
        verify(accountRepository, never()).save(any());
    }


    @Test
    void issueToken_hashesTokenAndReturnsRawOnlyInResponse() {
        ParticipantAccount account = account();
        IssueParticipantTokenRequest request = new IssueParticipantTokenRequest();
        request.setParticipantAccountId(5L);
        request.setExpiresInHours(24);
        request.setScope("questionnaire");
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(tokenGenerator.generateToken()).thenReturn("raw-token");
        when(tokenGenerator.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.save(any(ParticipantAccessToken.class))).thenAnswer(invocation -> {
            ParticipantAccessToken token = invocation.getArgument(0);
            token.setId(9L);
            return token;
        });

        IssuedParticipantTokenDTO result = service.issueToken(request, 42);

        assertEquals("raw-token", result.getRawToken());
        assertEquals("/participant/access/raw-token", result.getEntryUrl());
        assertEquals("questionnaire", result.getToken().getScope());
        verify(tokenRepository).save(argThat(token -> {
            assertEquals("hashed-token", token.getTokenHash());
            assertEquals(ParticipantAccessTokenStatus.ACTIVE, token.getStatus());
            assertEquals(10, token.getStudyId());
            return true;
        }));
    }

    @Test
    void verifyToken_whenActive_marksUsedAndReturnsBootstrap() {
        ParticipantAccessToken token = token(ParticipantAccessTokenStatus.ACTIVE);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(tokenGenerator.hashToken("raw")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account()));
        when(tokenRepository.save(token)).thenReturn(token);

        ParticipantBootstrapDTO result = service.verifyToken("raw");

        assertEquals(9L, result.getTokenId());
        assertEquals(5L, result.getParticipantAccountId());
        assertEquals(100, result.getStudySubjectId());
        assertEquals("participant", result.getScope());
        assertEquals(ParticipantAccessTokenStatus.USED, token.getStatus());
        assertNotNull(token.getLastUsedAt());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.VIEW), eq("participant_access_token"),
                eq(9L), eq("SS-001"), isNull(), isNull(), isNull(),
                eq("Participant access token used"), eq("participantaccess"));
    }

    @Test
    void verifyToken_whenExpired_marksExpiredAndDenies() {
        ParticipantAccessToken token = token(ParticipantAccessTokenStatus.ACTIVE);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenGenerator.hashToken("raw")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        assertThrows(AccessDeniedException.class, () -> service.verifyToken("raw"));

        assertEquals(ParticipantAccessTokenStatus.EXPIRED, token.getStatus());
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyToken_whenRevoked_denies() {
        ParticipantAccessToken token = token(ParticipantAccessTokenStatus.REVOKED);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        when(tokenGenerator.hashToken("raw")).thenReturn("hash");
        when(tokenRepository.findByTokenHash("hash")).thenReturn(Optional.of(token));

        assertThrows(AccessDeniedException.class, () -> service.verifyToken("raw"));
    }

    @Test
    void revokeToken_marksRevokedAndAudits() {
        ParticipantAccessToken token = token(ParticipantAccessTokenStatus.ACTIVE);
        when(tokenRepository.findById(9L)).thenReturn(Optional.of(token));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(tokenRepository.save(token)).thenReturn(token);

        var result = service.revokeToken(9L, "withdrawn", 42);

        assertEquals(ParticipantAccessTokenStatus.REVOKED, result.getStatus());
        assertEquals("withdrawn", result.getRevocationReason());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.UPDATE), eq("participant_access_token"),
                eq(9L), eq("token:9"), isNull(), isNull(), eq(42),
                eq("Participant access token revoked"), eq("participantaccess"));
    }

    @Test
    void expireTokens_marksActiveAndUsedTokensExpired() {
        ParticipantAccessToken token = token(ParticipantAccessTokenStatus.USED);
        LocalDateTime now = LocalDateTime.now();
        when(tokenRepository.findByStatusInAndExpiresAtBefore(
                Set.of(ParticipantAccessTokenStatus.ACTIVE, ParticipantAccessTokenStatus.USED), now))
                .thenReturn(List.of(token));

        int count = service.expireTokens(now);

        assertEquals(1, count);
        assertEquals(ParticipantAccessTokenStatus.EXPIRED, token.getStatus());
        verify(tokenRepository).saveAll(List.of(token));
    }

    private static StudySubjectEntity studySubject() {
        StudySubjectEntity studySubject = new StudySubjectEntity();
        studySubject.setStudySubjectId(100);
        studySubject.setStudyId(10);
        studySubject.setSubjectId(20);
        studySubject.setLabel("SS-001");
        return studySubject;
    }

    private static ParticipantAccount account() {
        ParticipantAccount account = new ParticipantAccount();
        account.setId(5L);
        account.setStudyId(10);
        account.setStudySubjectId(100);
        account.setSubjectId(20);
        account.setDisplayLabel("SS-001");
        account.setStatus(ParticipantAccountStatus.ACTIVE);
        return account;
    }

    private static ParticipantAccessToken token(ParticipantAccessTokenStatus status) {
        ParticipantAccessToken token = new ParticipantAccessToken();
        token.setId(9L);
        token.setParticipantAccountId(5L);
        token.setStudyId(10);
        token.setStudySubjectId(100);
        token.setScope("participant");
        token.setStatus(status);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        return token;
    }
}
