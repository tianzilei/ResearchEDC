package org.researchedc.module.randomization.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.randomization.algorithms.BlockRandomization;
import org.researchedc.module.randomization.algorithms.SimpleRandomization;
import org.researchedc.module.randomization.algorithms.StratifiedBlockRandomization;
import org.researchedc.module.randomization.dto.*;
import org.researchedc.module.randomization.entity.*;
import org.researchedc.module.randomization.enums.*;
import org.researchedc.module.randomization.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RandomizationService {

    private static final Logger log = LoggerFactory.getLogger(RandomizationService.class);

    private final RandomizationSchemeRepository schemeRepository;
    private final RandomizationArmRepository armRepository;
    private final RandomizationAssignmentRepository assignmentRepository;
    private final RandomizationBlockRepository blockRepository;
    private final RandomizationAuditLogRepository auditLogRepository;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final Map<RandomizationAlgorithm, RandomizationAlgorithmStrategy> strategies;

    public RandomizationService(
            RandomizationSchemeRepository schemeRepository,
            RandomizationArmRepository armRepository,
            RandomizationAssignmentRepository assignmentRepository,
            RandomizationBlockRepository blockRepository,
            RandomizationAuditLogRepository auditLogRepository,
            CurrentStudyAccessService currentStudyAccessService,
            SimpleRandomization simpleStrategy,
            BlockRandomization blockStrategy,
            StratifiedBlockRandomization stratifiedBlockStrategy) {

        this.schemeRepository = schemeRepository;
        this.armRepository = armRepository;
        this.assignmentRepository = assignmentRepository;
        this.blockRepository = blockRepository;
        this.auditLogRepository = auditLogRepository;
        this.currentStudyAccessService = currentStudyAccessService;

        this.strategies = new HashMap<>();
        this.strategies.put(RandomizationAlgorithm.SIMPLE, simpleStrategy);
        this.strategies.put(RandomizationAlgorithm.BLOCK, blockStrategy);
        this.strategies.put(RandomizationAlgorithm.STRATIFIED_BLOCK, stratifiedBlockStrategy);
    }

    // === Scheme Management ===

    public List<SchemeSummaryDTO> listSchemes(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return schemeRepository.findByStudyIdOrderByCreatedDateDesc(studyId)
                .stream().map(this::toSummary).toList();
    }

    public SchemeDTO getScheme(Long id, Integer currentUserId) {
        RandomizationScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + id));
        requireReadAccess(currentUserId, scheme.getStudyId());
        return toFullDTO(scheme);
    }

    public SchemeDTO createScheme(SchemeDTO dto, Integer userId) {
        validateSchemeDefinition(dto);
        requireWriteAccess(userId, dto.getStudyId());

        RandomizationScheme scheme = new RandomizationScheme();
        scheme.setStatus(SchemeStatus.DRAFT);
        scheme.setCreatedBy(userId);
        applySchemeDefinition(scheme, dto);

        scheme = schemeRepository.save(scheme);

        logAudit(scheme.getId(), scheme.getStudyId(), AuditAction.SCHEME_CREATED,
                "RandomizationScheme", scheme.getId(), null, scheme.getName(), userId);

        return toFullDTO(scheme);
    }

    public SchemeDTO updateScheme(Long id, SchemeDTO dto, Integer userId) {
        validateSchemeDefinition(dto);

        RandomizationScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + id));
        requireWriteAccess(userId, scheme.getStudyId());
        if (!Objects.equals(scheme.getStudyId(), dto.getStudyId())) {
            requireWriteAccess(userId, dto.getStudyId());
        }

        if (scheme.getStatus() != SchemeStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT schemes can be modified");
        }

        scheme.setUpdatedBy(userId);
        applySchemeDefinition(scheme, dto);

        scheme = schemeRepository.save(scheme);

        logAudit(scheme.getId(), scheme.getStudyId(), AuditAction.SCHEME_UPDATED,
                "RandomizationScheme", scheme.getId(), null, scheme.getName(), userId);

        return toFullDTO(scheme);
    }

    public void activateScheme(Long id, Integer userId) {
        RandomizationScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + id));
        requireWriteAccess(userId, scheme.getStudyId());
        if (scheme.getStatus() != SchemeStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT schemes can be activated");
        }
        if (scheme.getArms().isEmpty()) {
            throw new IllegalStateException("At least one arm is required before activation");
        }
        scheme.setStatus(SchemeStatus.ACTIVE);
        scheme.setUpdatedBy(userId);
        schemeRepository.save(scheme);

        logAudit(scheme.getId(), scheme.getStudyId(), AuditAction.SCHEME_ACTIVATED,
                "RandomizationScheme", scheme.getId(), "DRAFT", "ACTIVE", userId);
    }

    public void closeScheme(Long id, Integer userId) {
        RandomizationScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + id));
        requireWriteAccess(userId, scheme.getStudyId());
        SchemeStatus oldStatus = scheme.getStatus();
        if (oldStatus == SchemeStatus.CLOSED) {
            return;
        }
        scheme.setStatus(SchemeStatus.CLOSED);
        scheme.setUpdatedBy(userId);
        schemeRepository.save(scheme);

        logAudit(scheme.getId(), scheme.getStudyId(), AuditAction.SCHEME_CLOSED,
                "RandomizationScheme", scheme.getId(), oldStatus.name(), "CLOSED", userId);
    }

    // === Randomization ===

    public AssignmentDTO randomize(RandomizeRequest request, Integer userId) {
        validateRandomizeRequest(request);

        RandomizationScheme scheme = schemeRepository.findById(request.getSchemeId())
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + request.getSchemeId()));
        requireWriteAccess(userId, scheme.getStudyId());

        if (scheme.getStatus() != SchemeStatus.ACTIVE) {
            throw new IllegalStateException("Scheme is not ACTIVE (current: " + scheme.getStatus() + ")");
        }

        // Check if subject already assigned
        Optional<RandomizationAssignment> existing = assignmentRepository
                .findBySchemeIdAndStudySubjectId(scheme.getId(), request.getStudySubjectId());
        if (existing.isPresent()) {
            throw new IllegalStateException("Subject already assigned to this scheme");
        }

        List<RandomizationArm> arms = armRepository.findBySchemeIdOrderByOrderNumber(scheme.getId());
        if (arms.isEmpty()) {
            throw new IllegalStateException("No arms configured for scheme " + scheme.getId());
        }

        // Build stratum path
        String stratumPath = buildStratumPath(scheme, request);

        // Get current assignment counts per arm
        Map<Long, Long> currentCounts = buildCurrentCounts(scheme.getId(), stratumPath);

        // Execute algorithm
        RandomizationAlgorithmStrategy strategy = strategies.get(scheme.getAlgorithm());
        if (strategy == null) {
            throw new IllegalStateException("No strategy found for algorithm: " + scheme.getAlgorithm());
        }

        RandomizationArm selectedArm = strategy.assign(scheme, arms, stratumPath, currentCounts);

        // Create assignment
        RandomizationAssignment assignment = new RandomizationAssignment();
        assignment.setScheme(scheme);
        assignment.setStudySubjectId(request.getStudySubjectId());
        assignment.setArm(selectedArm);
        assignment.setStratumPath(stratumPath);
        assignment.setStatus(AssignmentStatus.ACTIVE);
        assignment.setAssignedBy(userId);
        assignment = assignmentRepository.save(assignment);

        logAudit(scheme.getId(), scheme.getStudyId(), AuditAction.SUBJECT_ASSIGNED,
                "RandomizationAssignment", assignment.getId(),
                null, "arm=" + selectedArm.getName() + " subject=" + request.getStudySubjectId(),
                userId);

        log.info("Assigned subject {} to arm {} in scheme {}",
                request.getStudySubjectId(), selectedArm.getName(), scheme.getId());

        return toAssignmentDTO(assignment, selectedArm.getName(), null);
    }

    public AssignmentDTO getAssignment(Long schemeId, Integer studySubjectId, Integer currentUserId) {
        RandomizationAssignment assignment = assignmentRepository
                .findBySchemeIdAndStudySubjectId(schemeId, studySubjectId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No assignment found for scheme " + schemeId + " subject " + studySubjectId));
        requireReadAccess(currentUserId, assignment.getScheme().getStudyId());

        return toAssignmentDTO(assignment, assignment.getArm().getName(), null);
    }

    public List<AssignmentDTO> listAssignments(Long schemeId, Integer currentUserId) {
        RandomizationScheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + schemeId));
        requireReadAccess(currentUserId, scheme.getStudyId());
        return assignmentRepository.findBySchemeIdAndStatusOrderByAssignedDateDesc(
                        schemeId, AssignmentStatus.ACTIVE)
                .stream().map(a -> toAssignmentDTO(a, a.getArm().getName(), null))
                .toList();
    }

    // === Audit ===

    public List<AuditLogDTO> getAuditLogs(Long schemeId, Integer currentUserId) {
        RandomizationScheme scheme = schemeRepository.findById(schemeId)
                .orElseThrow(() -> new NoSuchElementException("Scheme not found: " + schemeId));
        requireReadAccess(currentUserId, scheme.getStudyId());
        return auditLogRepository.findBySchemeIdOrderByPerformedDateDesc(schemeId)
                .stream().map(this::toAuditDTO).toList();
    }

    public List<AuditLogDTO> getStudyAuditLogs(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return auditLogRepository.findByStudyIdOrderByPerformedDateDesc(studyId)
                .stream().map(this::toAuditDTO).toList();
    }

    // === Internal Helpers ===

    private String buildStratumPath(RandomizationScheme scheme, RandomizeRequest request) {
        if (scheme.getStratifications().isEmpty()
                || scheme.getAlgorithm() == RandomizationAlgorithm.SIMPLE
                || scheme.getAlgorithm() == RandomizationAlgorithm.BLOCK) {
            return "";
        }
        if (request.getStratumValues() == null || request.getStratumValues().isEmpty()) {
            throw new IllegalArgumentException("Stratum values are required for " + scheme.getAlgorithm());
        }
        return scheme.getStratifications().stream()
                .map(s -> s.getName() + "=" + requireStratumValue(request, s.getName()))
                .collect(Collectors.joining("|"));
    }

    private Map<Long, Long> buildCurrentCounts(Long schemeId, String stratumPath) {
        List<RandomizationArm> arms = armRepository.findBySchemeIdOrderByOrderNumber(schemeId);
        Map<Long, Long> counts = new HashMap<>();
        for (RandomizationArm arm : arms) {
            long count = stratumPath == null || stratumPath.isBlank()
                    ? assignmentRepository.countBySchemeIdAndArmIdAndStatus(schemeId, arm.getId(), AssignmentStatus.ACTIVE)
                    : assignmentRepository.countBySchemeIdAndArmIdAndStratumPathAndStatus(
                            schemeId, arm.getId(), stratumPath, AssignmentStatus.ACTIVE);
            counts.put(arm.getId(), count);
        }
        return counts;
    }

    private void applySchemeDefinition(RandomizationScheme scheme, SchemeDTO dto) {
        scheme.setStudyId(dto.getStudyId());
        scheme.setName(dto.getName());
        scheme.setAlgorithm(dto.getAlgorithm());
        scheme.setMinBlockSize(dto.getMinBlockSize());
        scheme.setMaxBlockSize(dto.getMaxBlockSize());

        scheme.getArms().clear();
        if (dto.getArms() != null) {
            for (ArmDTO armDTO : dto.getArms()) {
                scheme.getArms().add(toArmEntity(armDTO, scheme));
            }
        }

        scheme.getStratifications().clear();
        if (dto.getStratifications() != null) {
            for (StratumDTO sDTO : dto.getStratifications()) {
                scheme.getStratifications().add(toStratumEntity(sDTO, scheme));
            }
        }
    }

    private RandomizationArm toArmEntity(ArmDTO armDTO, RandomizationScheme scheme) {
        RandomizationArm arm = new RandomizationArm();
        arm.setName(armDTO.getName());
        arm.setDisplayName(armDTO.getDisplayName());
        arm.setRatio(armDTO.getRatio() != null ? armDTO.getRatio() : 1);
        arm.setOrderNumber(armDTO.getOrderNumber());
        arm.setScheme(scheme);
        return arm;
    }

    private RandomizationStratum toStratumEntity(StratumDTO sDTO, RandomizationScheme scheme) {
        RandomizationStratum stratum = new RandomizationStratum();
        stratum.setName(sDTO.getName());
        stratum.setStratumType(sDTO.getStratumType());
        stratum.setOrderNumber(sDTO.getOrderNumber());
        stratum.setScheme(scheme);
        if (sDTO.getOptions() != null) {
            for (StratumOptionDTO oDTO : sDTO.getOptions()) {
                RandomizationStratumOption option = new RandomizationStratumOption();
                option.setLabel(oDTO.getLabel());
                option.setValue(oDTO.getValue());
                option.setOrderNumber(oDTO.getOrderNumber());
                option.setStratum(stratum);
                stratum.getOptions().add(option);
            }
        }
        return stratum;
    }

    private void validateSchemeDefinition(SchemeDTO dto) {
        Objects.requireNonNull(dto, "Scheme is required");
        if (dto.getStudyId() == null) {
            throw new IllegalArgumentException("studyId is required");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (dto.getAlgorithm() == null) {
            throw new IllegalArgumentException("algorithm is required");
        }
        if (dto.getArms() != null) {
            for (ArmDTO arm : dto.getArms()) {
                if (arm.getName() == null || arm.getName().isBlank()) {
                    throw new IllegalArgumentException("arm name is required");
                }
                if (arm.getRatio() != null && arm.getRatio() <= 0) {
                    throw new IllegalArgumentException("arm ratio must be positive");
                }
            }
        }
    }

    private void validateRandomizeRequest(RandomizeRequest request) {
        Objects.requireNonNull(request, "Randomize request is required");
        if (request.getSchemeId() == null) {
            throw new IllegalArgumentException("schemeId is required");
        }
        if (request.getStudySubjectId() == null) {
            throw new IllegalArgumentException("studySubjectId is required");
        }
    }

    private String requireStratumValue(RandomizeRequest request, String name) {
        String value = request.getStratumValues().get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing stratum value for " + name);
        }
        return value;
    }

    private void logAudit(Long schemeId, Integer studyId, AuditAction action,
                          String entityType, Long entityId, String oldValue, String newValue, Integer userId) {
        RandomizationAuditLog audit = new RandomizationAuditLog();
        audit.setSchemeId(schemeId);
        audit.setStudyId(studyId);
        audit.setAction(action);
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setPerformedBy(userId);
        audit.setDetails(action.name() + " | " + (newValue != null ? newValue : ""));
        auditLogRepository.save(audit);
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

    private SchemeSummaryDTO toSummary(RandomizationScheme s) {
        SchemeSummaryDTO dto = new SchemeSummaryDTO();
        dto.setId(s.getId());
        dto.setStudyId(s.getStudyId());
        dto.setName(s.getName());
        dto.setAlgorithm(s.getAlgorithm());
        dto.setStatus(s.getStatus());
        dto.setTotalArms(s.getArms().size());
        dto.setTotalAssigned(assignmentRepository
                .countBySchemeIdAndStatus(s.getId(), AssignmentStatus.ACTIVE));
        return dto;
    }

    private SchemeDTO toFullDTO(RandomizationScheme s) {
        SchemeDTO dto = new SchemeDTO();
        dto.setId(s.getId());
        dto.setStudyId(s.getStudyId());
        dto.setName(s.getName());
        dto.setAlgorithm(s.getAlgorithm());
        dto.setStatus(s.getStatus());
        dto.setSeed(s.getSeed());
        dto.setMinBlockSize(s.getMinBlockSize());
        dto.setMaxBlockSize(s.getMaxBlockSize());
        dto.setTotalAssigned(assignmentRepository
                .countBySchemeIdAndStatus(s.getId(), AssignmentStatus.ACTIVE));
        dto.setTotalArms(s.getArms().size());

        dto.setArms(s.getArms().stream().map(a -> {
            ArmDTO ad = new ArmDTO();
            ad.setId(a.getId());
            ad.setName(a.getName());
            ad.setDisplayName(a.getDisplayName());
            ad.setRatio(a.getRatio());
            ad.setOrderNumber(a.getOrderNumber());
            return ad;
        }).toList());

        dto.setStratifications(s.getStratifications().stream().map(st -> {
            StratumDTO sd = new StratumDTO();
            sd.setId(st.getId());
            sd.setName(st.getName());
            sd.setStratumType(st.getStratumType());
            sd.setOrderNumber(st.getOrderNumber());
            sd.setOptions(st.getOptions().stream().map(o -> {
                StratumOptionDTO od = new StratumOptionDTO();
                od.setId(o.getId());
                od.setLabel(o.getLabel());
                od.setValue(o.getValue());
                od.setOrderNumber(o.getOrderNumber());
                return od;
            }).toList());
            return sd;
        }).toList());

        return dto;
    }

    private AssignmentDTO toAssignmentDTO(RandomizationAssignment a, String armName, String subjectKey) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(a.getId());
        dto.setSchemeId(a.getScheme().getId());
        dto.setStudySubjectId(a.getStudySubjectId());
        dto.setSubjectKey(subjectKey);
        dto.setArmId(a.getArm().getId());
        dto.setArmName(armName);
        dto.setStratumPath(a.getStratumPath());
        dto.setStatus(a.getStatus());
        dto.setAssignedBy(a.getAssignedBy());
        return dto;
    }

    private AuditLogDTO toAuditDTO(RandomizationAuditLog a) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(a.getId());
        dto.setSchemeId(a.getSchemeId());
        dto.setStudyId(a.getStudyId());
        dto.setAction(a.getAction());
        dto.setEntityType(a.getEntityType());
        dto.setEntityId(a.getEntityId());
        dto.setOldValue(a.getOldValue());
        dto.setNewValue(a.getNewValue());
        dto.setPerformedBy(a.getPerformedBy());
        dto.setPerformedDate(a.getPerformedDate());
        dto.setDetails(a.getDetails());
        return dto;
    }
}
