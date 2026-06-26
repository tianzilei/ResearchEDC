package org.researchedc.module.export.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import static org.researchedc.testutil.TestDataFactory.*;
import org.researchedc.module.export.dto.CreateExportJobRequest;
import org.researchedc.module.export.dto.ExportJobDTO;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.repository.ExportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private ExportJobRepository jobRepository;
    @Mock private OdmExportExecutionService odmExecutionService;
    private ExportService service;

    @BeforeEach
    void setUp() {
        service = new ExportService(jobRepository, odmExecutionService);
    }

    @Test
    void createJob_savesAndReturnsDTO() {
        CreateExportJobRequest request = new CreateExportJobRequest();
        request.setStudyId(1);
        request.setName("Test Export");
        request.setExportFormat(ExportFormat.CSV);
        request.setRequestedBy(100);

        ExportJob savedJob = new ExportJob();
        savedJob.setId(1L);
        savedJob.setStudyId(1);
        savedJob.setName("Test Export");
        savedJob.setExportFormat(ExportFormat.CSV);
        savedJob.setRequestedBy(100);
        savedJob.setStatus(ExportJobStatus.PENDING);

        when(jobRepository.save(any())).thenReturn(savedJob);

        ExportJobDTO result = service.createJob(request);

        assertEquals("Test Export", result.getName());
        assertEquals(ExportFormat.CSV, result.getExportFormat());
        assertEquals(ExportJobStatus.PENDING, result.getStatus());
        verify(jobRepository).save(any());
    }

    @Test
    void listJobs_returnsJobsForStudy() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(1);
        job.setName("Job 1");
        job.setStatus(ExportJobStatus.COMPLETED);

        when(jobRepository.findByStudyIdOrderByRequestedDateDesc(1))
                .thenReturn(List.of(job));

        List<ExportJobDTO> result = service.listJobs(1);

        assertEquals(1, result.size());
        assertEquals("Job 1", result.getFirst().getName());
    }

    @Test
    void getJob_whenFound_returnsDTO() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setName("My Job");
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        ExportJobDTO result = service.getJob(1L);

        assertEquals("My Job", result.getName());
        assertEquals(ExportJobStatus.RUNNING, result.getStatus());
    }

    @Test
    void getJob_whenNotFound_throwsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getJob(99L));
    }

    @Test
    void cancelJob_whenPending_changesStatus() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.PENDING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExportJobDTO result = service.cancelJob(1L);

        assertEquals(ExportJobStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelJob_whenCompleted_doesNothing() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        ExportJobDTO result = service.cancelJob(1L);

        assertEquals(ExportJobStatus.COMPLETED, result.getStatus());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void retryJob_whenFailed_resetsStatus() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.FAILED);
        job.setRetryCount(0);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExportJobDTO result = service.retryJob(1L);

        assertEquals(ExportJobStatus.PENDING, result.getStatus());
        assertEquals(1, result.getRetryCount());
    }

    @Test
    void retryJob_whenCompleted_doesNothing() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        ExportJobDTO result = service.retryJob(1L);

        assertEquals(ExportJobStatus.COMPLETED, result.getStatus());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void markRunning_updatesStatus() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.PENDING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markRunning(1L);

        verify(jobRepository).save(argThat(j -> j.getStatus() == ExportJobStatus.RUNNING));
    }

    @Test
    void markCompleted_updatesStatusAndFileInfo() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markCompleted(1L, "/exports/file.csv", 1024L);

        verify(jobRepository).save(argThat(j ->
                j.getStatus() == ExportJobStatus.COMPLETED
                && "/exports/file.csv".equals(j.getFilePath())
                && j.getFileSize() == 1024L));
    }

    @Test
    void markFailed_updatesStatusAndError() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markFailed(1L, "Out of disk space");

        verify(jobRepository).save(argThat(j ->
                j.getStatus() == ExportJobStatus.FAILED
                && "Out of disk space".equals(j.getErrorMessage())));
    }

    @Test
    void createJob_odmXml_triggersExecution() {
        CreateExportJobRequest request = new CreateExportJobRequest();
        request.setStudyId(1);
        request.setName("ODM Export");
        request.setExportFormat(ExportFormat.ODM_XML);
        request.setRequestedBy(100);

        ExportJob savedJob = new ExportJob();
        savedJob.setId(1L);
        savedJob.setStudyId(1);
        savedJob.setName("ODM Export");
        savedJob.setExportFormat(ExportFormat.ODM_XML);
        savedJob.setRequestedBy(100);
        savedJob.setStatus(ExportJobStatus.PENDING);

        ExportJob completedJob = new ExportJob();
        completedJob.setId(1L);
        completedJob.setStatus(ExportJobStatus.COMPLETED);
        completedJob.setFilePath("/exports/odm/1/export_1.xml");
        completedJob.setFileSize(200L);

        when(jobRepository.save(any())).thenReturn(savedJob);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(completedJob));

        ExportJobDTO result = service.createJob(request);

        verify(odmExecutionService).execute(1L);
        assertEquals(ExportJobStatus.COMPLETED, result.getStatus());
    }

    @Test
    void createJob_csv_doesNotTriggerExecution() {
        CreateExportJobRequest request = new CreateExportJobRequest();
        request.setStudyId(1);
        request.setName("CSV Export");
        request.setExportFormat(ExportFormat.CSV);
        request.setRequestedBy(100);

        ExportJob savedJob = new ExportJob();
        savedJob.setId(1L);
        savedJob.setStudyId(1);
        savedJob.setName("CSV Export");
        savedJob.setExportFormat(ExportFormat.CSV);
        savedJob.setStatus(ExportJobStatus.PENDING);

        when(jobRepository.save(any())).thenReturn(savedJob);

        service.createJob(request);

        verify(odmExecutionService, never()).execute(anyLong());
    }

    @Test
    void getDownload_completedJob_returnsResource() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);
        job.setFilePath("/exports/odm/1/export_1.xml");
        job.setFileSize(500L);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        ExportService.DownloadResult result = service.getDownload(1L);

        assertNotNull(result.resource());
        assertEquals("export_1.xml", result.filename());
        assertEquals(500L, result.fileSize());
    }

    @Test
    void getDownload_notCompleted_throwsException() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.PENDING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () -> service.getDownload(1L));
    }

    @Test
    void getDownload_nullFilePath_throwsException() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);
        job.setFilePath(null);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () -> service.getDownload(1L));
    }

    @Test
    void markFailed_storesMessageAsProvided() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markFailed(1L, "Disk full");

        verify(jobRepository).save(argThat(j -> "Disk full".equals(j.getErrorMessage())));
    }

    @Test
    void markFailed_nullMessage_storesNull() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markFailed(1L, null);

        verify(jobRepository).save(argThat(j -> j.getErrorMessage() == null));
    }

    @Test
    void markFailed_setsCompletedDate() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markFailed(1L, "Error");

        verify(jobRepository).save(argThat(j -> j.getCompletedDate() != null));
    }
}
