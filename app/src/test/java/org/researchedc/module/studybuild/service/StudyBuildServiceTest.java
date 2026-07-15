package org.researchedc.module.studybuild.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.study.dto.CreateStudyRequest;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.service.StudyService;
import org.researchedc.module.studybuild.dto.ApplyStudyTemplateRequest;
import org.researchedc.module.studybuild.dto.CreateStudyTemplateRequest;
import org.researchedc.module.studybuild.entity.StudyTemplateEntity;
import org.researchedc.module.studybuild.repository.StudyTemplateRepository;

@ExtendWith(MockitoExtension.class)
class StudyBuildServiceTest {

    @Mock private StudyTemplateRepository templateRepository;
    @Mock private StudyService studyService;
    @Mock private AuditService auditService;

    private StudyBuildService service;

    @BeforeEach
    void setUp() {
        service = new StudyBuildService(templateRepository, studyService, auditService, new ObjectMapper());
    }

    @Test
    void createTemplate_savesDefaultsAndAudits() {
        CreateStudyTemplateRequest request = new CreateStudyTemplateRequest();
        request.setName("Oncology Phase II");
        request.setProtocolType("Interventional");
        request.setPhase("Phase 2");
        request.setDefaults(Map.of("purpose", "Treatment", "statusId", 1));
        when(templateRepository.save(any(StudyTemplateEntity.class))).thenAnswer(invocation -> {
            StudyTemplateEntity entity = invocation.getArgument(0);
            entity.setId(7L);
            entity.setCreatedDate(LocalDateTime.now());
            return entity;
        });

        var result = service.createTemplate(request, 42);

        assertEquals(7L, result.getId());
        assertEquals("Treatment", result.getDefaults().get("purpose"));
        verify(auditService).recordAudit(isNull(), eq(AuditEventType.CREATE), eq("study_template"),
                eq(7L), eq("Oncology Phase II"), isNull(), isNull(), eq(42),
                eq("Study template created"), eq("studybuild"));
    }

    @Test
    void listTemplates_returnsActiveTemplates() {
        StudyTemplateEntity entity = template();
        when(templateRepository.findByActiveTrueOrderByName()).thenReturn(List.of(entity));

        var result = service.listTemplates();

        assertEquals(1, result.size());
        assertEquals("Oncology", result.get(0).getName());
    }

    @Test
    void applyTemplate_buildsStudyFromDefaultsAndOverrides() {
        StudyTemplateEntity template = template();
        when(templateRepository.findById(3L)).thenReturn(Optional.of(template));
        when(studyService.createStudy(any(CreateStudyRequest.class), eq(42))).thenAnswer(invocation -> {
            CreateStudyRequest request = invocation.getArgument(0);
            StudyDetailDTO study = new StudyDetailDTO();
            study.setStudyId(99);
            study.setName(request.getName());
            study.setUniqueIdentifier(request.getUniqueIdentifier());
            study.setProtocolType(request.getProtocolType());
            study.setPhase(request.getPhase());
            return study;
        });

        ApplyStudyTemplateRequest request = new ApplyStudyTemplateRequest();
        request.setName("Lung Study");
        request.setUniqueIdentifier("LUNG-001");
        request.setPrincipalInvestigator("Dr. Lin");
        request.setExpectedTotalEnrollment(120);

        var result = service.applyTemplate(3L, request, 42);

        assertEquals(99, result.getStudy().getStudyId());
        ArgumentCaptor<CreateStudyRequest> captor = ArgumentCaptor.forClass(CreateStudyRequest.class);
        verify(studyService).createStudy(captor.capture(), eq(42));
        assertEquals("LUNG-001", captor.getValue().getUniqueIdentifier());
        assertEquals("Interventional", captor.getValue().getProtocolType());
        assertEquals("Phase 2", captor.getValue().getPhase());
        assertEquals(120, captor.getValue().getExpectedTotalEnrollment());
        verify(auditService).recordAudit(eq(99), eq(AuditEventType.CREATE), eq("study_template_application"),
                eq(3L), eq("Oncology"), isNull(), isNull(), eq(42),
                eq("Study created from template"), eq("studybuild"));
    }

    @Test
    void applyTemplate_requiresStudyIdentifiers() {
        when(templateRepository.findById(3L)).thenReturn(Optional.of(template()));

        ApplyStudyTemplateRequest request = new ApplyStudyTemplateRequest();
        request.setName("Missing Identifier");

        assertThrows(IllegalArgumentException.class, () -> service.applyTemplate(3L, request, 42));
        verifyNoInteractions(studyService);
    }

    private static StudyTemplateEntity template() {
        StudyTemplateEntity entity = new StudyTemplateEntity();
        entity.setId(3L);
        entity.setName("Oncology");
        entity.setProtocolType("Interventional");
        entity.setPhase("Phase 2");
        entity.setActive(true);
        entity.setCreatedDate(LocalDateTime.now());
        entity.setDefaultsJson("{\"statusId\":1,\"typeId\":1,\"purpose\":\"Treatment\",\"expectedTotalEnrollment\":80}");
        return entity;
    }
}
