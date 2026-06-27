package org.researchedc.module.export.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import static org.researchedc.testutil.TestDataFactory.*;
import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.export.dto.CreateExportJobRequest;
import org.researchedc.module.export.dto.ExportJobDTO;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.repository.ExportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private ExportJobRepository jobRepository;
    @Mock private OdmExportExecutionService odmExecutionService;
    @Mock private CurrentStudyAccessService currentStudyAccessService;
    private ExportService service;

    @BeforeEach
    void setUp() {
        service = new ExportService(jobRepository, odmExecutionService, currentStudyAccessService);
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

        when(currentStudyAccessService.canExportStudy(100, 1)).thenReturn(true);
        when(jobRepository.save(any())).thenReturn(savedJob);

        ExportJobDTO result = service.createJob(request, 100);

        assertEquals("Test Export", result.getName());
        assertEquals(ExportFormat.CSV, result.getExportFormat());
        assertEquals(ExportJobStatus.PENDING, result.getStatus());
        assertEquals(100, result.getRequestedBy());
        verify(jobRepository).save(any());
    }

    @Test
    void createJob_withoutStudyExportAccess_throwsAccessDenied() {
        CreateExportJobRequest request = new CreateExportJobRequest();
        request.setStudyId(1);
        request.setName("Test Export");
        request.setExportFormat(ExportFormat.CSV);

        when(currentStudyAccessService.canExportStudy(100, 1)).thenReturn(false);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.createJob(request, 100));
        verify(jobRepository, never()).save(any());
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
        when(currentStudyAccessService.canExportStudy(42, 1)).thenReturn(true);

        List<ExportJobDTO> result = service.listJobs(1, 42);

        assertEquals(1, result.size());
        assertEquals("Job 1", result.getFirst().getName());
    }

    @Test
    void listJobs_withoutStudyExportAccess_throwsAccessDenied() {
        when(currentStudyAccessService.canExportStudy(42, 1)).thenReturn(false);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.listJobs(1, 42));
        verify(jobRepository, never()).findByStudyIdOrderByRequestedDateDesc(anyInt());
    }

    @Test
    void getJob_whenFound_returnsDTO() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(2);
        job.setName("My Job");
        job.setStatus(ExportJobStatus.RUNNING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        ExportJobDTO result = service.getJob(1L, 42);

        assertEquals("My Job", result.getName());
        assertEquals(ExportJobStatus.RUNNING, result.getStatus());
    }

    @Test
    void getJob_whenNotFound_throwsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getJob(99L, 42));
    }

    @Test
    void cancelJob_whenPending_changesStatus() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(2);
        job.setStatus(ExportJobStatus.PENDING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExportJobDTO result = service.cancelJob(1L, 42);

        assertEquals(ExportJobStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelJob_whenCompleted_doesNothing() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(2);
        job.setStatus(ExportJobStatus.COMPLETED);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        ExportJobDTO result = service.cancelJob(1L, 42);

        assertEquals(ExportJobStatus.COMPLETED, result.getStatus());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void retryJob_whenFailed_resetsStatus() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(2);
        job.setStatus(ExportJobStatus.FAILED);
        job.setRetryCount(0);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ExportJobDTO result = service.retryJob(1L, 42);

        assertEquals(ExportJobStatus.PENDING, result.getStatus());
        assertEquals(1, result.getRetryCount());
    }

    @Test
    void retryJob_whenCompleted_doesNothing() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(2);
        job.setStatus(ExportJobStatus.COMPLETED);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        ExportJobDTO result = service.retryJob(1L, 42);

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
        completedJob.setStudyId(1);
        completedJob.setStatus(ExportJobStatus.COMPLETED);
        completedJob.setFilePath("/exports/odm/1/export_1.xml");
        completedJob.setFileSize(200L);

        when(currentStudyAccessService.canExportStudy(100, 1)).thenReturn(true);
        when(jobRepository.save(any())).thenReturn(savedJob);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(completedJob));

        ExportJobDTO result = service.createJob(request, 100);

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

        when(currentStudyAccessService.canExportStudy(100, 1)).thenReturn(true);
        when(jobRepository.save(any())).thenReturn(savedJob);

        service.createJob(request, 100);

        verify(odmExecutionService, never()).execute(anyLong());
    }

    @Test
    void getDownload_completedJob_returnsResource(@TempDir Path tempDir) throws Exception {
        Path artifact = tempDir.resolve("export_1.xml");
        Files.writeString(artifact, "<ODM/>");

        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);
        job.setFilePath(artifact.toString());
        job.setFileSize(500L);
        job.setStudyId(2);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        ExportService.DownloadResult result = service.getDownload(1L, 42);

        assertNotNull(result.resource());
        assertEquals("export_1.xml", result.filename());
        assertEquals(Files.size(artifact), result.fileSize());
    }

    @Test
    void getDownload_withoutStudyExportAccess_throwsAccessDenied(@TempDir Path tempDir) throws Exception {
        Path artifact = tempDir.resolve("export_1.xml");
        Files.writeString(artifact, "<ODM/>");

        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(2);
        job.setStatus(ExportJobStatus.COMPLETED);
        job.setFilePath(artifact.toString());

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(false);

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.getDownload(1L, 42));
    }

    @Test
    void getDownload_notCompleted_throwsException() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.PENDING);
        job.setStudyId(2);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.getDownload(1L, 42));
    }

    @Test
    void getDownload_nullFilePath_throwsException() {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);
        job.setFilePath(null);
        job.setStudyId(2);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.getDownload(1L, 42));
    }

    @Test
    void getDownload_missingArtifact_throwsUnavailableException(@TempDir Path tempDir) {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStatus(ExportJobStatus.COMPLETED);
        job.setFilePath(tempDir.resolve("missing.xml").toString());
        job.setStudyId(2);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(currentStudyAccessService.canExportStudy(42, 2)).thenReturn(true);

        ExportService.ExportArtifactUnavailableException ex = assertThrows(
                ExportService.ExportArtifactUnavailableException.class,
                () -> service.getDownload(1L, 42));
        assertTrue(ex.getMessage().contains("job 1"));
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
