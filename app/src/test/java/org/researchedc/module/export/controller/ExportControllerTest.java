package org.researchedc.module.export.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.export.dto.CreateExportJobRequest;
import org.researchedc.module.export.dto.ExportJobDTO;
import org.researchedc.module.export.enums.ExportFormat;
import org.researchedc.module.export.enums.ExportJobStatus;
import org.researchedc.module.export.service.ExportService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private ExportService exportService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExportController(exportService))
                .build();
    }

    @Test
    void createJob_returnsCreatedOdmExportJob() throws Exception {
        CreateExportJobRequest request = new CreateExportJobRequest();
        request.setStudyId(1);
        request.setName("ODM export");
        request.setExportFormat(ExportFormat.ODM_XML);
        request.setRequestedBy(42);
        request.setCriteriaJson("{\"includeDNs\":true}");

        ExportJobDTO response = job(10L, ExportJobStatus.PENDING);
        response.setName("ODM export");
        response.setExportFormat(ExportFormat.ODM_XML);
        response.setRequestedBy(42);
        response.setCriteriaJson("{\"includeDNs\":true}");
        when(exportService.createJob(any(CreateExportJobRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/exports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.exportFormat").value("ODM_XML"))
                .andExpect(jsonPath("$.criteriaJson").value("{\"includeDNs\":true}"));

        ArgumentCaptor<CreateExportJobRequest> captor = ArgumentCaptor.forClass(CreateExportJobRequest.class);
        verify(exportService).createJob(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(1, captor.getValue().getStudyId());
        org.junit.jupiter.api.Assertions.assertEquals(ExportFormat.ODM_XML, captor.getValue().getExportFormat());
    }

    @Test
    void listJobs_returnsStudyJobs() throws Exception {
        ExportJobDTO first = job(1L, ExportJobStatus.PENDING);
        ExportJobDTO second = job(2L, ExportJobStatus.COMPLETED);
        when(exportService.listJobs(1)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/exports").param("studyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));

        verify(exportService).listJobs(1);
    }

    @Test
    void getJob_returnsJob() throws Exception {
        when(exportService.getJob(12L)).thenReturn(job(12L, ExportJobStatus.RUNNING));

        mockMvc.perform(get("/api/v1/exports/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    void cancelJob_returnsCancelledJob() throws Exception {
        when(exportService.cancelJob(13L)).thenReturn(job(13L, ExportJobStatus.CANCELLED));

        mockMvc.perform(post("/api/v1/exports/13/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(13))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(exportService).cancelJob(13L);
    }

    @Test
    void retryJob_returnsQueuedJob() throws Exception {
        ExportJobDTO response = job(14L, ExportJobStatus.PENDING);
        response.setRetryCount(2);
        when(exportService.retryJob(14L)).thenReturn(response);

        mockMvc.perform(post("/api/v1/exports/14/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(14))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.retryCount").value(2));

        verify(exportService).retryJob(14L);
    }

    private ExportJobDTO job(Long id, ExportJobStatus status) {
        ExportJobDTO dto = new ExportJobDTO();
        dto.setId(id);
        dto.setStudyId(1);
        dto.setName("Export " + id);
        dto.setExportFormat(ExportFormat.CSV);
        dto.setStatus(status);
        dto.setRequestedBy(100);
        dto.setRetryCount(0);
        return dto;
    }
}
