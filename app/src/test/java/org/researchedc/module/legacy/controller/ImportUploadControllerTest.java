package org.researchedc.module.legacy.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.module.dataimport.dto.ImportJobDTO;
import org.researchedc.module.dataimport.enums.ImportJobStatus;
import org.researchedc.module.dataimport.enums.ImportType;
import org.researchedc.module.dataimport.service.ImportService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImportUploadControllerTest {

    @Mock private ImportService importService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ImportUploadController(importService))
                .build();
    }

    @Test
    void uploadFile_delegatesToCanonicalImportServiceAndStoresSessionState() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "legacy-data.xml", MediaType.TEXT_XML_VALUE, "<ODM/>".getBytes());
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userBean", new SessionUser(77));

        ImportJobDTO job = job();
        when(importService.uploadFile(any(MultipartFile.class), eq("CRF_DATA"), eq(5), eq("legacy-data.xml"), eq(77)))
                .thenReturn(job);

        mockMvc.perform(multipart("/api/legacy/import/upload")
                        .file(file)
                        .session(session)
                        .param("studyId", "5")
                        .param("importType", "CRF_DATA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.fileName").value("legacy-data.xml"))
                .andExpect(jsonPath("$.fileSize").value(6))
                .andExpect(jsonPath("$.storedAs").value("/tmp/legacy-data.xml"))
                .andExpect(jsonPath("$.importJobId").value(21))
                .andExpect(jsonPath("$.importJob.requestedBy").value(77))
                .andExpect(request().sessionAttribute("importFilePath", "/tmp/legacy-data.xml"))
                .andExpect(request().sessionAttribute("importFileName", "legacy-data.xml"))
                .andExpect(request().sessionAttribute("importJobId", 21L));

        verify(importService).uploadFile(any(MultipartFile.class), eq("CRF_DATA"), eq(5), eq("legacy-data.xml"), eq(77));
    }

    @Test
    void uploadFile_whenSessionHasNoUser_keepsCompatibilityWithNullRequestedBy() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "legacy-data.xml", MediaType.TEXT_XML_VALUE, "<ODM/>".getBytes());
        ImportJobDTO job = job();
        job.setRequestedBy(null);
        when(importService.uploadFile(any(MultipartFile.class), eq("CRF_DATA"), eq(5), eq("legacy-data.xml"), eq(null)))
                .thenReturn(job);

        mockMvc.perform(multipart("/api/legacy/import/upload")
                        .file(file)
                        .param("studyId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(importService).uploadFile(any(MultipartFile.class), eq("CRF_DATA"), eq(5), eq("legacy-data.xml"), eq(null));
    }

    @Test
    void uploadFile_whenFileIsEmpty_returnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.xml", MediaType.TEXT_XML_VALUE, new byte[0]);

        mockMvc.perform(multipart("/api/legacy/import/upload")
                        .file(file)
                        .param("studyId", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No file provided"));

        verifyNoInteractions(importService);
    }

    @Test
    void uploadFile_whenServiceFails_returnsServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "legacy-data.xml", MediaType.TEXT_XML_VALUE, "<ODM/>".getBytes());
        when(importService.uploadFile(any(MultipartFile.class), eq("CRF_DATA"), eq(5), eq("legacy-data.xml"), eq(null)))
                .thenThrow(new RuntimeException("disk full"));

        mockMvc.perform(multipart("/api/legacy/import/upload")
                        .file(file)
                        .param("studyId", "5"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to store file: disk full"));
    }

    private ImportJobDTO job() {
        ImportJobDTO dto = new ImportJobDTO();
        dto.setId(21L);
        dto.setStudyId(5);
        dto.setName("legacy-data.xml");
        dto.setImportType(ImportType.CRF_DATA);
        dto.setFileName("legacy-data.xml");
        dto.setStoredFilePath("/tmp/legacy-data.xml");
        dto.setStatus(ImportJobStatus.STAGED);
        dto.setRequestedBy(77);
        dto.setRetryCount(0);
        return dto;
    }

    static final class SessionUser {
        private final int id;

        SessionUser(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}
