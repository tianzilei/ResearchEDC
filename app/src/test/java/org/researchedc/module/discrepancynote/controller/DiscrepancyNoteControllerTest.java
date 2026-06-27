package org.researchedc.module.discrepancynote.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.discrepancynote.dto.CreateDiscrepancyNoteRequest;
import org.researchedc.module.discrepancynote.entity.DiscrepancyNoteEntity;
import org.researchedc.module.discrepancynote.service.DiscrepancyNoteService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DiscrepancyNoteControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private DiscrepancyNoteService discrepancyNoteService;
    @Mock private CurrentUserUtils currentUserUtils;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DiscrepancyNoteController(discrepancyNoteService, currentUserUtils))
                .build();
    }

    @Test
    void listNotes_withEventCrfIdPassesCurrentUserToEventCrfQuery() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(discrepancyNoteService.listByEventCrf(55, 42)).thenReturn(List.of(note(1, 10, 100)));

        mockMvc.perform(get("/api/v1/discrepancy-notes").param("eventCrfId", "55"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].discrepancyNoteId").value(1))
                .andExpect(jsonPath("$[0].studyId").value(10));

        verify(discrepancyNoteService).listByEventCrf(55, 42);
    }

    @Test
    void listNotes_withStudyIdPassesCurrentUserToStudyQuery() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(discrepancyNoteService.listByStudy(10, 42)).thenReturn(List.of(note(1, 10, 100)));

        mockMvc.perform(get("/api/v1/discrepancy-notes").param("studyId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].discrepancyNoteId").value(1));

        verify(discrepancyNoteService).listByStudy(10, 42);
    }

    @Test
    void getNote_passesCurrentUser() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(discrepancyNoteService.getById(1, 42)).thenReturn(note(1, 10, 100));

        mockMvc.perform(get("/api/v1/discrepancy-notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discrepancyNoteId").value(1));

        verify(discrepancyNoteService).getById(1, 42);
    }

    @Test
    void createNote_usesCurrentUserAsOwnerAndAccessUser() throws Exception {
        CreateDiscrepancyNoteRequest request = new CreateDiscrepancyNoteRequest();
        request.setDescription("Missing value");
        request.setDetailedNotes("Value required before lock");
        request.setEntityType("itemData");
        request.setEntityId(55);
        request.setStudyId(10);

        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(discrepancyNoteService.create(
                "Missing value", 1, 1, "Value required before lock",
                42, null, "itemData", 55, 10, null, 42))
                .thenReturn(note(1, 10, 42));

        mockMvc.perform(post("/api/v1/discrepancy-notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discrepancyNoteId").value(1))
                .andExpect(jsonPath("$.ownerId").value(42));

        verify(discrepancyNoteService).create(
                "Missing value", 1, 1, "Value required before lock",
                42, null, "itemData", 55, 10, null, 42);
    }

    @Test
    void resolveNote_passesCurrentUser() throws Exception {
        DiscrepancyNoteEntity resolved = note(1, 10, 100);
        resolved.setResolutionStatusId(5);
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(discrepancyNoteService.resolveNote(1, 42)).thenReturn(resolved);

        mockMvc.perform(patch("/api/v1/discrepancy-notes/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolutionStatusId").value(5));

        verify(discrepancyNoteService).resolveNote(1, 42);
    }

    private static DiscrepancyNoteEntity note(Integer id, Integer studyId, Integer ownerId) {
        DiscrepancyNoteEntity entity = new DiscrepancyNoteEntity();
        entity.setDiscrepancyNoteId(id);
        entity.setDescription("Note " + id);
        entity.setDetailedNotes("Details " + id);
        entity.setDiscrepancyNoteTypeId(1);
        entity.setResolutionStatusId(1);
        entity.setEntityType("itemData");
        entity.setEntityId(55);
        entity.setStudyId(studyId);
        entity.setOwnerId(ownerId);
        return entity;
    }
}
