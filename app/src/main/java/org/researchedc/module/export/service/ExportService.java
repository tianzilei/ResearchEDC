package org.researchedc.module.export.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.researchedc.module.export.dto.CreateExportJobRequest;
import org.researchedc.module.export.dto.ExportJobDTO;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.repository.ExportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final ExportJobRepository jobRepository;

    public ExportService(ExportJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public ExportJobDTO createJob(CreateExportJobRequest request) {
        ExportJob job = new ExportJob();
        job.setStudyId(request.getStudyId());
        job.setName(request.getName());
        job.setExportFormat(request.getExportFormat());
        job.setOdmContractVersion(request.getOdmContractVersion() != null
                ? request.getOdmContractVersion() : OdmContractVersion.OC2_1);
        job.setRequestedBy(request.getRequestedBy());
        job.setCriteriaJson(request.getCriteriaJson());
        job.setStatus(ExportJobStatus.PENDING);
        job = jobRepository.save(job);

        log.info("Export job created: id={}, study={}, format={}, contract={}",
                job.getId(), job.getStudyId(), job.getExportFormat(), job.getOdmContractVersion());

        return toDTO(job);
    }

    public List<ExportJobDTO> listJobs(Integer studyId) {
        return jobRepository.findByStudyIdOrderByRequestedDateDesc(studyId)
                .stream().map(this::toDTO).toList();
    }

    public ExportJobDTO getJob(Long id) {
        ExportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Export job not found: " + id));
        return toDTO(job);
    }

    public ExportJobDTO cancelJob(Long id) {
        ExportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Export job not found: " + id));
        if (job.getStatus() == ExportJobStatus.PENDING || job.getStatus() == ExportJobStatus.RUNNING) {
            job.setStatus(ExportJobStatus.CANCELLED);
            job = jobRepository.save(job);
            log.info("Export job cancelled: id={}", id);
        }
        return toDTO(job);
    }

    public ExportJobDTO retryJob(Long id) {
        ExportJob job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Export job not found: " + id));
        if (job.getStatus() == ExportJobStatus.FAILED) {
            job.setStatus(ExportJobStatus.PENDING);
            job.setRetryCount(job.getRetryCount() != null ? job.getRetryCount() + 1 : 1);
            job.setErrorMessage(null);
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
        jobRepository.findById(id).ifPresent(job -> {
            job.setStatus(ExportJobStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setCompletedDate(LocalDateTime.now());
            jobRepository.save(job);
            log.error("Export job failed: id={}, error={}", id, errorMessage);
        });
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
        dto.setCriteriaJson(job.getCriteriaJson());
        dto.setRetryCount(job.getRetryCount());
        return dto;
    }
}
