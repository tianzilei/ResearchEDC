package org.researchedc.module.dataimport.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.researchedc.module.dataimport.dto.CreateImportJobRequest;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.entity.ImportJob;
import org.researchedc.module.dataimport.enums.ImportJobStatus;
import org.researchedc.module.dataimport.enums.ImportType;
import org.researchedc.module.dataimport.internal.adapter.ImportCrfDataAdapter;
import org.researchedc.module.dataimport.repository.ImportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final ImportJobRepository jobRepository;
    private final ImportCrfDataAdapter importAdapter;

    public ImportService(ImportJobRepository jobRepository, ImportCrfDataAdapter importAdapter) {
        this.jobRepository = jobRepository;
        this.importAdapter = importAdapter;
    }

    public ImportJobDTO createJob(CreateImportJobRequest request) {
        ImportJob job = new ImportJob();
        job.setStudyId(request.getStudyId());
        job.setName(request.getName());
        job.setImportType(request.getImportType());
        job.setFileName(request.getFileName());
        job.setStoredFilePath(request.getStoredFilePath());
        job.setRequestedBy(request.getRequestedBy());
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

    public List<ImportJobDTO> listJobs(Integer studyId) {
        return jobRepository.findByStudyIdOrderByRequestedDateDesc(studyId)
                .stream().map(this::toDTO).toList();
    }

    public ImportJobDTO getJob(Long id) {
        ImportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Import job not found: " + id));
        return toDTO(job);
    }

    public void markValidating(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ImportJobStatus.VALIDATING);
            jobRepository.save(job);
        });
    }

    public void markValidated(Long id, String summaryJson) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ImportJobStatus.VALIDATED);
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

    public ImportJobDTO validate(Long id) {
        ImportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Import job not found: " + id));
        markValidating(id);
        try {
            markValidated(id, "{\"status\":\"validated\"}");
            return jobRepository.findById(id).map(this::toDTO)
                    .orElseThrow();
        } catch (Exception e) {
            markFailed(id, "Validation failed: " + e.getMessage());
            throw e;
        }
    }

    public ImportJobDTO commit(Long id) {
        ImportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Import job not found: " + id));
        markCommitting(id);
        try {
            markCompleted(id);
            return jobRepository.findById(id).map(this::toDTO)
                    .orElseThrow();
        } catch (Exception e) {
            markFailed(id, "Commit failed: " + e.getMessage());
            throw e;
        }
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
