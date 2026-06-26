package org.researchedc.module.export.service;

import java.util.UUID;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.internal.ExportDataProvider;
import org.researchedc.module.export.internal.OdmSubjectDataSnapshot;
import org.researchedc.module.export.repository.ExportJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OdmExportExecutionService {

    private static final Logger log = LoggerFactory.getLogger(OdmExportExecutionService.class);

    private final ExportJobRepository jobRepository;
    private final ExportDataProvider dataProvider;
    private final OdmExportGenerator odmGenerator;
    private final ExportArtifactWriter artifactWriter;

    public OdmExportExecutionService(ExportJobRepository jobRepository,
                                      ExportDataProvider dataProvider,
                                      OdmExportGenerator odmGenerator,
                                      ExportArtifactWriter artifactWriter) {
        this.jobRepository = jobRepository;
        this.dataProvider = dataProvider;
        this.odmGenerator = odmGenerator;
        this.artifactWriter = artifactWriter;
    }

    public void execute(Long jobId) {
        ExportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Export job not found: " + jobId));

        if (job.getStatus() != ExportJobStatus.PENDING) {
            log.warn("Export job {} is not in PENDING state (current: {}), skipping", jobId, job.getStatus());
            return;
        }

        if (job.getExportFormat() != ExportFormat.ODM_XML) {
            log.warn("Export job {} format {} is not ODM_XML, skipping execution", jobId, job.getExportFormat());
            return;
        }

        job.setStatus(ExportJobStatus.RUNNING);
        jobRepository.save(job);

        try {
            var studySnapshot = dataProvider.getStudySnapshot(job.getStudyId(), job.getOdmContractVersion());
            var subjectData = dataProvider.getSubjectData(job.getStudyId());

            String fileOid = "export_" + jobId + "_" + UUID.randomUUID().toString().substring(0, 8);
            String xmlContent = odmGenerator.generate(studySnapshot, subjectData, job.getOdmContractVersion(), fileOid);

            var artifact = artifactWriter.writeOdmXml(jobId, xmlContent);

            job.setStatus(ExportJobStatus.COMPLETED);
            job.setFilePath(artifact.filePath());
            job.setFileSize(artifact.fileSize());
            jobRepository.save(job);

            log.info("ODM export completed: jobId={}, subjects={}, size={}", jobId, subjectData.size(), artifact.fileSize());

        } catch (Exception e) {
            job.setStatus(ExportJobStatus.FAILED);
            job.setErrorMessage(truncateMessage(e.getMessage()));
            jobRepository.save(job);
            log.error("ODM export failed: jobId={}", jobId, e);
        }
    }

    public void cancelIfRunning(Long jobId) {
        jobRepository.findById(jobId).ifPresent(job -> {
            if (job.getStatus() == ExportJobStatus.RUNNING) {
                job.setStatus(ExportJobStatus.CANCELLED);
                jobRepository.save(job);
                log.info("Export job cancelled: id={}", jobId);
            }
        });
    }

    private String truncateMessage(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() > 3900 ? message.substring(0, 3900) + "..." : message;
    }
}
