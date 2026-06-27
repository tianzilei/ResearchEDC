package org.researchedc.module.export.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.export.dto.CreateExportJobRequest;
import org.researchedc.module.export.dto.ExportJobDTO;
import org.researchedc.module.export.dto.ExportJobFilter;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.repository.ExportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final ExportJobRepository jobRepository;
    private final OdmExportExecutionService odmExecutionService;
    private final CurrentStudyAccessService currentStudyAccessService;

    public ExportService(ExportJobRepository jobRepository,
                         OdmExportExecutionService odmExecutionService,
                         CurrentStudyAccessService currentStudyAccessService) {
        this.jobRepository = jobRepository;
        this.odmExecutionService = odmExecutionService;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public ExportJobDTO createJob(CreateExportJobRequest request, Integer currentUserId) {
        requireStudyId(request.getStudyId());
        requireExportAccess(currentUserId, request.getStudyId());

        ExportJob job = new ExportJob();
        job.setStudyId(request.getStudyId());
        job.setName(request.getName());
        job.setExportFormat(request.getExportFormat());
        job.setOdmContractVersion(request.getOdmContractVersion() != null
                ? request.getOdmContractVersion() : OdmContractVersion.OC2_1);
        job.setRequestedBy(currentUserId);
        job.setCriteriaJson(request.getCriteriaJson());
        job.setStatus(ExportJobStatus.PENDING);
        job = jobRepository.save(job);

        log.info("Export job created: id={}, study={}, format={}, contract={}",
                job.getId(), job.getStudyId(), job.getExportFormat(), job.getOdmContractVersion());

        if (job.getExportFormat() == ExportFormat.ODM_XML) {
            try {
                odmExecutionService.execute(job.getId());
                job = jobRepository.findById(job.getId()).orElse(job);
            } catch (Exception e) {
                log.error("Failed to execute ODM export job {}: {}", job.getId(), e.getMessage());
                job = jobRepository.findById(job.getId()).orElse(job);
            }
        }

        return toDTO(job);
    }

    public List<ExportJobDTO> listJobs(Integer studyId, Integer currentUserId) {
        requireStudyId(studyId);
        requireExportAccess(currentUserId, studyId);
        return jobRepository.findByStudyIdOrderByRequestedDateDesc(studyId)
                .stream().map(this::toDTO).toList();
    }

    public List<ExportJobDTO> listJobs(Integer studyId, ExportJobFilter filter, Integer currentUserId) {
        requireStudyId(studyId);
        requireExportAccess(currentUserId, studyId);
        List<ExportJob> jobs = jobRepository.findByStudyIdOrderByRequestedDateDesc(studyId);
        return jobs.stream()
                .filter(j -> filter.getStatus() == null || j.getStatus() == filter.getStatus())
                .filter(j -> filter.getExportFormat() == null || j.getExportFormat() == filter.getExportFormat())
                .filter(j -> filter.getOdmContractVersion() == null || j.getOdmContractVersion() == filter.getOdmContractVersion())
                .filter(j -> filter.getRequestedBy() == null || j.getRequestedBy() != null && j.getRequestedBy().equals(filter.getRequestedBy()))
                .filter(j -> filter.getCreatedAfter() == null || j.getRequestedDate() != null && !j.getRequestedDate().isBefore(filter.getCreatedAfter()))
                .filter(j -> filter.getCreatedBefore() == null || j.getRequestedDate() != null && !j.getRequestedDate().isAfter(filter.getCreatedBefore()))
                .map(this::toDTO)
                .toList();
    }

    public ExportJobDTO getJob(Long id, Integer currentUserId) {
        ExportJob job = findJobForUser(id, currentUserId);
        return toDTO(job);
    }

    public ExportJobDTO cancelJob(Long id, Integer currentUserId) {
        ExportJob job = findJobForUser(id, currentUserId);
        if (job.getStatus() == ExportJobStatus.PENDING || job.getStatus() == ExportJobStatus.RUNNING) {
            job.setStatus(ExportJobStatus.CANCELLED);
            job = jobRepository.save(job);
            log.info("Export job cancelled: id={}", id);
        }
        return toDTO(job);
    }

    public ExportJobDTO retryJob(Long id, Integer currentUserId) {
        ExportJob job = findJobForUser(id, currentUserId);
        if (job.getStatus() == ExportJobStatus.FAILED && Boolean.TRUE.equals(job.getRetryable())) {
            job.setStatus(ExportJobStatus.PENDING);
            job.setRetryCount(job.getRetryCount() != null ? job.getRetryCount() + 1 : 1);
            job.setErrorMessage(null);
            job.setFailureCode(null);
            job = jobRepository.save(job);
            log.info("Export job queued for retry: id={}", id);
        }
        return toDTO(job);
    }

    public void markRunning(Long id) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ExportJobStatus.RUNNING);
            jobRepository.save(job);
        });
    }

    public void markCompleted(Long id, String filePath, Long fileSize) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ExportJobStatus.COMPLETED);
            job.setFilePath(filePath);
            job.setFileSize(fileSize);
            job.setCompletedDate(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Export job completed: id={}, size={}", id, fileSize);
        });
    }

    public void markFailed(Long id, String errorMessage) {
        markFailed(id, errorMessage, null, true);
    }

    public void markFailed(Long id, String errorMessage, String failureCode, boolean retryable) {
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ExportJobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setFailureCode(failureCode);
            job.setRetryable(retryable);
            job.setCompletedDate(LocalDateTime.now());
            jobRepository.save(job);
            log.error("Export job failed: id={}, error={}, code={}, retryable={}", id, errorMessage, failureCode, retryable);
        });
    }

    public record DownloadResult(Resource resource, String filename, long fileSize) {
    }

    public static class ExportArtifactUnavailableException extends RuntimeException {
        public ExportArtifactUnavailableException(String message) {
            super(message);
        }
    }

    public DownloadResult getDownload(Long id, Integer currentUserId) {
        ExportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Export job not found: " + id));
        if (!currentStudyAccessService.canExportStudy(currentUserId, job.getStudyId())) {
            throw new AccessDeniedException("You do not have export access to this study");
        }
        if (job.getStatus() != ExportJobStatus.COMPLETED) {
            throw new IllegalStateException("Export job is not completed: status=" + job.getStatus());
        }
        if (job.getFilePath() == null) {
            throw new IllegalStateException("Export job has no file path");
        }
        FileSystemResource resource = new FileSystemResource(job.getFilePath());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ExportArtifactUnavailableException(
                    "Export artifact is missing or unreadable for job " + id);
        }
        String filename = "export_" + job.getId() + ".xml";
        long fileSize = resource.getFile().length();
        return new DownloadResult(resource, filename, fileSize);
    }

    private ExportJob findJobForUser(Long id, Integer currentUserId) {
        ExportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Export job not found: " + id));
        requireExportAccess(currentUserId, job.getStudyId());
        return job;
    }

    private void requireStudyId(Integer studyId) {
        if (studyId == null) {
            throw new IllegalArgumentException("studyId is required");
        }
    }

    private void requireExportAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canExportStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have export access to this study");
        }
    }

    private ExportJobDTO toDTO(ExportJob job) {
        ExportJobDTO dto = new ExportJobDTO();
        dto.setId(job.getId());
        dto.setStudyId(job.getStudyId());
        dto.setName(job.getName());
        dto.setExportFormat(job.getExportFormat());
        dto.setOdmContractVersion(job.getOdmContractVersion());
        dto.setStatus(job.getStatus());
        dto.setRequestedBy(job.getRequestedBy());
        dto.setRequestedDate(job.getRequestedDate());
        dto.setCompletedDate(job.getCompletedDate());
        dto.setFilePath(job.getFilePath());
        dto.setFileSize(job.getFileSize());
        dto.setErrorMessage(job.getErrorMessage());
        dto.setFailureCode(job.getFailureCode());
        dto.setRetryable(job.getRetryable());
        dto.setCriteriaJson(job.getCriteriaJson());
        dto.setRetryCount(job.getRetryCount());
        return dto;
    }
}
