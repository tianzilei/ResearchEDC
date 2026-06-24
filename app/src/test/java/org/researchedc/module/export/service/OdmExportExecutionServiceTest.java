package org.researchedc.module.export.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.researchedc.module.export.entity.ExportJob;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.enums.OdmContractVersion;
import org.researchedc.module.export.internal.ExportDataProvider;
import org.researchedc.module.export.internal.OdmStudySnapshot;
import org.researchedc.module.export.internal.OdmSubjectDataSnapshot;
import org.researchedc.module.export.repository.ExportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OdmExportExecutionServiceTest {

    @Mock private ExportJobRepository jobRepository;
    @Mock private ExportDataProvider dataProvider;
    @Mock private OdmExportGenerator odmGenerator;
    @Mock private ExportArtifactWriter artifactWriter;

    private OdmExportExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new OdmExportExecutionService(
                jobRepository, dataProvider, odmGenerator, artifactWriter);
    }

    @Test
    void execute_odmXmlJob_transitionsToCompleted() {
        ExportJob job = createJob(ExportFormat.ODM_XML, ExportJobStatus.PENDING);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(dataProvider.getStudySnapshot(eq(1), eq(OdmContractVersion.OC2_1)))
                .thenReturn(new OdmStudySnapshot("S_1", "Study", null, null, null, null, "mdv_1_1", null));
        when(dataProvider.getSubjectData(1)).thenReturn(java.util.Collections.emptyList());
        when(odmGenerator.generate(any(), any(), eq(OdmContractVersion.OC2_1), anyString()))
                .thenReturn("<ODM/>");
        when(artifactWriter.writeOdmXml(eq(1L), eq("<ODM/>")))
                .thenReturn(new ExportArtifactWriter.ArtifactResult("/exports/odm/1/export_1.xml", 100L));

        executionService.execute(1L);

        verify(jobRepository, atLeastOnce()).save(argThat(j ->
                j.getStatus() == ExportJobStatus.COMPLETED
                && "/exports/odm/1/export_1.xml".equals(j.getFilePath())
                && j.getFileSize() == 100L));
    }

    @Test
    void execute_generationFails_transitionsToFailed() {
        ExportJob job = createJob(ExportFormat.ODM_XML, ExportJobStatus.PENDING);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(dataProvider.getStudySnapshot(eq(1), any())).thenReturn(
                new OdmStudySnapshot("S_1", "Study", null, null, null, null, "mdv_1_1", null));
        when(dataProvider.getSubjectData(1)).thenReturn(java.util.Collections.emptyList());
        when(odmGenerator.generate(any(), any(), any(), anyString()))
                .thenThrow(new RuntimeException("Generation error"));

        executionService.execute(1L);

        verify(jobRepository, atLeastOnce()).save(argThat(j ->
                j.getStatus() == ExportJobStatus.FAILED
                && "Generation error".equals(j.getErrorMessage())));
    }

    @Test
    void execute_notPending_doesNotExecute() {
        ExportJob job = createJob(ExportFormat.ODM_XML, ExportJobStatus.COMPLETED);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        executionService.execute(1L);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void execute_csvFormat_doesNotExecuteOdm() {
        ExportJob job = createJob(ExportFormat.CSV, ExportJobStatus.PENDING);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        executionService.execute(1L);

        verify(jobRepository, never()).save(any());
    }

    @Test
    void execute_defaultContractVersion_isOc21() {
        ExportJob job = createJob(ExportFormat.ODM_XML, ExportJobStatus.PENDING);
        job.setOdmContractVersion(OdmContractVersion.OC2_1);
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(dataProvider.getStudySnapshot(eq(1), eq(OdmContractVersion.OC2_1)))
                .thenReturn(new OdmStudySnapshot("S_1", "Study", null, null, null, null, "mdv_1_1", null));
        when(dataProvider.getSubjectData(1)).thenReturn(java.util.Collections.emptyList());
        when(odmGenerator.generate(any(), any(), eq(OdmContractVersion.OC2_1), anyString()))
                .thenReturn("<ODM/>");
        when(artifactWriter.writeOdmXml(eq(1L), anyString()))
                .thenReturn(new ExportArtifactWriter.ArtifactResult("/path", 50L));

        executionService.execute(1L);

        verify(odmGenerator).generate(any(), any(), eq(OdmContractVersion.OC2_1), anyString());
    }

    private ExportJob createJob(ExportFormat format, ExportJobStatus status) {
        ExportJob job = new ExportJob();
        job.setId(1L);
        job.setStudyId(1);
        job.setName("Test");
        job.setExportFormat(format);
        job.setOdmContractVersion(OdmContractVersion.OC2_1);
        job.setStatus(status);
        return job;
    }
}
