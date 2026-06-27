package org.researchedc.module.dataimport.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.dataimport.dto.CreateImportJobRequest;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.dto.ImportPreviewDTO;
import org.researchedc.module.dataimport.dto.ImportResultDTO;
import org.researchedc.module.dataimport.enums.ImportJobStatus;
import org.researchedc.module.dataimport.enums.ImportType;
import org.researchedc.module.dataimport.service.ImportService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImportControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private ImportService importService;
    @Mock private CurrentUserUtils currentUserUtils;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ImportController(importService, currentUserUtils))
                .build();
    }

    @Test
    void createJob_returnsCreatedJob() throws Exception {
        CreateImportJobRequest request = new CreateImportJobRequest();
        request.setStudyId(1);
        request.setName("CRF data import");
        request.setImportType(ImportType.CRF_DATA);
        request.setFileName("data.xml");
        request.setStoredFilePath("/tmp/data.xml");
        request.setRequestedBy(42);

        ImportJobDTO response = job(10L, ImportJobStatus.STAGED);
        response.setName("CRF data import");
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.createJob(any(CreateImportJobRequest.class), eq(42))).thenReturn(response);

        mockMvc.perform(post("/api/v1/imports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("STAGED"))
                .andExpect(jsonPath("$.name").value("CRF data import"));

        ArgumentCaptor<CreateImportJobRequest> captor = ArgumentCaptor.forClass(CreateImportJobRequest.class);
        verify(importService).createJob(captor.capture(), eq(42));
        org.junit.jupiter.api.Assertions.assertEquals(1, captor.getValue().getStudyId());
        org.junit.jupiter.api.Assertions.assertEquals(ImportType.CRF_DATA, captor.getValue().getImportType());
    }

    @Test
    void uploadFile_usesCurrentUserAndReturnsCreatedJob() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "data.xml", MediaType.TEXT_XML_VALUE, "<ODM/>".getBytes());
        ImportJobDTO response = job(11L, ImportJobStatus.STAGED);
        response.setRequestedBy(42);

        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.uploadFile(any(MultipartFile.class), eq("CRF_DATA"), eq(1), eq("Nightly import"), eq(42)))
                .thenReturn(response);

        mockMvc.perform(multipart("/api/v1/imports/upload")
                        .file(file)
                        .param("studyId", "1")
                        .param("importType", "CRF_DATA")
                        .param("name", "Nightly import"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.requestedBy").value(42));
    }

    @Test
    void listJobs_returnsStudyJobs() throws Exception {
        ImportJobDTO first = job(1L, ImportJobStatus.STAGED);
        ImportJobDTO second = job(2L, ImportJobStatus.COMPLETED);
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.listJobs(1, 42)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/v1/imports").param("studyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));
    }

    @Test
    void getJob_returnsJob() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.getJob(12L, 42)).thenReturn(job(12L, ImportJobStatus.VALIDATED));

        mockMvc.perform(get("/api/v1/imports/12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    void validate_returnsPreview() throws Exception {
        ImportPreviewDTO preview = ImportPreviewDTO.valid(2, 7, 1);
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.validate(13L, 42)).thenReturn(preview);

        mockMvc.perform(post("/api/v1/imports/13/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("validated"))
                .andExpect(jsonPath("$.eventCrfs").value(2))
                .andExpect(jsonPath("$.totalItems").value(7))
                .andExpect(jsonPath("$.editCheckErrors").value(1));

        verify(importService).validate(13L, 42);
    }

    @Test
    void validate_whenServiceFails_marksFailedAndReturnsFailedJob() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        doThrow(new RuntimeException("Validation failed")).when(importService).validate(14L, 42);

        mockMvc.perform(post("/api/v1/imports/14/validate"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.errors[0]").value("Validation failed"));

        verify(importService).markFailed(14L, "Validation failed");
    }

    @Test
    void commit_returnsCommitResult() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.commit(15L, 42)).thenReturn(ImportResultDTO.committed(2, 9));

        mockMvc.perform(post("/api/v1/imports/15/commit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("committed"))
                .andExpect(jsonPath("$.eventCrfs").value(2))
                .andExpect(jsonPath("$.items").value(9));

        verify(importService).commit(15L, 42);
    }


    @Test
    void preview_returnsStoredPreview() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(importService.getPreview(16L, 42)).thenReturn(ImportPreviewDTO.valid(1, 4, 0));

        mockMvc.perform(get("/api/v1/imports/16/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("validated"))
                .andExpect(jsonPath("$.eventCrfs").value(1))
                .andExpect(jsonPath("$.totalItems").value(4));
    }

    private ImportJobDTO job(Long id, ImportJobStatus status) {
        ImportJobDTO dto = new ImportJobDTO();
        dto.setId(id);
        dto.setStudyId(1);
        dto.setName("Import " + id);
        dto.setImportType(ImportType.CRF_DATA);
        dto.setFileName("data.xml");
        dto.setStoredFilePath("/tmp/data.xml");
        dto.setStatus(status);
        dto.setRequestedBy(100);
        dto.setRetryCount(0);
        return dto;
    }
}
