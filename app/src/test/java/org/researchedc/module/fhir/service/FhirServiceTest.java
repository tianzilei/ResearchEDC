package org.researchedc.module.fhir.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.fhir.dto.CreateFhirConnectorRequest;
import org.researchedc.module.fhir.dto.ReconcileFhirRecordRequest;
import org.researchedc.module.fhir.dto.SubmitFhirResourceRequest;
import org.researchedc.module.fhir.entity.FhirConnectorEntity;
import org.researchedc.module.fhir.entity.FhirImportRecordEntity;
import org.researchedc.module.fhir.enums.FhirImportStatus;
import org.researchedc.module.fhir.repository.FhirConnectorRepository;
import org.researchedc.module.fhir.repository.FhirImportRecordRepository;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class FhirServiceTest {

    @Mock private FhirConnectorRepository connectorRepository;
    @Mock private FhirImportRecordRepository recordRepository;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    @Mock private AuditService auditService;

    private FhirService service;

    @BeforeEach
    void setUp() {
        service = new FhirService(connectorRepository, recordRepository, currentStudyAccessService,
                auditService, new ObjectMapper());
    }

    @Test
    void createConnector_whenWriteAccess_savesAndAudits() {
        CreateFhirConnectorRequest request = new CreateFhirConnectorRequest();
        request.setStudyId(10);
        request.setName("Hospital FHIR");
        request.setBaseUrl("https://ehr.example/fhir");
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(connectorRepository.save(any(FhirConnectorEntity.class))).thenAnswer(invocation -> {
            FhirConnectorEntity connector = invocation.getArgument(0);
            connector.setId(1L);
            connector.setCreatedDate(LocalDateTime.now());
            return connector;
        });

        var result = service.createConnector(request, 42);

        assertEquals(1L, result.getId());
        assertEquals("Hospital FHIR", result.getName());
        verify(auditService).recordAudit(eq(10), eq(AuditEventType.CREATE), eq("fhir_connector"),
                eq(1L), eq("Hospital FHIR"), isNull(), isNull(), eq(42),
                eq("FHIR connector configured"), eq("fhir"));
    }

    @Test
    void submitResource_mapsPatientPayload() {
        FhirConnectorEntity connector = connector();
        SubmitFhirResourceRequest request = new SubmitFhirResourceRequest();
        request.setConnectorId(1L);
        request.setPayloadJson("""
                {"resourceType":"Patient","id":"pat-1","gender":"female","identifier":[{"value":"MRN-001"}]}
                """);
        when(connectorRepository.findById(1L)).thenReturn(Optional.of(connector));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(recordRepository.save(any(FhirImportRecordEntity.class))).thenAnswer(invocation -> {
            FhirImportRecordEntity record = invocation.getArgument(0);
            record.setId(2L);
            record.setCreatedDate(LocalDateTime.now());
            return record;
        });

        var result = service.submitResource(request, 42);

        assertEquals(FhirImportStatus.MAPPED, result.getStatus());
        assertEquals("pat-1", result.getExternalId());
        assertEquals("MRN-001", result.getMappedSubjectIdentifier());
        assertEquals("female", result.getMappedGender());
    }

    @Test
    void submitResource_rejectsUnsupportedResource() {
        SubmitFhirResourceRequest request = new SubmitFhirResourceRequest();
        request.setConnectorId(1L);
        request.setPayloadJson("{\"resourceType\":\"Encounter\",\"id\":\"enc-1\"}");
        when(connectorRepository.findById(1L)).thenReturn(Optional.of(connector()));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.submitResource(request, 42));
    }

    @Test
    void reconcile_updatesStatusAndNotes() {
        FhirImportRecordEntity record = new FhirImportRecordEntity();
        record.setId(2L);
        record.setStudyId(10);
        record.setExternalId("pat-1");
        record.setStatus(FhirImportStatus.MAPPED);
        ReconcileFhirRecordRequest request = new ReconcileFhirRecordRequest();
        request.setStatus(FhirImportStatus.RECONCILED);
        request.setReviewNotes("Accepted");
        when(recordRepository.findById(2L)).thenReturn(Optional.of(record));
        when(currentStudyAccessService.canWriteStudy(42, 10)).thenReturn(true);
        when(recordRepository.save(record)).thenReturn(record);

        var result = service.reconcile(2L, request, 42);

        assertEquals(FhirImportStatus.RECONCILED, result.getStatus());
        assertEquals("Accepted", result.getReviewNotes());
    }

    @Test
    void listConnectors_whenNoReadAccess_denies() {
        when(currentStudyAccessService.canReadStudy(42, 10)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.listConnectors(10, 42));
    }

    private static FhirConnectorEntity connector() {
        FhirConnectorEntity connector = new FhirConnectorEntity();
        connector.setId(1L);
        connector.setStudyId(10);
        connector.setName("Hospital FHIR");
        return connector;
    }
}
