package org.researchedc.module.study.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import static org.researchedc.testutil.TestDataFactory.*;

import org.researchedc.module.study.application.command.CreateStudyCommand;
import org.researchedc.module.study.application.command.UpdateStudyCommand;
import org.researchedc.module.study.domain.StudyDomainService;
import org.researchedc.module.study.domain.StudyPolicy;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.event.StudyChangedEvent;
import org.researchedc.module.study.service.StudyService;
import org.researchedc.module.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class StudyApplicationServiceTest {

    @Mock private StudyService studyService;
    @Mock private StudyPolicy studyPolicy;
    @Mock private StudyDomainService studyDomainService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AuditService auditService;

    private StudyApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new StudyApplicationService(
                studyService, studyPolicy, studyDomainService,
                eventPublisher, auditService);
    }

    @Test
    void createStudy_delegatesToServiceAndPublishesEvent() {
        CreateStudyCommand cmd = new CreateStudyCommand();
        cmd.setName("New Study");
        cmd.setUniqueIdentifier("NEW-001");
        cmd.setStatusId(1);
        cmd.setTypeId(1);

        when(studyPolicy.validateCreate(cmd)).thenReturn(List.of());
        when(studyService.createStudy(any(), eq(42)))
                .thenReturn(createDetail(1, "New Study"));

        ArgumentCaptor<StudyChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(StudyChangedEvent.class);

        StudyDetailDTO result = applicationService.createStudy(cmd, 42);

        assertEquals("New Study", result.getName());
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(StudyChangedEvent.ChangeType.CREATED,
                eventCaptor.getValue().getChangeType());
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void createStudy_withValidationErrors_throwsException() {
        CreateStudyCommand cmd = new CreateStudyCommand();
        when(studyPolicy.validateCreate(cmd))
                .thenReturn(List.of("Name is required"));

        assertThrows(IllegalArgumentException.class,
                () -> applicationService.createStudy(cmd, 42));
        verifyNoInteractions(studyService, eventPublisher, auditService);
    }

    @Test
    void updateStudy_delegatesToServiceAndPublishesEvent() {
        UpdateStudyCommand cmd = new UpdateStudyCommand();
        cmd.setStudyId(1);
        cmd.setName("Updated Name");
        cmd.setUpdatedBy(99);

        when(studyPolicy.validateUpdate(cmd)).thenReturn(List.of());
        when(studyService.updateStudy(eq(1), any(), eq(99)))
                .thenReturn(createDetail(1, "Updated Name"));

        ArgumentCaptor<StudyChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(StudyChangedEvent.class);

        StudyDetailDTO result = applicationService.updateStudy(cmd);

        assertEquals("Updated Name", result.getName());
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(StudyChangedEvent.ChangeType.UPDATED,
                eventCaptor.getValue().getChangeType());
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void deleteStudy_publishesDeletedEvent() {
        applicationService.deleteStudy(1, 42);

        ArgumentCaptor<StudyChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(StudyChangedEvent.class);

        verify(studyService).deleteStudy(1, 42);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(StudyChangedEvent.ChangeType.DELETED,
                eventCaptor.getValue().getChangeType());
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void updateStudyStatus_publishesStatusChangedEvent() {
        applicationService.updateStudyStatus(1, 2, 42);

        ArgumentCaptor<StudyChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(StudyChangedEvent.class);

        verify(studyService).updateStudyStatus(1, 2, 42);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(StudyChangedEvent.ChangeType.STATUS_CHANGED,
                eventCaptor.getValue().getChangeType());
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void listStudies_delegatesToService() {
        when(studyService.listStudies()).thenReturn(List.of());

        applicationService.listStudies();

        verify(studyService).listStudies();
    }

    @Test
    void getStudy_delegatesToService() {
        when(studyService.getStudy(1)).thenReturn(createDetail(1, "Test"));

        applicationService.getStudy(1);

        verify(studyService).getStudy(1);
    }

    private StudyDetailDTO createDetail(Integer id, String name) {
        StudyDetailDTO dto = new StudyDetailDTO();
        dto.setStudyId(id);
        dto.setName(name);
        return dto;
    }
}
