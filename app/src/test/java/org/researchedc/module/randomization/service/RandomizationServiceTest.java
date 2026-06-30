package org.researchedc.module.randomization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.researchedc.testutil.TestDataFactory.*;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.randomization.algorithms.BlockRandomization;
import org.researchedc.module.randomization.algorithms.SimpleRandomization;
import org.researchedc.module.randomization.algorithms.StratifiedBlockRandomization;
import org.researchedc.module.randomization.dto.*;
import org.researchedc.module.randomization.entity.*;
import org.researchedc.module.randomization.enums.*;
import org.researchedc.module.randomization.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class RandomizationServiceTest {

    @Mock private RandomizationSchemeRepository schemeRepository;
    @Mock private RandomizationArmRepository armRepository;
    @Mock private RandomizationAssignmentRepository assignmentRepository;
    @Mock private RandomizationBlockRepository blockRepository;
    @Mock private RandomizationAuditLogRepository auditLogRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;

    private RandomizationService service;

    @BeforeEach
    void setUp() {
        SimpleRandomization simpleStrategy = new SimpleRandomization();
        BlockRandomization blockStrategy = new BlockRandomization(blockRepository);
        StratifiedBlockRandomization stratifiedStrategy = new StratifiedBlockRandomization(blockRepository);

        service = new RandomizationService(
                schemeRepository, armRepository, assignmentRepository,
                blockRepository, auditLogRepository, currentStudyAccessService,
                simpleStrategy, blockStrategy, stratifiedStrategy);
    }

    @Test
    void listSchemes_delegatesToRepository() {
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(true);
        when(schemeRepository.findByStudyIdOrderByCreatedDateDesc(1))
                .thenReturn(List.of(createScheme(1L, "S1", SchemeStatus.DRAFT)));

        List<SchemeSummaryDTO> result = service.listSchemes(1, 100);

        assertEquals(1, result.size());
        assertEquals("S1", result.getFirst().getName());
    }

    @Test
    void listSchemes_whenReadDenied_throwsAndDoesNotQuery() {
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listSchemes(1, 100));
        verifyNoInteractions(schemeRepository);
    }

    @Test
    void getScheme_whenFound_returnsDTO() {
        RandomizationScheme scheme = createScheme(1L, "My Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(true);

        SchemeDTO result = service.getScheme(1L, 100);

        assertEquals("My Scheme", result.getName());
        assertEquals(RandomizationAlgorithm.SIMPLE, result.getAlgorithm());
        assertEquals(SchemeStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getScheme_whenNotFound_throwsException() {
        when(schemeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getScheme(99L, 100));
    }

    @Test
    void getScheme_whenReadDenied_throws() {
        RandomizationScheme scheme = createScheme(1L, "My Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getScheme(1L, 100));
    }

    @Test
    void createScheme_savesAndReturnsDTO() {
        SchemeDTO input = new SchemeDTO();
        input.setStudyId(1);
        input.setName("New Scheme");
        input.setAlgorithm(RandomizationAlgorithm.SIMPLE);

        RandomizationScheme savedScheme = createScheme(1L, "New Scheme", SchemeStatus.DRAFT);
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(true);
        when(schemeRepository.save(any())).thenReturn(savedScheme);

        SchemeDTO result = service.createScheme(input, 100);

        assertEquals("New Scheme", result.getName());
        assertEquals(SchemeStatus.DRAFT, result.getStatus());
        verify(auditLogRepository).save(any());
    }

    @Test
    void createScheme_whenWriteDenied_throwsAndDoesNotSave() {
        SchemeDTO input = new SchemeDTO();
        input.setStudyId(1);
        input.setName("New Scheme");
        input.setAlgorithm(RandomizationAlgorithm.SIMPLE);
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.createScheme(input, 100));
        verify(schemeRepository, never()).save(any());
    }

    @Test
    void activateScheme_changesStatusAndLogsAudit() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.DRAFT);
        scheme.setArms(List.of(createArm(1L, "Active", 1, 1)));
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(true);
        when(schemeRepository.save(any())).thenReturn(scheme);

        service.activateScheme(1L, 100);

        assertEquals(SchemeStatus.ACTIVE, scheme.getStatus());
        verify(auditLogRepository).save(any());
    }

    @Test
    void closeScheme_changesStatusAndLogsAudit() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(true);
        when(schemeRepository.save(any())).thenReturn(scheme);

        service.closeScheme(1L, 100);

        assertEquals(SchemeStatus.CLOSED, scheme.getStatus());
    }

    @Test
    void randomize_whenSchemeNotActive_throwsException() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.DRAFT);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(true);

        RandomizeRequest request = new RandomizeRequest();
        request.setSchemeId(1L);
        request.setStudySubjectId(10);

        assertThrows(IllegalStateException.class, () -> service.randomize(request, 100));
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void randomize_whenSubjectAlreadyAssigned_throwsException() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        scheme.setAlgorithm(RandomizationAlgorithm.SIMPLE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(true);
        when(assignmentRepository.findBySchemeIdAndStudySubjectId(1L, 10))
                .thenReturn(Optional.of(new RandomizationAssignment()));

        RandomizeRequest request = new RandomizeRequest();
        request.setSchemeId(1L);
        request.setStudySubjectId(10);

        assertThrows(IllegalStateException.class, () -> service.randomize(request, 100));
    }

    @Test
    void randomize_withSimpleAlgorithm_returnsAssignment() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        scheme.setAlgorithm(RandomizationAlgorithm.SIMPLE);

        RandomizationArm arm = createArm(1L, "Treatment", 1, 1);
        arm.setScheme(scheme);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(true);
        when(assignmentRepository.findBySchemeIdAndStudySubjectId(1L, 10))
                .thenReturn(Optional.empty());
        when(armRepository.findBySchemeIdOrderByOrderNumber(1L))
                .thenReturn(List.of(arm));

        when(assignmentRepository.save(any())).thenAnswer(invocation -> {
            RandomizationAssignment savedAssignment = invocation.getArgument(0);
            savedAssignment.setId(100L);
            return savedAssignment;
        });

        RandomizeRequest request = new RandomizeRequest();
        request.setSchemeId(1L);
        request.setStudySubjectId(10);
        request.setAssignedBy(999);

        AssignmentDTO result = service.randomize(request, 100);

        assertNotNull(result);
        assertEquals("Treatment", result.getArmName());
        assertEquals(AssignmentStatus.ACTIVE, result.getStatus());
        assertEquals(100, result.getAssignedBy());
        verify(auditLogRepository).save(any());
    }

    @Test
    void randomize_whenWriteDenied_throwsAndDoesNotAssign() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canWriteStudy(100, 1)).thenReturn(false);

        RandomizeRequest request = new RandomizeRequest();
        request.setSchemeId(1L);
        request.setStudySubjectId(10);

        assertThrows(AccessDeniedException.class, () -> service.randomize(request, 100));
        verifyNoInteractions(assignmentRepository);
    }

    @Test
    void getAssignment_returnsDTO() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        RandomizationArm arm = createArm(1L, "Control", 1, 1);
        arm.setScheme(scheme);

        RandomizationAssignment assignment = new RandomizationAssignment();
        assignment.setId(100L);
        assignment.setScheme(scheme);
        assignment.setStudySubjectId(10);
        assignment.setArm(arm);
        assignment.setStatus(AssignmentStatus.ACTIVE);

        when(assignmentRepository.findBySchemeIdAndStudySubjectId(1L, 10))
                .thenReturn(Optional.of(assignment));
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(true);

        AssignmentDTO result = service.getAssignment(1L, 10, 100);

        assertEquals("Control", result.getArmName());
        assertEquals(10, result.getStudySubjectId());
    }

    @Test
    void getAssignment_whenNotFound_throwsException() {
        when(assignmentRepository.findBySchemeIdAndStudySubjectId(1L, 10))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getAssignment(1L, 10, 100));
    }

    @Test
    void listAssignments_whenReadAllowed_returnsActiveAssignments() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        RandomizationArm arm = createArm(1L, "Control", 1, 1);
        arm.setScheme(scheme);
        RandomizationAssignment assignment = createAssignment(100L, 1L, 10, arm, AssignmentStatus.ACTIVE);
        assignment.setScheme(scheme);

        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(true);
        when(assignmentRepository.findBySchemeIdAndStatusOrderByAssignedDateDesc(1L, AssignmentStatus.ACTIVE))
                .thenReturn(List.of(assignment));

        List<AssignmentDTO> result = service.listAssignments(1L, 100);

        assertEquals(1, result.size());
        assertEquals(10, result.getFirst().getStudySubjectId());
    }

    @Test
    void listAssignments_whenReadDenied_throwsAndDoesNotQueryAssignments() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listAssignments(1L, 100));
        verify(assignmentRepository, never())
                .findBySchemeIdAndStatusOrderByAssignedDateDesc(anyLong(), any());
    }

    @Test
    void getStudyAuditLogs_whenReadDenied_throwsAndDoesNotQuery() {
        when(currentStudyAccessService.canReadStudy(100, 1)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.getStudyAuditLogs(1, 100));
        verifyNoInteractions(auditLogRepository);
    }

}
