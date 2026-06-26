package org.researchedc.module.datacapture.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.datacapture.dto.AttachmentDTO;
import org.researchedc.module.datacapture.service.DataCaptureService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DataCaptureControllerTest {

    @Mock private DataCaptureService dataCaptureService;
    @Mock private CurrentUserUtils currentUserUtils;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DataCaptureController(dataCaptureService, currentUserUtils))
                .build();
    }

    @Test
    void listAttachments_usesCurrentUserAndReturnsAttachmentDtos() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(dataCaptureService.listAttachmentsByEventCrf(100, 42))
                .thenReturn(List.of(new AttachmentDTO("bGFiLnBkZg", "lab.pdf", 8L)));

        mockMvc.perform(get("/api/v1/data-capture/events/100/attachments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("bGFiLnBkZg"))
                .andExpect(jsonPath("$[0].fileName").value("lab.pdf"))
                .andExpect(jsonPath("$[0].size").value(8));

        verify(dataCaptureService).listAttachmentsByEventCrf(100, 42);
    }

    @Test
    void downloadAttachment_usesPathAttachmentIdAndCurrentUser() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);

        mockMvc.perform(get("/api/v1/data-capture/events/100/attachments/bGFiLnBkZg"))
                .andExpect(status().isOk());

        verify(dataCaptureService).downloadAttachmentByEventCrf(eq(100), eq("bGFiLnBkZg"), eq(42), any());
    }

    @Test
    void uploadAttachment_usesEventCrfPathAndCurrentUser() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        MockMultipartFile file = new MockMultipartFile(
                "file", "lab.pdf", MediaType.APPLICATION_PDF_VALUE, "data".getBytes());

        mockMvc.perform(multipart("/api/v1/data-capture/events/100/attachments")
                        .file(file))
                .andExpect(status().isOk());

        verify(dataCaptureService).uploadAttachment(eq(100), any(), eq(42));
    }
}
