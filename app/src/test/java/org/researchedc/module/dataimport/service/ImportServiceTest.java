package org.researchedc.module.dataimport.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.dataimport.dto.CreateImportJobRequest;
import org.researchedc.module.dataimport.event.ImportCommittedEvent;
import org.springframework.context.ApplicationEventPublisher;
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
import org.researchedc.module.dataimport.repository.ImportJobRepository;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock private ImportJobRepository jobRepository;
    @Mock private ImportCrfDataAdapter importAdapter;
    @Mock private MultipartFile multipartFile;

    private ImportService service;
    private MockedStatic<Files> filesMock;

    @BeforeEach
    void setUp() {
        service = new ImportService(jobRepository, importAdapter);
    }

    @AfterEach
    void tearDown() {
        if (filesMock != null) {
            filesMock.close();
        }
    }

    private ImportJob createJob(Long id, String name, ImportType type, ImportJobStatus status) {
        ImportJob job = new ImportJob();
        job.setId(id);
        job.setStudyId(1);
        job.setName(name);
        job.setImportType(type);
        job.setFileName("test.xml");
        job.setStoredFilePath("/tmp/test.xml");
        job.setStatus(status);
        job.setRequestedBy(100);
        job.setRetryCount(0);
        return job;
    }

    @Test
    void createJob_savesAndReturnsDTO() {
        CreateImportJobRequest request = new CreateImportJobRequest();
        request.setStudyId(1);
        request.setName("Test Import");
        request.setImportType(ImportType.CRF_DATA);
        request.setFileName("data.xml");
        request.setStoredFilePath("/tmp/data.xml");
        request.setRequestedBy(100);

        ImportJob savedJob = new ImportJob();
        savedJob.setId(1L);
        savedJob.setStudyId(1);
        savedJob.setName("Test Import");
        savedJob.setImportType(ImportType.CRF_DATA);
        savedJob.setFileName("data.xml");
        savedJob.setStoredFilePath("/tmp/data.xml");
        savedJob.setRequestedBy(100);
        savedJob.setStatus(ImportJobStatus.STAGED);

        when(jobRepository.save(any())).thenReturn(savedJob);

        ImportJobDTO result = service.createJob(request);

        assertEquals("Test Import", result.getName());
        assertEquals(ImportType.CRF_DATA, result.getImportType());
        assertEquals(ImportJobStatus.STAGED, result.getStatus());
        assertEquals("data.xml", result.getFileName());
        verify(jobRepository).save(any());
    }

    @Test
    void listJobs_returnsJobsForStudy() {
        ImportJob job1 = createJob(1L, "Job 1", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        ImportJob job2 = createJob(2L, "Job 2", ImportType.CRF_DEFINITION, ImportJobStatus.COMPLETED);

        when(jobRepository.findByStudyIdOrderByRequestedDateDesc(1))
                .thenReturn(List.of(job1, job2));

        List<ImportJobDTO> result = service.listJobs(1);

        assertEquals(2, result.size());
        assertEquals("Job 1", result.get(0).getName());
        assertEquals("Job 2", result.get(1).getName());
    }

    @Test
    void listJobs_returnsEmptyWhenNoJobs() {
        when(jobRepository.findByStudyIdOrderByRequestedDateDesc(1))
                .thenReturn(List.of());

        List<ImportJobDTO> result = service.listJobs(1);

        assertTrue(result.isEmpty());
    }

    @Test
    void getJob_whenFound_returnsDTO() {
        ImportJob job = createJob(1L, "My Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        ImportJobDTO result = service.getJob(1L);

        assertEquals("My Job", result.getName());
        assertEquals(ImportJobStatus.VALIDATING, result.getStatus());
        assertEquals(ImportType.CRF_DATA, result.getImportType());
    }

    @Test
    void getJob_whenNotFound_throwsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getJob(99L));
    }

    @Test
    void markValidating_whenFound_updatesStatus() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markValidating(1L);

        verify(jobRepository).save(argThat(j -> j.getStatus() == ImportJobStatus.VALIDATING));
    }

    @Test
    void markValidating_whenNotFound_doesNothing() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        service.markValidating(99L);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void markValidated_updatesStatusAndSummary() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markValidated(1L, "{\"status\":\"validated\"}");

        verify(jobRepository).save(argThat(j ->
                j.getStatus() == ImportJobStatus.VALIDATED
                && "{\"status\":\"validated\"}".equals(j.getSummaryJson())));
    }

    @Test
    void markCommitting_updatesStatus() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markCommitting(1L);

        verify(jobRepository).save(argThat(j -> j.getStatus() == ImportJobStatus.COMMITTING));
    }

    @Test
    void markCompleted_updatesStatusAndCompletedDate() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.COMMITTING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markCompleted(1L);

        verify(jobRepository).save(argThat(j ->
                j.getStatus() == ImportJobStatus.COMPLETED
                && j.getCompletedDate() != null));
    }

    @Test
    void markFailed_updatesStatusErrorAndCompletedDate() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATING);

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        service.markFailed(1L, "Validation error: invalid XML");

        verify(jobRepository).save(argThat(j ->
                j.getStatus() == ImportJobStatus.FAILED
                && "Validation error: invalid XML".equals(j.getErrorMessage())
                && j.getCompletedDate() != null));
    }


    private String committablePreviewJson() {
        return "{\"status\":\"validated\",\"eventCrfs\":1,\"totalItems\":5,"
                + "\"editCheckErrors\":0,\"errors\":[],\"warnings\":[]}";
    }

    private ParsedOdm mockOdm() {
        return mock(ParsedOdm.class);
    }

    private void stubSuccessfulValidate() {
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.validateMetadata(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(Collections.emptyList());
        when(importAdapter.validateEventCrfs(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(new EventCrfValidationResult(true, 1));
        when(importAdapter.validateEditChecks(eq(odm), anyInt()))
                .thenReturn("{\"total\":5,\"errors\":0}");
    }

    @Test
    void validate_happyPath_marksValidated() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        stubSuccessfulValidate();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportPreviewDTO result = service.validate(1L);

        assertEquals("validated", result.getStatus());
        assertEquals(1, result.getEventCrfs());
        assertEquals(5, result.getTotalItems());
        verify(importAdapter).parseOdm(any(Path.class));
        verify(importAdapter).validateMetadata(any(), anyInt(), any(Locale.class));
        verify(importAdapter).validateEventCrfs(any(), anyInt(), any(Locale.class));
        verify(jobRepository, atLeast(2)).save(any());
    }

    @Test
    void validate_whenValidationErrors_marksInvalid() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.validateMetadata(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(List.of("Metadata mismatch"));
        // checkStatusesValid and getEventCrfBeans should NOT be called when errors exist

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportPreviewDTO result = service.validate(1L);

        assertEquals("invalid", result.getStatus());
        assertTrue(result.getErrors().contains("Metadata mismatch"));
        verify(jobRepository, atLeastOnce()).save(argThat(j -> j.getStatus() == ImportJobStatus.INVALID));
        verify(importAdapter, never()).validateEventCrfs(any(), anyInt(), any(Locale.class));
    }

    @Test
    void validate_whenStatusesInvalid_marksBlocked() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.validateMetadata(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(Collections.emptyList());
        when(importAdapter.validateEventCrfs(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(new EventCrfValidationResult(false, 0));

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportPreviewDTO result = service.validate(1L);

        assertEquals("blocked", result.getStatus());
        assertTrue(result.getErrors().contains("event_crf_status"));
        verify(jobRepository, atLeastOnce()).save(argThat(j -> j.getStatus() == ImportJobStatus.BLOCKED));
    }


    @Test
    void validate_whenNoEventCrfs_marksInvalid() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.validateMetadata(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(Collections.emptyList());
        when(importAdapter.validateEventCrfs(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(new EventCrfValidationResult(true, 0));

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportPreviewDTO result = service.validate(1L);

        assertEquals("invalid", result.getStatus());
        assertTrue(result.getErrors().get(0).contains("No event CRFs"));
        verify(importAdapter, never()).validateEditChecks(any(), anyInt());
        verify(jobRepository, atLeastOnce()).save(argThat(j -> j.getStatus() == ImportJobStatus.INVALID));
    }

    @Test
    void validate_whenEditChecksFail_marksInvalid() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.validateMetadata(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(Collections.emptyList());
        when(importAdapter.validateEventCrfs(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(new EventCrfValidationResult(true, 1));
        when(importAdapter.validateEditChecks(eq(odm), anyInt()))
                .thenReturn("{\"total\":5,\"errors\":2}");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportPreviewDTO result = service.validate(1L);

        assertEquals("invalid", result.getStatus());
        assertEquals(2, result.getEditCheckErrors());
        assertEquals(5, result.getTotalItems());
        verify(jobRepository, atLeastOnce()).save(argThat(j -> j.getStatus() == ImportJobStatus.INVALID));
    }

    @Test
    void validate_whenJobNotFound_throwsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.validate(99L));
    }

    @Test
    void validate_whenRepositoryFails_marksFailed() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        stubSuccessfulValidate();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0))
                .thenThrow(new RuntimeException("DB error during markValidated"));

        assertThrows(RuntimeException.class, () -> service.validate(1L));

        verify(jobRepository, atLeast(2)).save(any());
    }


    @Test
    void getPreview_whenSummaryExists_returnsTypedPreview() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);
        job.setSummaryJson("{\"status\":\"validated\",\"eventCrfs\":2,\"totalItems\":7,\"editCheckErrors\":1,\"errors\":[],\"warnings\":[\"1 edit-check error(s) were found.\"]}");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        ImportPreviewDTO result = service.getPreview(1L);

        assertEquals("validated", result.getStatus());
        assertEquals(2, result.getEventCrfs());
        assertEquals(7, result.getTotalItems());
        assertEquals(1, result.getEditCheckErrors());
        assertEquals(1, result.getWarnings().size());
    }

    @Test
    void commit_happyPath_marksCompleted() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);
        job.setSummaryJson(committablePreviewJson());
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.commitImport(any(), anyInt(), any()))
                .thenReturn(new CommitResult(3, 12));

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResultDTO result = service.commit(1L);

        assertEquals("committed", result.getStatus());
        assertEquals(3, result.getEventCrfs());
        assertEquals(12, result.getItems());
        assertEquals(ImportJobStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getCompletedDate());
        assertTrue(job.getSummaryJson().contains("committed"));
        assertTrue(job.getSummaryJson().contains("12"));
        verify(importAdapter).parseOdm(any(Path.class));
        verify(importAdapter).commitImport(any(), anyInt(), any());
        verify(jobRepository, atLeast(2)).save(any());
    }


    @Test
    void commit_whenStatusInvalid_throwsBeforeAdapterCall() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.INVALID);
        job.setSummaryJson("{\"status\":\"invalid\",\"eventCrfs\":0,\"totalItems\":0,\"editCheckErrors\":0,\"errors\":[\"Metadata mismatch\"],\"warnings\":[]}");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () -> service.commit(1L));

        verify(importAdapter, never()).parseOdm(any(Path.class));
        verify(jobRepository, never()).save(argThat(j -> j.getStatus() == ImportJobStatus.COMMITTING));
    }

    @Test
    void commit_whenPreviewHasEditCheckErrors_throwsBeforeAdapterCall() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);
        job.setSummaryJson("{\"status\":\"validated\",\"eventCrfs\":1,\"totalItems\":5,\"editCheckErrors\":2,\"errors\":[],\"warnings\":[]}");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        assertThrows(IllegalStateException.class, () -> service.commit(1L));

        verify(importAdapter, never()).parseOdm(any(Path.class));
        verify(jobRepository, never()).save(argThat(j -> j.getStatus() == ImportJobStatus.COMMITTING));
    }


    @Test
    void commit_publishesImportCommittedEventWhenPublisherProvided() {
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        service = new ImportService(jobRepository, importAdapter, eventPublisher);
        ImportJob job = createJob(1L, "Audited Import", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);
        job.setSummaryJson(committablePreviewJson());
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.commitImport(any(), anyInt(), any()))
                .thenReturn(new CommitResult(2, 8));
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportResultDTO result = service.commit(1L);

        assertEquals("committed", result.getStatus());
        verify(eventPublisher).publishEvent((Object) argThat(event -> {
            ImportCommittedEvent committed = (ImportCommittedEvent) event;
            return committed.importJobId().equals(1L)
                    && committed.studyId().equals(1)
                    && committed.importName().equals("Audited Import")
                    && committed.requestedBy().equals(100)
                    && committed.eventCrfs().equals(2)
                    && committed.items().equals(8);
        }));
    }

    @Test
    void commit_whenJobNotFound_throwsException() {
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.commit(99L));
    }

    @Test
    void commit_whenRepositoryFails_marksFailed() {
        ImportJob job = createJob(1L, "Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);
        job.setSummaryJson(committablePreviewJson());

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0))
                .thenThrow(new RuntimeException("DB error during markCompleted"));

        assertThrows(RuntimeException.class, () -> service.commit(1L));

        verify(jobRepository, atLeast(2)).save(any());
    }

    @Test
    void uploadFile_createsJobAndStoresFile() throws IOException {
        multipartFileSetup("data.xml", new byte[]{1, 2, 3, 4});

        mockFilesStatic();

        ImportJob savedJob = new ImportJob();
        savedJob.setId(1L);
        savedJob.setStudyId(1);
        savedJob.setName("My Import");
        savedJob.setImportType(ImportType.CRF_DATA);
        savedJob.setFileName("data.xml");
        savedJob.setStoredFilePath("/test/path");
        savedJob.setStatus(ImportJobStatus.STAGED);
        savedJob.setRequestedBy(100);
        when(jobRepository.save(any())).thenReturn(savedJob);

        ImportJobDTO result = service.uploadFile(multipartFile, "CRF_DATA", 1, "My Import", 100);

        assertEquals("My Import", result.getName());
        assertEquals(ImportType.CRF_DATA, result.getImportType());
        assertEquals(ImportJobStatus.STAGED, result.getStatus());
        assertEquals("data.xml", result.getFileName());
        assertNotNull(result.getStoredFilePath());
        verify(jobRepository).save(any());
    }

    @Test
    void uploadFile_usesCRFDefinitionType() throws IOException {
        multipartFileSetup("definition.xml", new byte[]{1, 2});

        mockFilesStatic();

        ImportJob savedJob = createJob(1L, "Def Import", ImportType.CRF_DEFINITION, ImportJobStatus.STAGED);
        when(jobRepository.save(any())).thenReturn(savedJob);

        ImportJobDTO result = service.uploadFile(multipartFile, "CRF_DEFINITION", 1, "Def Import", 100);

        assertEquals(ImportType.CRF_DEFINITION, result.getImportType());
    }

    @Test
    void uploadFile_usesFileNameWhenNameIsNull() throws IOException {
        multipartFileSetup("auto-name.xml", new byte[]{1});

        mockFilesStatic();

        ImportJob savedJob = createJob(1L, "auto-name.xml", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        when(jobRepository.save(any())).thenReturn(savedJob);

        ImportJobDTO result = service.uploadFile(multipartFile, "CRF_DATA", 1, null, 100);

        assertEquals("auto-name.xml", result.getName());
    }

    @Test
    void uploadFile_usesDefaultNameWhenBothAreNull() throws IOException {
        multipartFileSetup(null, new byte[]{1});

        mockFilesStatic();

        ImportJob savedJob = createJob(1L, "Import", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        when(jobRepository.save(any())).thenReturn(savedJob);

        ImportJobDTO result = service.uploadFile(multipartFile, "CRF_DATA", 1, null, 100);

        assertEquals("Import", result.getName());
    }

    @Test
    void uploadFile_throwsRuntimeExceptionWhenCreateDirectoriesFails() {
        filesMock = mockStatic(Files.class);
        filesMock.when(() -> Files.createDirectories(any()))
                .thenThrow(new IOException("Permission denied"));

        assertThrows(RuntimeException.class,
                () -> service.uploadFile(multipartFile, "CRF_DATA", 1, "Test", 100));
    }

    @Test
    void uploadFile_throwsRuntimeExceptionWhenCopyFails() throws IOException {
        multipartFileSetup("data.xml", new byte[]{1});

        filesMock = mockStatic(Files.class);
        filesMock.when(() -> Files.createDirectories(any())).thenReturn(null);
        filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any()))
                .thenThrow(new IOException("Disk full"));

        assertThrows(RuntimeException.class,
                () -> service.uploadFile(multipartFile, "CRF_DATA", 1, "Test", 100));
    }

    @Test
    void fullLifecycle_stagedToCompleted() {
        ImportJob job = createJob(1L, "Lifecycle Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        stubSuccessfulValidate();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(importAdapter.commitImport(any(), anyInt(), any()))
                .thenReturn(new CommitResult(1, 5));

        ImportPreviewDTO validated = service.validate(1L);
        assertEquals("validated", validated.getStatus());

        ImportResultDTO completed = service.commit(1L);
        assertEquals("committed", completed.getStatus());
        assertEquals(ImportJobStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getCompletedDate());
    }

    @Test
    void fullLifecycle_validateErrorShortCircuits() {
        ImportJob job = createJob(1L, "Fail Job", ImportType.CRF_DATA, ImportJobStatus.STAGED);
        ParsedOdm odm = mockOdm();
        when(importAdapter.parseOdm(any(Path.class))).thenReturn(odm);
        when(importAdapter.validateMetadata(eq(odm), anyInt(), any(Locale.class)))
                .thenReturn(List.of("Error"));

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ImportPreviewDTO result = service.validate(1L);
        assertEquals("invalid", result.getStatus());
        assertTrue(result.getErrors().contains("Error"));
        verify(importAdapter, never()).validateEventCrfs(any(), anyInt(), any(Locale.class));
    }

    @Test
    void fullLifecycle_validatedToFailed() {
        ImportJob job = createJob(1L, "Fail Job", ImportType.CRF_DATA, ImportJobStatus.VALIDATED);
        job.setSummaryJson(committablePreviewJson());

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0))
                .thenThrow(new RuntimeException("Commit error"));

        assertThrows(RuntimeException.class, () -> service.commit(1L));

        verify(jobRepository, atLeast(2)).save(any());
    }

    private void multipartFileSetup(String originalName, byte[] content) throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(originalName);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(content));
    }

    private void mockFilesStatic() {
        filesMock = mockStatic(Files.class);
        filesMock.when(() -> Files.createDirectories(any())).thenReturn(null);
        filesMock.when(() -> Files.copy(any(InputStream.class), any(Path.class), any())).thenReturn(1L);
    }
}
