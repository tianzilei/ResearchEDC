package org.researchedc.module.studybuild.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.study.dto.CreateStudyRequest;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.service.StudyService;
import org.researchedc.module.studybuild.dto.ApplyStudyTemplateRequest;
import org.researchedc.module.studybuild.dto.CreateStudyTemplateRequest;
import org.researchedc.module.studybuild.dto.StudyTemplateApplicationDTO;
import org.researchedc.module.studybuild.dto.StudyTemplateDTO;
import org.researchedc.module.studybuild.entity.StudyTemplateEntity;
import org.researchedc.module.studybuild.repository.StudyTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudyBuildService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final StudyTemplateRepository templateRepository;
    private final StudyService studyService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public StudyBuildService(StudyTemplateRepository templateRepository, StudyService studyService,
            AuditService auditService, ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.studyService = studyService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public List<StudyTemplateDTO> listTemplates() {
        return templateRepository.findByActiveTrueOrderByName()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public StudyTemplateDTO createTemplate(CreateStudyTemplateRequest request, Integer userId) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Template name is required");
        }
        StudyTemplateEntity entity = new StudyTemplateEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setProtocolType(request.getProtocolType());
        entity.setPhase(request.getPhase());
        entity.setCreatedBy(userId);
        entity.setDefaultsJson(writeDefaults(request.getDefaults()));

        StudyTemplateEntity saved = templateRepository.save(entity);
        auditService.recordAudit(null, AuditEventType.CREATE, "study_template", saved.getId(),
                saved.getName(), null, null, userId, "Study template created", "studybuild");
        return toDto(saved);
    }

    @Transactional
    public StudyTemplateApplicationDTO applyTemplate(Long templateId, ApplyStudyTemplateRequest request, Integer userId) {
        StudyTemplateEntity template = templateRepository.findById(templateId)
                .orElseThrow(() -> new NoSuchElementException("Study template not found: " + templateId));
        if (!Boolean.TRUE.equals(template.getActive())) {
            throw new IllegalArgumentException("Study template is inactive");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Study name is required");
        }
        if (request.getUniqueIdentifier() == null || request.getUniqueIdentifier().isBlank()) {
            throw new IllegalArgumentException("Study unique identifier is required");
        }

        CreateStudyRequest create = buildCreateRequest(template, request);
        StudyDetailDTO study = studyService.createStudy(create, userId);

        auditService.recordAudit(study.getStudyId(), AuditEventType.CREATE, "study_template_application",
                template.getId(), template.getName(), null, null, userId,
                "Study created from template", "studybuild");
        return new StudyTemplateApplicationDTO(toDto(template), study);
    }

    private CreateStudyRequest buildCreateRequest(StudyTemplateEntity template, ApplyStudyTemplateRequest request) {
        Map<String, Object> defaults = readDefaults(template.getDefaultsJson());
        CreateStudyRequest create = new CreateStudyRequest();
        create.setName(request.getName());
        create.setUniqueIdentifier(request.getUniqueIdentifier());
        create.setSummary(asString(defaults.get("summary")));
        create.setTypeId(asInteger(defaults.getOrDefault("typeId", 1)));
        create.setStatusId(asInteger(defaults.getOrDefault("statusId", 1)));
        create.setProtocolType(firstNonBlank(template.getProtocolType(), asString(defaults.get("protocolType"))));
        create.setProtocolDescription(asString(defaults.get("protocolDescription")));
        create.setPhase(firstNonBlank(template.getPhase(), asString(defaults.get("phase"))));
        create.setPurpose(asString(defaults.get("purpose")));
        create.setAllocation(asString(defaults.get("allocation")));
        create.setMasking(asString(defaults.get("masking")));
        create.setAssignment(asString(defaults.get("assignment")));
        create.setEndpoint(asString(defaults.get("endpoint")));
        create.setConditions(asString(defaults.get("conditions")));
        create.setKeywords(asString(defaults.get("keywords")));
        create.setEligibility(asString(defaults.get("eligibility")));
        create.setGender(asString(defaults.get("gender")));
        create.setSponsor(firstNonBlank(request.getSponsor(), asString(defaults.get("sponsor"))));
        create.setExpectedTotalEnrollment(firstNonNull(request.getExpectedTotalEnrollment(),
                asInteger(defaults.get("expectedTotalEnrollment"))));
        create.setPrincipalInvestigator(firstNonBlank(request.getPrincipalInvestigator(),
                asString(defaults.get("principalInvestigator"))));
        create.setFacilityName(firstNonBlank(request.getFacilityName(), asString(defaults.get("facilityName"))));
        return create;
    }

    private StudyTemplateDTO toDto(StudyTemplateEntity entity) {
        StudyTemplateDTO dto = new StudyTemplateDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setCategory(entity.getCategory());
        dto.setProtocolType(entity.getProtocolType());
        dto.setPhase(entity.getPhase());
        dto.setActive(Boolean.TRUE.equals(entity.getActive()));
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setDefaults(readDefaults(entity.getDefaultsJson()));
        return dto;
    }

    private String writeDefaults(Map<String, Object> defaults) {
        try {
            return objectMapper.writeValueAsString(defaults == null ? Map.of() : defaults);
        } catch (Exception e) {
            throw new IllegalArgumentException("Template defaults must be valid JSON", e);
        }
    }

    private Map<String, Object> readDefaults(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.valueOf(text);
        }
        return null;
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private static Integer firstNonNull(Integer first, Integer second) {
        return first != null ? first : second;
    }

    public static Map<String, Object> oncologyDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("typeId", 1);
        defaults.put("statusId", 1);
        defaults.put("summary", "Oncology interventional study template");
        defaults.put("purpose", "Treatment");
        defaults.put("allocation", "Randomized");
        defaults.put("masking", "Open Label");
        defaults.put("assignment", "Parallel Assignment");
        defaults.put("endpoint", "Safety/Efficacy");
        return defaults;
    }
}
