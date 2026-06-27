package org.researchedc.module.dataset.controller;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.dataset.dto.CreateDatasetRequest;
import org.researchedc.module.dataset.entity.DatasetEntity;
import org.researchedc.module.dataset.service.DatasetService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DatasetControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private DatasetService datasetService;
    @Mock private CurrentUserUtils currentUserUtils;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DatasetController(datasetService, currentUserUtils))
                .build();
    }

    @Test
    void listDatasets_withStudyIdPassesCurrentUser() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(datasetService.listByStudy(10, 42)).thenReturn(List.of(dataset(1, 10, 100)));

        mockMvc.perform(get("/api/v1/datasets").param("studyId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].datasetId").value(1))
                .andExpect(jsonPath("$[0].studyId").value(10));

        verify(datasetService).listByStudy(10, 42);
    }

    @Test
    void listDatasets_withoutStudyIdPassesCurrentUser() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(datasetService.listAll(42)).thenReturn(List.of(dataset(1, 10, 100)));

        mockMvc.perform(get("/api/v1/datasets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].datasetId").value(1));

        verify(datasetService).listAll(42);
    }

    @Test
    void getDataset_passesCurrentUser() throws Exception {
        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(datasetService.getById(1, 42)).thenReturn(dataset(1, 10, 100));

        mockMvc.perform(get("/api/v1/datasets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(1));

        verify(datasetService).getById(1, 42);
    }

    @Test
    void createDataset_usesCurrentUserAsOwnerAndAccessUser() throws Exception {
        CreateDatasetRequest request = new CreateDatasetRequest();
        request.setName("Safety dataset");
        request.setDescription("Safety review");
        request.setStudyId(10);

        when(currentUserUtils.getCurrentUserId()).thenReturn(42);
        when(datasetService.create("Safety dataset", "Safety review", 10, 42, 42))
                .thenReturn(dataset(1, 10, 42));

        mockMvc.perform(post("/api/v1/datasets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datasetId").value(1))
                .andExpect(jsonPath("$.ownerId").value(42));

        verify(datasetService).create("Safety dataset", "Safety review", 10, 42, 42);
    }

    private static DatasetEntity dataset(Integer id, Integer studyId, Integer ownerId) {
        DatasetEntity entity = new DatasetEntity();
        entity.setDatasetId(id);
        entity.setName("Dataset " + id);
        entity.setDescription("Description " + id);
        entity.setStudyId(studyId);
        entity.setOwnerId(ownerId);
        entity.setStatusId(1);
        return entity;
    }
}
