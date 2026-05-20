package org.researchedc.module.randomization.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.researchedc.module.randomization.dto.*;
import org.researchedc.module.randomization.enums.AssignmentStatus;
import org.researchedc.module.randomization.enums.AuditAction;
import org.researchedc.module.randomization.enums.RandomizationAlgorithm;
import org.researchedc.module.randomization.enums.SchemeStatus;
import org.researchedc.module.randomization.service.RandomizationService;
import org.researchedc.module.randomization.service.UnblindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class RandomizationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private RandomizationService randomizationService;
    @Mock private UnblindingService unblindingService;

    @BeforeEach
    void setUp() {
        RandomizationController controller = new RandomizationController(randomizationService, unblindingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listSchemes_returns200() throws Exception {
        SchemeSummaryDTO scheme = new SchemeSummaryDTO();
        scheme.setId(1L);
        scheme.setName("Test Scheme");
        scheme.setStatus(SchemeStatus.DRAFT);

        when(randomizationService.listSchemes(1)).thenReturn(List.of(scheme));

        mockMvc.perform(get("/api/v1/randomization/schemes")
                        .param("studyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Scheme"))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));
    }

    @Test
    void getScheme_returns200() throws Exception {
        SchemeDTO scheme = new SchemeDTO();
        scheme.setId(1L);
        scheme.setName("My Scheme");
        scheme.setAlgorithm(RandomizationAlgorithm.SIMPLE);
        scheme.setStatus(SchemeStatus.ACTIVE);

        when(randomizationService.getScheme(1L)).thenReturn(scheme);

        mockMvc.perform(get("/api/v1/randomization/schemes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Scheme"))
                .andExpect(jsonPath("$.algorithm").value("SIMPLE"));
    }

    @Test
    void createScheme_returns201() throws Exception {
        SchemeDTO input = new SchemeDTO();
        input.setStudyId(1);
        input.setName("New Scheme");
        input.setAlgorithm(RandomizationAlgorithm.SIMPLE);

        SchemeDTO output = new SchemeDTO();
        output.setId(1L);
        output.setStudyId(1);
        output.setName("New Scheme");
        output.setAlgorithm(RandomizationAlgorithm.SIMPLE);
        output.setStatus(SchemeStatus.DRAFT);

        when(randomizationService.createScheme(any(), eq(0))).thenReturn(output);

        mockMvc.perform(post("/api/v1/randomization/schemes")
                        .param("userId", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Scheme"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void activateScheme_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/randomization/schemes/1/activate")
                        .param("userId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void closeScheme_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/randomization/schemes/1/close")
                        .param("userId", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void listAssignments_returns200() throws Exception {
        AssignmentDTO assignment = new AssignmentDTO();
        assignment.setId(100L);
        assignment.setSchemeId(1L);
        assignment.setArmName("Treatment");
        assignment.setStatus(AssignmentStatus.ACTIVE);

        when(randomizationService.listAssignments(1L)).thenReturn(List.of(assignment));

        mockMvc.perform(get("/api/v1/randomization/assignments")
                        .param("schemeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].armName").value("Treatment"));
    }

    @Test
    void randomize_returns200() throws Exception {
        RandomizeRequest request = new RandomizeRequest();
        request.setSchemeId(1L);
        request.setStudySubjectId(10);

        AssignmentDTO result = new AssignmentDTO();
        result.setId(100L);
        result.setSchemeId(1L);
        result.setStudySubjectId(10);
        result.setArmName("Treatment");
        result.setStatus(org.researchedc.module.randomization.enums.AssignmentStatus.ACTIVE);

        when(randomizationService.randomize(any(), eq(0))).thenReturn(result);

        mockMvc.perform(post("/api/v1/randomization/randomize")
                        .param("userId", "0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.armName").value("Treatment"));
    }

    @Test
    void getAuditLogs_byScheme_returns200() throws Exception {
        AuditLogDTO log = new AuditLogDTO();
        log.setId(1L);
        log.setSchemeId(1L);
        log.setAction(AuditAction.SCHEME_CREATED);

        when(randomizationService.getAuditLogs(1L)).thenReturn(List.of(log));

        mockMvc.perform(get("/api/v1/randomization/audit")
                        .param("schemeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SCHEME_CREATED"));
    }

    @Test
    void getAuditLogs_withoutParams_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/randomization/audit"))
                .andExpect(status().isBadRequest());
    }
}
