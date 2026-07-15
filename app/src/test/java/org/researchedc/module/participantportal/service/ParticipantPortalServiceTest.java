package org.researchedc.module.participantportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.ecoa.dto.EcoaAssignmentDTO;
import org.researchedc.module.ecoa.entity.EcoaAssignment;
import org.researchedc.module.ecoa.enums.EcoaAssignmentStatus;
import org.researchedc.module.ecoa.service.EcoaService;
import org.researchedc.module.econsent.dto.ConsentAssignmentDTO;
import org.researchedc.module.econsent.dto.ConsentTemplateDTO;
import org.researchedc.module.econsent.dto.ConsentVersionDTO;
import org.researchedc.module.econsent.dto.ParticipantConsentDTO;
import org.researchedc.module.econsent.enums.ConsentAssignmentStatus;
import org.researchedc.module.econsent.service.EconsentService;
import org.researchedc.module.participantaccess.dto.ParticipantBootstrapDTO;
import org.researchedc.module.participantaccess.service.ParticipantAccessService;

@ExtendWith(MockitoExtension.class)
class ParticipantPortalServiceTest {

    @Mock private ParticipantAccessService participantAccessService;
    @Mock private EcoaService ecoaService;
    @Mock private EconsentService econsentService;

    private ParticipantPortalService service;

    @BeforeEach
    void setUp() {
        service = new ParticipantPortalService(participantAccessService, ecoaService, econsentService);
    }

    @Test
    void bootstrap_aggregatesParticipantWorkIntoSortedTaskList() {
        ParticipantBootstrapDTO bootstrap = bootstrap();
        EcoaAssignment ecoaAssignment = new EcoaAssignment();
        EcoaAssignmentDTO ecoaDto = ecoaAssignmentDto(11L, EcoaAssignmentStatus.PENDING,
                LocalDateTime.now().plusDays(2));
        ParticipantConsentDTO consentDto = consentDto(22L, ConsentAssignmentStatus.ASSIGNED,
                LocalDateTime.now().plusDays(1));

        when(participantAccessService.verifyToken("raw-token")).thenReturn(bootstrap);
        when(ecoaService.listParticipantAssignmentsForAccount(5L)).thenReturn(List.of(ecoaAssignment));
        when(ecoaService.toAssignmentDto(ecoaAssignment)).thenReturn(ecoaDto);
        when(econsentService.listParticipantConsentsForAccount(5L)).thenReturn(List.of(consentDto));

        var result = service.bootstrap("raw-token");

        assertSame(bootstrap, result.getParticipant());
        assertEquals(2, result.getTasks().size());
        assertEquals("consent:22", result.getTasks().get(0).getId());
        assertEquals("ecoa:11", result.getTasks().get(1).getId());
        assertEquals(2, result.getSummary().getTotalTasks());
        assertEquals(1, result.getSummary().getQuestionnaireTasks());
        assertEquals(1, result.getSummary().getConsentTasks());
        assertEquals(2, result.getSummary().getActionableTasks());
        assertEquals(0, result.getSummary().getOverdueTasks());
        verify(participantAccessService).verifyToken("raw-token");
    }

    @Test
    void bootstrap_marksCompletedAndOverdueTasksNonUniformly() {
        ParticipantBootstrapDTO bootstrap = bootstrap();
        EcoaAssignment ecoaAssignment = new EcoaAssignment();
        EcoaAssignmentDTO overdueEcoa = ecoaAssignmentDto(11L, EcoaAssignmentStatus.OVERDUE,
                LocalDateTime.now().minusDays(1));
        ParticipantConsentDTO signedConsent = consentDto(22L, ConsentAssignmentStatus.PARTICIPANT_SIGNED, null);

        when(participantAccessService.verifyToken("raw-token")).thenReturn(bootstrap);
        when(ecoaService.listParticipantAssignmentsForAccount(5L)).thenReturn(List.of(ecoaAssignment));
        when(ecoaService.toAssignmentDto(ecoaAssignment)).thenReturn(overdueEcoa);
        when(econsentService.listParticipantConsentsForAccount(5L)).thenReturn(List.of(signedConsent));

        var result = service.bootstrap("raw-token");

        assertEquals(2, result.getSummary().getTotalTasks());
        assertEquals(1, result.getSummary().getOverdueTasks());
        assertEquals(0, result.getSummary().getActionableTasks());
        assertFalse(result.getTasks().get(0).isActionable());
    }

    private static ParticipantBootstrapDTO bootstrap() {
        ParticipantBootstrapDTO bootstrap = new ParticipantBootstrapDTO();
        bootstrap.setParticipantAccountId(5L);
        bootstrap.setStudyId(10);
        bootstrap.setStudySubjectId(100);
        bootstrap.setDisplayLabel("SS-001");
        bootstrap.setExpiresAt(LocalDateTime.now().plusDays(7));
        return bootstrap;
    }

    private static EcoaAssignmentDTO ecoaAssignmentDto(Long id, EcoaAssignmentStatus status, LocalDateTime dueAt) {
        EcoaAssignmentDTO dto = new EcoaAssignmentDTO();
        dto.setId(id);
        dto.setTaskInstanceId(101L);
        dto.setQuestionnaireAssignmentId("ISI daily");
        dto.setStatus(status);
        dto.setDueAt(dueAt);
        return dto;
    }

    private static ParticipantConsentDTO consentDto(Long id, ConsentAssignmentStatus status, LocalDateTime dueAt) {
        ConsentAssignmentDTO assignment = new ConsentAssignmentDTO();
        assignment.setId(id);
        assignment.setTaskInstanceId(202L);
        assignment.setStatus(status);
        assignment.setDueAt(dueAt);

        ConsentTemplateDTO template = new ConsentTemplateDTO();
        template.setName("Main Consent");
        template.setDescription("Read and sign the main consent");

        ConsentVersionDTO version = new ConsentVersionDTO();
        version.setVersionLabel("v1");
        version.setBodyText("Consent body");

        ParticipantConsentDTO dto = new ParticipantConsentDTO();
        dto.setAssignment(assignment);
        dto.setTemplate(template);
        dto.setVersion(version);
        return dto;
    }
}
