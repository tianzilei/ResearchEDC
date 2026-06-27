package org.researchedc.module.dataimport.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.dataimport.dto.CreateImportJobRequest;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.dto.ImportPreviewDTO;
import org.researchedc.module.dataimport.dto.ImportResultDTO;
import org.researchedc.module.dataimport.entity.ImportJob;
import org.researchedc.module.dataimport.enums.ImportJobStatus;
import org.researchedc.module.dataimport.enums.ImportType;
import org.researchedc.module.dataimport.internal.adapter.ImportCrfDataAdapter;
import org.researchedc.module.dataimport.internal.adapter.ImportCrfDataAdapter.CommitResult;
import org.researchedc.module.dataimport.internal.adapter.ImportCrfDataAdapter.EventCrfValidationResult;
import org.researchedc.module.dataimport.internal.adapter.ImportCrfDataAdapter.ParsedOdm;
import org.researchedc.module.dataimport.event.ImportCommittedEvent;
import org.researchedc.module.dataimport.repository.ImportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ImportJobRepository jobRepository;
    private final ImportCrfDataAdapter importAdapter;
    private final CurrentStudyAccessService currentStudyAccessService;
    private final ApplicationEventPublisher eventPublisher;

    public ImportService(ImportJobRepository jobRepository, ImportCrfDataAdapter importAdapter) {
        this(jobRepository, importAdapter, null, null);
    }

    public ImportService(ImportJobRepository jobRepository, ImportCrfDataAdapter importAdapter,
                         CurrentStudyAccessService currentStudyAccessService) {
        this(jobRepository, importAdapter, currentStudyAccessService, null);
    }

    @Autowired
    public ImportService(ImportJobRepository jobRepository, ImportCrfDataAdapter importAdapter,
                         CurrentStudyAccessService currentStudyAccessService,
                         ApplicationEventPublisher eventPublisher) {
        this.jobRepository = jobRepository;
        this.importAdapter = importAdapter;
        this.currentStudyAccessService = currentStudyAccessService;
        this.eventPublisher = eventPublisher;
    }

    public ImportJobDTO createJob(CreateImportJobRequest request, Integer currentUserId) {
        requireStudyId(request.getStudyId());
        requireImportAccess(currentUserId, request.getStudyId());

        ImportJob job = new ImportJob();
        job.setStudyId(request.getStudyId());
        job.setName(request.getName());
        job.setImportType(request.getImportType());
        job.setFileName(request.getFileName());
        job.setStoredFilePath(request.getStoredFilePath());
        job.setRequestedBy(currentUserId);
        job.setStatus(ImportJobStatus.STAGED);
        job = jobRepository.save(job);

        log.info("Import job created: id={}, study={}, type={}, file={}",
                job.getId(), job.getStudyId(), job.getImportType(), job.getFileName());

        return toDTO(job);
    }

    private static final Path UPLOAD_DIR =
            Path.of(System.getProperty("user.home"), "ResearchEDC", "data", "imports");

    /**
     * Upload a file and create an import job in a single atomic step.
     * This is the canonical endpoint for the SPA import wizard — replaces the
     * separate upload-then-create pattern that caused duplicate jobs.
     */
    public ImportJobDTO uploadFile(MultipartFile file, String importTypeStr,
                                   Integer studyId, String name, Integer userId) {
        requireStudyId(studyId);
        requireImportAccess(userId, studyId);

        try {
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + UPLOAD_DIR, e);
        }

        String storedName = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();
        Path target = UPLOAD_DIR.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store import file: " + originalName, e);
        }

        ImportType importType = "CRF_DEFINITION".equalsIgnoreCase(importTypeStr)
                ? ImportType.CRF_DEFINITION : ImportType.CRF_DATA;

        ImportJob job = new ImportJob();
        job.setStudyId(studyId);
        job.setName(name != null ? name : (originalName != null ? originalName : "Import"));
        job.setImportType(importType);
        job.setFileName(originalName);
        job.setStoredFilePath(target.toString());
        job.setRequestedBy(userId);
        job.setStatus(ImportJobStatus.STAGED);
        job = jobRepository.save(job);

        log.info("Import file uploaded and job created: id={}, study={}, type={}, file={} ({} bytes)",
                job.getId(), studyId, importType, originalName, file.getSize());

        return toDTO(job);
    }

    public List<ImportJobDTO> listJobs(Integer studyId, Integer currentUserId) {
        requireStudyId(studyId);
        requireImportAccess(currentUserId, studyId);
        return jobRepository.findByStudyIdOrderByRequestedDateDesc(studyId)
                .stream().map(this::toDTO).toList();
    }

    public ImportJobDTO getJob(Long id, Integer currentUserId) {
        ImportJob job = findJobForUser(id, currentUserId);
        return toDTO(job);
    }

    public void markValidating(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ImportJobStatus.VALIDATING);
            jobRepository.save(job);
        });
    }

    public void markValidated(Long id, String summaryJson) {
        markPreviewResult(id, ImportJobStatus.VALIDATED, summaryJson);
    }

    private void markPreviewResult(Long id, ImportJobStatus status, String summaryJson) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(status);
            job.setSummaryJson(summaryJson);
            jobRepository.save(job);
        });
    }

    public void markCommitting(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ImportJobStatus.COMMITTING);
            jobRepository.save(job);
        });
    }

    public void markCompleted(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ImportJobStatus.COMPLETED);
            job.setCompletedDate(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Import job completed: id={}", id);
        });
    }

    public void markFailed(Long id, String errorMessage) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ImportJobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setCompletedDate(LocalDateTime.now());
            jobRepository.save(job);
            log.error("Import job failed: id={}, error={}", id, errorMessage);
        });
    }

    public ImportPreviewDTO validate(Long id, Integer currentUserId) {
        ImportJob job = findJobForUser(id, currentUserId);
        markValidating(id);
        try {
            Path filePath = Path.of(job.getStoredFilePath());
            ParsedOdm odm = importAdapter.parseOdm(filePath);
            List<String> errors = importAdapter.validateMetadata(odm, job.getStudyId(), Locale.ENGLISH);
            if (!errors.isEmpty()) {
                ImportPreviewDTO preview = ImportPreviewDTO.invalid(errors);
                markPreviewResult(id, ImportJobStatus.INVALID, serializePreview(preview));
                return preview;
            }
            EventCrfValidationResult eventCrfs = importAdapter.validateEventCrfs(odm, job.getStudyId(), Locale.ENGLISH);
            ImportPreviewDTO preview;
            ImportJobStatus resultStatus;
            if (!eventCrfs.statusesValid() || eventCrfs.eventCrfCount() < 0) {
                preview = ImportPreviewDTO.blocked("event_crf_status");
                resultStatus = ImportJobStatus.BLOCKED;
            } else if (eventCrfs.eventCrfCount() == 0) {
                preview = ImportPreviewDTO.invalid(List.of("No event CRFs were found in the import file."));
                resultStatus = ImportJobStatus.INVALID;
            } else {
                EditCheckSummary editChecks = parseEditCheckSummary(
                        importAdapter.validateEditChecks(odm, job.getStudyId()));
                if (editChecks.errors() > 0) {
                    preview = ImportPreviewDTO.invalid(List.of(
                            editChecks.errors() + " edit-check error(s) were found."));
                    preview.setEventCrfs(eventCrfs.eventCrfCount());
                    preview.setTotalItems(editChecks.totalItems());
                    preview.setEditCheckErrors(editChecks.errors());
                    resultStatus = ImportJobStatus.INVALID;
                } else {
                    preview = ImportPreviewDTO.valid(
                            eventCrfs.eventCrfCount(),
                            editChecks.totalItems(),
                            editChecks.errors());
                    resultStatus = ImportJobStatus.VALIDATED;
                }
            }
            markPreviewResult(id, resultStatus, serializePreview(preview));
            return preview;
        } catch (Exception e) {
            markFailed(id, "Validation failed: " + e.getMessage());
            throw e;
        }
    }

    public ImportPreviewDTO getPreview(Long id, Integer currentUserId) {
        ImportJob job = findJobForUser(id, currentUserId);
        if (job.getSummaryJson() == null || job.getSummaryJson().isBlank()) {
            return ImportPreviewDTO.failed("No validation preview is available for this import job.");
        }
        return parsePreview(job.getSummaryJson());
    }

    public ImportResultDTO commit(Long id, Integer currentUserId) {
        ImportJob job = findJobForUser(id, currentUserId);
        assertCommitEligible(job);
        markCommitting(id);
        try {
            Path filePath = Path.of(job.getStoredFilePath());
            ParsedOdm odm = importAdapter.parseOdm(filePath);
            CommitResult commitResult = importAdapter.commitImport(odm, job.getStudyId(), Locale.ENGLISH);
            ImportResultDTO result = ImportResultDTO.committed(
                    commitResult.eventCrfCount(), commitResult.itemCount());
            job.setSummaryJson(serializeResult(result));
            jobRepository.save(job);
            publishCommitEvent(job, result);
            markCompleted(id);
            log.info("Import commit complete: id={}, study={}, eventCrfs={}, items={}",
                    id, job.getStudyId(), commitResult.eventCrfCount(), commitResult.itemCount());
            return result;
        } catch (Exception e) {
            markFailed(id, "Commit failed: " + e.getMessage());
            throw e;
        }
    }

    private void assertCommitEligible(ImportJob job) {
        if (job.getStatus() != ImportJobStatus.VALIDATED) {
            throw new IllegalStateException(
                    "Import job must be VALIDATED before commit; current status is " + job.getStatus());
        }
        ImportPreviewDTO preview = job.getSummaryJson() == null || job.getSummaryJson().isBlank()
                ? null
                : parsePreview(job.getSummaryJson());
        if (preview == null || !"validated".equals(preview.getStatus())
                || !preview.getErrors().isEmpty() || preview.getEditCheckErrors() > 0) {
            throw new IllegalStateException("Import job validation preview is not committable");
        }
    }

    private ImportJob findJobForUser(Long id, Integer currentUserId) {
        ImportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Import job not found: " + id));
        requireImportAccess(currentUserId, job.getStudyId());
        return job;
    }

    private void requireStudyId(Integer studyId) {
        if (studyId == null) {
            throw new IllegalArgumentException("studyId is required");
        }
    }

    private void requireImportAccess(Integer currentUserId, Integer studyId) {
        if (currentStudyAccessService == null) {
            return;
        }
        if (!currentStudyAccessService.canImportStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have import access to this study");
        }
    }

    private String serializeResult(ImportResultDTO result) {
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize import result", e);
        }
    }

    private void publishCommitEvent(ImportJob job, ImportResultDTO result) {
        if (eventPublisher == null) {
            return;
        }
        eventPublisher.publishEvent(new ImportCommittedEvent(
                job.getId(),
                job.getStudyId(),
                job.getName(),
                job.getRequestedBy(),
                result.getEventCrfs(),
                result.getItems()));
    }

    private String serializePreview(ImportPreviewDTO preview) {
        try {
            return OBJECT_MAPPER.writeValueAsString(preview);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize import preview", e);
        }
    }

    private ImportPreviewDTO parsePreview(String summaryJson) {
        try {
            return OBJECT_MAPPER.readValue(summaryJson, ImportPreviewDTO.class);
        } catch (Exception e) {
            log.warn("Failed to parse import preview summary JSON: {}", e.getMessage());
            return ImportPreviewDTO.failed("Stored import preview could not be parsed.");
        }
    }

    private EditCheckSummary parseEditCheckSummary(String editChecksJson) {
        if (editChecksJson == null || editChecksJson.isBlank()) {
            return new EditCheckSummary(0, 0);
        }
        try {
            Map<String, Object> values = OBJECT_MAPPER.readValue(
                    editChecksJson, new TypeReference<Map<String, Object>>() {});
            return new EditCheckSummary(asInt(values.get("total")), asInt(values.get("errors")));
        } catch (Exception e) {
            log.warn("Failed to parse edit-check summary JSON: {}", e.getMessage());
            return new EditCheckSummary(0, 0);
        }
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private record EditCheckSummary(int totalItems, int errors) {
    }

    private ImportJobDTO toDTO(ImportJob job) {
        ImportJobDTO dto = new ImportJobDTO();
        dto.setId(job.getId());
        dto.setStudyId(job.getStudyId());
        dto.setName(job.getName());
        dto.setImportType(job.getImportType());
        dto.setFileName(job.getFileName());
        dto.setStoredFilePath(job.getStoredFilePath());
        dto.setStatus(job.getStatus());
        dto.setRequestedBy(job.getRequestedBy());
        dto.setRequestedDate(job.getRequestedDate());
        dto.setCompletedDate(job.getCompletedDate());
        dto.setErrorMessage(job.getErrorMessage());
        dto.setSummaryJson(job.getSummaryJson());
        dto.setRetryCount(job.getRetryCount());
        return dto;
    }
}
