package org.researchedc.module.randomization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.researchedc.testutil.TestDataFactory.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.randomization.entity.RandomizationArm;
import org.researchedc.module.randomization.entity.RandomizationAssignment;
import org.researchedc.module.randomization.entity.RandomizationAuditLog;
import org.researchedc.module.randomization.entity.RandomizationScheme;
import org.researchedc.module.randomization.entity.UnblindingRequest;
import org.researchedc.module.randomization.enums.AssignmentStatus;
import org.researchedc.module.randomization.enums.SchemeStatus;
import org.researchedc.module.randomization.enums.UnblindingStatus;
import org.researchedc.module.randomization.repository.RandomizationAssignmentRepository;
import org.researchedc.module.randomization.repository.RandomizationAuditLogRepository;
import org.researchedc.module.randomization.repository.UnblindingRequestRepository;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class UnblindingServiceTest {

    @Mock private UnblindingRequestRepository requestRepository;
    @Mock private RandomizationAssignmentRepository assignmentRepository;
    @Mock private RandomizationAuditLogRepository auditLogRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;

    private UnblindingService service;

    @BeforeEach
    void setUp() {
        service = new UnblindingService(
                requestRepository, assignmentRepository, auditLogRepository, currentStudyAccessService);
    }

    @Test
    void requestUnblinding_whenWriteAllowed_savesRequestAndAuditWithStudy() {
        RandomizationAssignment assignment = assignment();
        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(currentStudyAccessService.canWriteStudy(42, 1)).thenReturn(true);
        when(requestRepository.save(any(UnblindingRequest.class)))
                .thenAnswer(invocation -> {
                    UnblindingRequest request = invocation.getArgument(0);
                    request.setId(200L);
                    return request;
                });

        var result = service.requestUnblinding(100L, 42, "Emergency");

        assertEquals(200L, result.getId());
        assertEquals(UnblindingStatus.PENDING, result.getStatus());
        verify(auditLogRepository).save(argThat(audit -> {
            assertEquals(1L, audit.getSchemeId());
            assertEquals(1, audit.getStudyId());
            assertEquals(42, audit.getPerformedBy());
            return true;
        }));
    }

    @Test
    void requestUnblinding_whenWriteDenied_throwsAndDoesNotSave() {
        RandomizationAssignment assignment = assignment();
        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(currentStudyAccessService.canWriteStudy(42, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.requestUnblinding(100L, 42, "Emergency"));
        verifyNoInteractions(requestRepository, auditLogRepository);
    }

    @Test
    void reviewUnblinding_whenWriteAllowed_approvesAndUnblindsAssignment() {
        UnblindingRequest request = pendingRequest();
        when(requestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(currentStudyAccessService.canWriteStudy(42, 1)).thenReturn(true);
        when(requestRepository.save(any(UnblindingRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.reviewUnblinding(200L, UnblindingStatus.APPROVED, 42, "Approved");

        assertEquals(UnblindingStatus.APPROVED, result.getStatus());
        assertEquals(AssignmentStatus.UNBLINDED, request.getAssignment().getStatus());
        verify(assignmentRepository).save(request.getAssignment());
        verify(auditLogRepository).save(any(RandomizationAuditLog.class));
    }

    @Test
    void reviewUnblinding_whenWriteDenied_throwsAndDoesNotSave() {
        UnblindingRequest request = pendingRequest();
        when(requestRepository.findById(200L)).thenReturn(Optional.of(request));
        when(currentStudyAccessService.canWriteStudy(42, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.reviewUnblinding(200L, UnblindingStatus.APPROVED, 42, "Approved"));
        verify(requestRepository, never()).save(any());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    void listPendingRequests_filtersUnreadableStudies() {
        UnblindingRequest readable = pendingRequest();
        UnblindingRequest hidden = pendingRequest();
        hidden.setId(201L);
        hidden.getAssignment().getScheme().setStudyId(2);
        when(requestRepository.findByStatusOrderByRequestedDateAsc(UnblindingStatus.PENDING))
                .thenReturn(List.of(readable, hidden));
        when(currentStudyAccessService.canReadStudy(42, 1)).thenReturn(true);
        when(currentStudyAccessService.canReadStudy(42, 2)).thenReturn(false);

        var result = service.listPendingRequests(42);

        assertEquals(1, result.size());
        assertEquals(readable.getId(), result.getFirst().getId());
    }

    private static UnblindingRequest pendingRequest() {
        UnblindingRequest request = new UnblindingRequest();
        request.setId(200L);
        request.setAssignment(assignment());
        request.setRequestedBy(10);
        request.setStatus(UnblindingStatus.PENDING);
        return request;
    }

    private static RandomizationAssignment assignment() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        RandomizationArm arm = createArm(1L, "Treatment", 1, 1);
        arm.setScheme(scheme);
        RandomizationAssignment assignment = createAssignment(100L, 1L, 10, arm, AssignmentStatus.ACTIVE);
        assignment.setScheme(scheme);
        return assignment;
    }
}
