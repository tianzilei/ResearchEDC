package org.researchedc.module.fhir.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.fhir.dto.CreateFhirConnectorRequest;
import org.researchedc.module.fhir.dto.FhirConnectorDTO;
import org.researchedc.module.fhir.dto.FhirImportRecordDTO;
import org.researchedc.module.fhir.dto.ReconcileFhirRecordRequest;
import org.researchedc.module.fhir.dto.SubmitFhirResourceRequest;
import org.researchedc.module.fhir.entity.FhirConnectorEntity;
import org.researchedc.module.fhir.entity.FhirImportRecordEntity;
import org.researchedc.module.fhir.enums.FhirImportStatus;
import org.researchedc.module.fhir.repository.FhirConnectorRepository;
import org.researchedc.module.fhir.repository.FhirImportRecordRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class FhirService {

    private final FhirConnectorRepository connectorRepository;
    private final FhirImportRecordRepository recordRepository;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public FhirService(FhirConnectorRepository connectorRepository,
                       FhirImportRecordRepository recordRepository,
                       CurrentStudyAccessService currentStudyAccessService,
                       AuditService auditService,
                       ObjectMapper objectMapper) {
        this.connectorRepository = connectorRepository;
        this.recordRepository = recordRepository;
        this.currentStudyAccessService = currentStudyAccessService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public List<FhirConnectorDTO> listConnectors(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return connectorRepository.findByStudyIdOrderByCreatedDateDesc(studyId).stream().map(this::toConnectorDto).toList();
    }

    @Transactional
    public FhirConnectorDTO createConnector(CreateFhirConnectorRequest request, Integer currentUserId) {
        if (request.getStudyId() == null) throw new IllegalArgumentException("studyId is required");
        if (!StringUtils.hasText(request.getName())) throw new IllegalArgumentException("name is required");
        requireWriteAccess(currentUserId, request.getStudyId());
        FhirConnectorEntity connector = new FhirConnectorEntity();
        connector.setStudyId(request.getStudyId());
        connector.setName(request.getName().trim());
        connector.setBaseUrl(request.getBaseUrl() == null ? "" : request.getBaseUrl().trim());
        connector.setCreatedBy(currentUserId);
        FhirConnectorEntity saved = connectorRepository.save(connector);
        record(saved.getStudyId(), AuditEventType.CREATE, "fhir_connector", saved.getId(),
                saved.getName(), currentUserId, "FHIR connector configured");
        return toConnectorDto(saved);
    }

    public List<FhirImportRecordDTO> listRecords(Integer studyId, FhirImportStatus status, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        List<FhirImportRecordEntity> records = status == null
                ? recordRepository.findByStudyIdOrderByCreatedDateDesc(studyId)
                : recordRepository.findByStudyIdAndStatusOrderByCreatedDateDesc(studyId, status);
        return records.stream().map(this::toRecordDto).toList();
    }

    @Transactional
    public FhirImportRecordDTO submitResource(SubmitFhirResourceRequest request, Integer currentUserId) {
        if (request.getConnectorId() == null) throw new IllegalArgumentException("connectorId is required");
        if (!StringUtils.hasText(request.getPayloadJson())) throw new IllegalArgumentException("payloadJson is required");
        FhirConnectorEntity connector = connectorRepository.findById(request.getConnectorId())
                .orElseThrow(() -> new NoSuchElementException("FHIR connector not found: " + request.getConnectorId()));
        requireWriteAccess(currentUserId, connector.getStudyId());
        FhirImportRecordEntity entity = mapResource(connector, request.getPayloadJson());
        entity.setCreatedBy(currentUserId);
        FhirImportRecordEntity saved = recordRepository.save(entity);
        record(saved.getStudyId(), AuditEventType.CREATE, "fhir_import_record", saved.getId(),
                saved.getExternalId(), currentUserId, "FHIR resource received");
        return toRecordDto(saved);
    }

    @Transactional
    public FhirImportRecordDTO reconcile(Long recordId, ReconcileFhirRecordRequest request, Integer currentUserId) {
        FhirImportRecordEntity record = recordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("FHIR import record not found: " + recordId));
        requireWriteAccess(currentUserId, record.getStudyId());
        if (request.getStatus() == null || request.getStatus() == FhirImportStatus.RECEIVED) {
            throw new IllegalArgumentException("status must be MAPPED, RECONCILED, REJECTED, or FAILED");
        }
        record.setStatus(request.getStatus());
        record.setReviewNotes(request.getReviewNotes() == null ? "" : request.getReviewNotes());
        record.setUpdatedDate(LocalDateTime.now());
        FhirImportRecordEntity saved = recordRepository.save(record);
        record(saved.getStudyId(), AuditEventType.UPDATE, "fhir_import_record", saved.getId(),
                saved.getExternalId(), currentUserId, "FHIR record reconciled: " + saved.getStatus());
        return toRecordDto(saved);
    }

    private FhirImportRecordEntity mapResource(FhirConnectorEntity connector, String payloadJson) {
        try {
            JsonNode root = objectMapper.readTree(payloadJson);
            String resourceType = text(root, "resourceType");
            if (!"Patient".equals(resourceType)) {
                throw new IllegalArgumentException("Only FHIR Patient resources are supported in this slice");
            }
            FhirImportRecordEntity entity = new FhirImportRecordEntity();
            entity.setConnectorId(connector.getId());
            entity.setStudyId(connector.getStudyId());
            entity.setResourceType(resourceType);
            entity.setExternalId(text(root, "id"));
            entity.setMappedSubjectIdentifier(resolveSubjectIdentifier(root));
            entity.setMappedGender(text(root, "gender"));
            entity.setPayloadJson(payloadJson);
            entity.setStatus(FhirImportStatus.MAPPED);
            return entity;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid FHIR JSON payload", e);
        }
    }

    private String resolveSubjectIdentifier(JsonNode root) {
        JsonNode identifier = root.path("identifier");
        if (identifier.isArray() && !identifier.isEmpty()) {
            String value = text(identifier.get(0), "value");
            if (StringUtils.hasText(value)) return value;
        }
        String id = text(root, "id");
        return StringUtils.hasText(id) ? id : "FHIR-PATIENT";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : "";
    }

    private FhirConnectorDTO toConnectorDto(FhirConnectorEntity entity) {
        FhirConnectorDTO dto = new FhirConnectorDTO();
        dto.setId(entity.getId());
        dto.setStudyId(entity.getStudyId());
        dto.setName(entity.getName());
        dto.setBaseUrl(entity.getBaseUrl());
        dto.setActive(entity.getActive());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        return dto;
    }

    private FhirImportRecordDTO toRecordDto(FhirImportRecordEntity entity) {
        FhirImportRecordDTO dto = new FhirImportRecordDTO();
        dto.setId(entity.getId());
        dto.setConnectorId(entity.getConnectorId());
        dto.setStudyId(entity.getStudyId());
        dto.setResourceType(entity.getResourceType());
        dto.setExternalId(entity.getExternalId());
        dto.setMappedSubjectIdentifier(entity.getMappedSubjectIdentifier());
        dto.setMappedGender(entity.getMappedGender());
        dto.setStatus(entity.getStatus());
        dto.setReviewNotes(entity.getReviewNotes());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        return dto;
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

    private void record(Integer studyId, AuditEventType eventType, String entityType, Long entityId,
                        String entityLabel, Integer performedBy, String details) {
        auditService.recordAudit(studyId, eventType, entityType, entityId, entityLabel,
                null, null, performedBy, details, "fhir");
    }
}
