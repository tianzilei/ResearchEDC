package org.researchedc.module.randomization.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.researchedc.testutil.TestDataFactory.*;
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

@ExtendWith(MockitoExtension.class)
class RandomizationServiceTest {

    @Mock private RandomizationSchemeRepository schemeRepository;
    @Mock private RandomizationArmRepository armRepository;
    @Mock private RandomizationAssignmentRepository assignmentRepository;
    @Mock private RandomizationBlockRepository blockRepository;
    @Mock private RandomizationAuditLogRepository auditLogRepository;

    private RandomizationService service;

    @BeforeEach
    void setUp() {
        SimpleRandomization simpleStrategy = new SimpleRandomization();
        BlockRandomization blockStrategy = new BlockRandomization(blockRepository);
        StratifiedBlockRandomization stratifiedStrategy = new StratifiedBlockRandomization(blockRepository);

        service = new RandomizationService(
                schemeRepository, armRepository, assignmentRepository,
                blockRepository, auditLogRepository,
                simpleStrategy, blockStrategy, stratifiedStrategy);
    }

    @Test
    void listSchemes_delegatesToRepository() {
        when(schemeRepository.findByStudyIdOrderByCreatedDateDesc(1))
                .thenReturn(List.of(createScheme(1L, "S1", SchemeStatus.DRAFT)));

        List<SchemeSummaryDTO> result = service.listSchemes(1);

        assertEquals(1, result.size());
        assertEquals("S1", result.getFirst().getName());
    }

    @Test
    void getScheme_whenFound_returnsDTO() {
        RandomizationScheme scheme = createScheme(1L, "My Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));

        SchemeDTO result = service.getScheme(1L);

        assertEquals("My Scheme", result.getName());
        assertEquals(RandomizationAlgorithm.SIMPLE, result.getAlgorithm());
        assertEquals(SchemeStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getScheme_whenNotFound_throwsException() {
        when(schemeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getScheme(99L));
    }

    @Test
    void createScheme_savesAndReturnsDTO() {
        SchemeDTO input = new SchemeDTO();
        input.setStudyId(1);
        input.setName("New Scheme");
        input.setAlgorithm(RandomizationAlgorithm.SIMPLE);

        RandomizationScheme savedScheme = createScheme(1L, "New Scheme", SchemeStatus.DRAFT);
        when(schemeRepository.save(any())).thenReturn(savedScheme);

        SchemeDTO result = service.createScheme(input, 100);

        assertEquals("New Scheme", result.getName());
        assertEquals(SchemeStatus.DRAFT, result.getStatus());
        verify(auditLogRepository).save(any());
    }

    @Test
    void activateScheme_changesStatusAndLogsAudit() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.DRAFT);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(schemeRepository.save(any())).thenReturn(scheme);

        service.activateScheme(1L, 100);

        assertEquals(SchemeStatus.ACTIVE, scheme.getStatus());
        verify(auditLogRepository).save(any());
    }

    @Test
    void closeScheme_changesStatusAndLogsAudit() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.ACTIVE);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));
        when(schemeRepository.save(any())).thenReturn(scheme);

        service.closeScheme(1L, 100);

        assertEquals(SchemeStatus.CLOSED, scheme.getStatus());
    }

    @Test
    void randomize_whenSchemeNotActive_throwsException() {
        RandomizationScheme scheme = createScheme(1L, "Scheme", SchemeStatus.DRAFT);
        when(schemeRepository.findById(1L)).thenReturn(Optional.of(scheme));

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
        when(assignmentRepository.findBySchemeIdAndStudySubjectId(1L, 10))
                .thenReturn(Optional.empty());
        when(armRepository.findBySchemeIdOrderByOrderNumber(1L))
                .thenReturn(List.of(arm));

        RandomizationAssignment savedAssignment = new RandomizationAssignment();
        savedAssignment.setId(100L);
        savedAssignment.setScheme(scheme);
        savedAssignment.setStudySubjectId(10);
        savedAssignment.setArm(arm);
        savedAssignment.setStatus(AssignmentStatus.ACTIVE);
        when(assignmentRepository.save(any())).thenReturn(savedAssignment);

        RandomizeRequest request = new RandomizeRequest();
        request.setSchemeId(1L);
        request.setStudySubjectId(10);

        AssignmentDTO result = service.randomize(request, 100);

        assertNotNull(result);
        assertEquals("Treatment", result.getArmName());
        assertEquals(AssignmentStatus.ACTIVE, result.getStatus());
        verify(auditLogRepository).save(any());
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

        AssignmentDTO result = service.getAssignment(1L, 10);

        assertEquals("Control", result.getArmName());
        assertEquals(10, result.getStudySubjectId());
    }

    @Test
    void getAssignment_whenNotFound_throwsException() {
        when(assignmentRepository.findBySchemeIdAndStudySubjectId(1L, 10))
                .thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getAssignment(1L, 10));
    }

}
