package org.researchedc.module.legacy;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.researchedc.bean.extract.DatasetBean;
import org.researchedc.bean.extract.FilterBean;
import org.researchedc.bean.managestudy.DiscrepancyNoteBean;
import org.researchedc.bean.managestudy.StudyGroupBean;
import org.researchedc.bean.managestudy.StudyGroupClassBean;
import org.researchedc.dao.extract.DatasetDAO;
import org.researchedc.dao.extract.FilterDAO;
import org.researchedc.dao.managestudy.DiscrepancyNoteDAO;
import org.researchedc.dao.managestudy.StudyGroupClassDAO;
import org.researchedc.dao.managestudy.StudyGroupDAO;
import org.researchedc.module.crf.entity.CrfEntity;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.service.CrfService;
import org.researchedc.module.legacy.controller.LegacyCrfManageController;
import org.researchedc.module.legacy.controller.LegacyDatasetController;
import org.researchedc.module.legacy.controller.LegacyDiscrepancyNoteController;
import org.researchedc.module.legacy.controller.LegacyFilterController;
import org.researchedc.module.legacy.controller.LegacyRuleSetController;
import org.researchedc.module.legacy.controller.LegacyStudyController;
import org.researchedc.module.legacy.controller.LegacySubjectController;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.service.RuleService;
import org.researchedc.module.legacy.controller.LegacySubjectGroupController;
import org.researchedc.module.study.dto.StudyDetailDTO;
import org.researchedc.module.study.dto.StudySummaryDTO;
import org.researchedc.module.study.service.StudyService;
import org.researchedc.module.subject.dto.StudySubjectDTO;
import org.researchedc.module.subject.service.SubjectService;
import org.researchedc.module.legacy.dto.CreateCrfRequest;
import org.researchedc.module.legacy.dto.CreateDiscrepancyNoteRequest;
import org.researchedc.module.legacy.dto.SubjectGroupClassDTO;
import org.researchedc.module.legacy.dto.SubjectGroupDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Contract tests for the legacy-gateway REST API layer.
 *
 * <p>Validates HTTP contract (URLs, methods, status codes, response shapes)
 * of all 8 legacy-gateway controllers. Legacy DAOs are mocked — these tests
 * verify the API boundary, not the data layer.</p>
 */
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class LegacyGatewayContractTest {

    private static MockedStatic<org.researchedc.i18n.util.ResourceBundleProvider> resourceBundleProviderMock;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUpResourceBundle() {
        ResourceBundle mockBundle = mock(ResourceBundle.class);
        when(mockBundle.getString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        resourceBundleProviderMock = mockStatic(org.researchedc.i18n.util.ResourceBundleProvider.class);
        resourceBundleProviderMock.when(org.researchedc.i18n.util.ResourceBundleProvider::getTermsBundle)
                .thenReturn(mockBundle);
    }

    @AfterAll
    static void tearDownResourceBundle() {
        if (resourceBundleProviderMock != null) {
            resourceBundleProviderMock.close();
        }
    }

    // ─── DAO mocks (shared across controller tests) ──────────────

    @Mock private StudyService studyService;
    @Mock private SubjectService subjectService;
    @Mock private RuleService ruleService;
    @Mock private DiscrepancyNoteDAO discrepancyNoteDao;
    @Mock private DatasetDAO datasetDao;
    @Mock private CrfService crfService;
    @Mock private StudyGroupClassDAO studyGroupClassDao;
    @Mock private StudyGroupDAO studyGroupDao;
    @Mock private FilterDAO filterDao;

    // ─── Bean helpers ───────────────────────────────────────────

    private static DiscrepancyNoteBean createNoteBean(int id, String description, int eventCrfId) {
        DiscrepancyNoteBean b = new DiscrepancyNoteBean();
        b.setId(id);
        b.setDescription(description);
        b.setDetailedNotes("details");
        b.setDiscrepancyNoteTypeId(1);
        b.setResolutionStatusId(1);
        b.setDisType(null);      // avoid ResourceBundleProvider.getTermsBundle()
        b.setResStatus(null);    // avoid ResourceBundleProvider.getTermsBundle()
        b.setEntityType("item");
        b.setEventCRFId(eventCrfId);
        b.setParentDnId(0);
        b.setColumn("value");
        b.setCreatedDate(new java.util.Date());
        return b;
    }

    private static DatasetBean createDatasetBean(int id, String name, int studyId) {
        DatasetBean b = new DatasetBean();
        b.setId(id);
        b.setName(name);
        b.setStudyId(studyId);
        b.setCreatedDate(new java.util.Date());
        return b;
    }

    private static StudyGroupClassBean createGroupClassBean(int id, int studyId, String name) {
        StudyGroupClassBean b = new StudyGroupClassBean();
        b.setId(id);
        b.setStudyId(studyId);
        b.setName(name);
        b.setGroupClassTypeId(1);
        b.setSubjectAssignment("optimal");
        return b;
    }

    private static StudyGroupBean createGroupBean(int id, String name, int classId) {
        StudyGroupBean b = new StudyGroupBean();
        b.setId(id);
        b.setName(name);
        b.setDescription("desc");
        b.setStudyGroupClassId(classId);
        return b;
    }

    private static FilterBean createFilterBean(int id, String name) {
        FilterBean b = new FilterBean();
        b.setId(id);
        b.setName(name);
        b.setDescription("desc");
        b.setCreatedDate(new java.util.Date());
        return b;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Contract Tests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("LegacyStudyController — /api/legacy/studies")
    class StudyContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacyStudyController(studyService)).build();
        }

        private static StudySummaryDTO createStudySummaryDto(int id, String name) {
            StudySummaryDTO dto = new StudySummaryDTO();
            dto.setStudyId(id);
            dto.setName(name);
            dto.setUniqueIdentifier("ID-" + id);
            dto.setOcOid("S_OID_" + id);
            dto.setDateCreated(LocalDateTime.now());
            return dto;
        }

        @Test
        void listStudies_returns200() throws Exception {
            when(studyService.listStudies()).thenReturn(new ArrayList<>(List.of(
                    createStudySummaryDto(1, "Study A"),
                    createStudySummaryDto(2, "Study B"))));

            mockMvc.perform(get("/api/legacy/studies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Study A"));
        }

        @Test
        void getStudy_whenFound_returns200() throws Exception {
            StudyDetailDTO detail = new StudyDetailDTO();
            detail.setStudyId(1);
            detail.setName("Test Study");
            detail.setUniqueIdentifier("ID-1");
            detail.setOcOid("S_OID_1");
            when(studyService.getStudy(1)).thenReturn(detail);

            mockMvc.perform(get("/api/legacy/studies/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Test Study"));
        }

        @Test
        void getStudy_whenNotFound_returns404() throws Exception {
            when(studyService.getStudy(99)).thenThrow(
                    new java.util.NoSuchElementException("not found"));

            mockMvc.perform(get("/api/legacy/studies/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void searchStudies_returnsFiltered() throws Exception {
            when(studyService.searchByName("Clinical"))
                    .thenReturn(new ArrayList<>(List.of(
                            createStudySummaryDto(1, "Clinical Trial A"))));

            mockMvc.perform(get("/api/legacy/studies/search")
                            .param("query", "Clinical"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].name").value("Clinical Trial A"));
        }

        @Test
        void listSites_returns200() throws Exception {
            when(studyService.listSites(1))
                    .thenReturn(new ArrayList<>(List.of(
                            createStudySummaryDto(2, "Site X"))));

            mockMvc.perform(get("/api/legacy/studies/1/sites"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Site X"));
        }

        @Test
        void findByOid_whenFound_returns200() throws Exception {
            when(studyService.listStudies())
                    .thenReturn(new ArrayList<>(List.of(
                            createStudySummaryDto(1, "By OID"))));

            mockMvc.perform(get("/api/legacy/studies/by-oid")
                            .param("oid", "S_OID_1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("By OID"));
        }

        @Test
        void findByOid_whenNotFound_returns404() throws Exception {
            when(studyService.listStudies()).thenReturn(new ArrayList<>());

            mockMvc.perform(get("/api/legacy/studies/by-oid")
                            .param("oid", "NONEXISTENT"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("LegacySubjectController — /api/legacy/subjects")
    class SubjectContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacySubjectController(subjectService)).build();
        }

        private static StudySubjectDTO createStudySubjectDto(int id, int studyId, String label) {
            StudySubjectDTO dto = new StudySubjectDTO();
            dto.setStudySubjectId(id);
            dto.setStudyId(studyId);
            dto.setLabel(label);
            dto.setSecondaryLabel("sec-" + id);
            dto.setEnrollmentDate(LocalDateTime.now());
            return dto;
        }

        @Test
        void listSubjects_returns200() throws Exception {
            when(subjectService.listStudySubjects(1))
                    .thenReturn(new ArrayList<>(List.of(
                            createStudySubjectDto(1, 1, "SUBJ-001"))));

            mockMvc.perform(get("/api/legacy/subjects")
                            .param("studyId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].label").value("SUBJ-001"));
        }

        @Test
        void getSubject_whenFound_returns200() throws Exception {
            when(subjectService.getStudySubject(1))
                    .thenReturn(createStudySubjectDto(1, 1, "SUBJ-001"));

            mockMvc.perform(get("/api/legacy/subjects/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.label").value("SUBJ-001"));
        }

        @Test
        void getSubject_whenNotFound_returns404() throws Exception {
            when(subjectService.getStudySubject(99))
                    .thenThrow(new java.util.NoSuchElementException("not found"));

            mockMvc.perform(get("/api/legacy/subjects/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void searchByLabel_returnsFiltered() throws Exception {
            when(subjectService.listStudySubjects(1))
                    .thenReturn(new ArrayList<>(List.of(
                            createStudySubjectDto(1, 1, "SUBJ-001"),
                            createStudySubjectDto(2, 1, "SUBJ-002"))));

            mockMvc.perform(get("/api/legacy/subjects/by-label")
                            .param("studyId", "1")
                            .param("label", "001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    @Nested
    @DisplayName("LegacyRuleSetController — /api/legacy/rule-sets")
    class RuleSetContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacyRuleSetController(ruleService)).build();
        }

        private static RuleSetEntity createRuleSetEntity(int id) {
            RuleSetEntity e = new RuleSetEntity();
            e.setRuleSetId(id);
            e.setStudyId(1);
            e.setStudyEventDefinitionId(1);
            e.setStatusId(1);
            return e;
        }

        @Test
        void listRuleSets_withoutStudy_returnsAll() throws Exception {
            when(ruleService.listAllRuleSets()).thenReturn(
                    new ArrayList<>(List.of(createRuleSetEntity(1))));

            mockMvc.perform(get("/api/legacy/rule-sets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void listRuleSets_withStudyId_filters() throws Exception {
            when(ruleService.listRuleSetsByStudy(1)).thenReturn(
                    new ArrayList<>(List.of(createRuleSetEntity(1))));

            mockMvc.perform(get("/api/legacy/rule-sets")
                            .param("studyId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void getRuleSet_whenFound_returns200() throws Exception {
            when(ruleService.getRuleSet(1)).thenReturn(createRuleSetEntity(1));

            mockMvc.perform(get("/api/legacy/rule-sets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ruleSetId").value(1));
        }

        @Test
        void getRuleSet_whenNotFound_returns404() throws Exception {
            when(ruleService.getRuleSet(99))
                    .thenThrow(new java.util.NoSuchElementException("not found"));

            mockMvc.perform(get("/api/legacy/rule-sets/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @SuppressWarnings("unchecked")
    @Nested
    @DisplayName("LegacyDiscrepancyNoteController — /api/legacy/discrepancy-notes")
    class DiscrepancyNoteContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacyDiscrepancyNoteController(discrepancyNoteDao)).build();
        }

        @Test
        void listNotes_byEventCrf_returns200() throws Exception {
            ArrayList<DiscrepancyNoteBean> notes = new ArrayList<>();
            DiscrepancyNoteBean parent = createNoteBean(1, "Parent note", 100);
            parent.setParentDnId(0);
            notes.add(parent);
            notes.add(createNoteBean(2, "Child note", 100));
            notes.get(1).setParentDnId(1);
            when(discrepancyNoteDao.findAllParentItemNotesByEventCRF(100)).thenReturn(notes);

            mockMvc.perform(get("/api/legacy/discrepancy-notes")
                            .param("eventCrfId", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].description").value("Parent note"));
        }

        @Test
        void getNote_whenFound_returns200() throws Exception {
            when(discrepancyNoteDao.findByPK(1)).thenReturn(createNoteBean(1, "Note", 0));

            mockMvc.perform(get("/api/legacy/discrepancy-notes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Note"));
        }

        @Test
        void getNote_whenNotFound_returns404() throws Exception {
            DiscrepancyNoteBean empty = new DiscrepancyNoteBean();
            empty.setId(0);
            when(discrepancyNoteDao.findByPK(99)).thenReturn(empty);

            mockMvc.perform(get("/api/legacy/discrepancy-notes/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createNote_returns200() throws Exception {
            CreateDiscrepancyNoteRequest req = new CreateDiscrepancyNoteRequest();
            req.setDescription("New note");
            req.setDetailedNotes("details");
            req.setEntityType("item");
            req.setEntityId(10);
            req.setStudyId(1);
            req.setEventCrfId(100);

            DiscrepancyNoteBean created = createNoteBean(1, "New note", 100);
            when(discrepancyNoteDao.create(any(DiscrepancyNoteBean.class))).thenReturn(created);

            mockMvc.perform(post("/api/legacy/discrepancy-notes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("New note"));
        }

        @Test
        void resolveNote_returns200() throws Exception {
            DiscrepancyNoteBean note = createNoteBean(1, "To resolve", 0);
            when(discrepancyNoteDao.findByPK(1)).thenReturn(note);

            mockMvc.perform(patch("/api/legacy/discrepancy-notes/1/resolve"))
                    .andExpect(status().isOk());
        }

        @Test
        void resolveNote_whenNotFound_returns404() throws Exception {
            DiscrepancyNoteBean empty = new DiscrepancyNoteBean();
            empty.setId(0);
            when(discrepancyNoteDao.findByPK(99)).thenReturn(empty);

            mockMvc.perform(patch("/api/legacy/discrepancy-notes/99/resolve"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("LegacyDatasetController — /api/legacy/datasets")
    class DatasetContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacyDatasetController(datasetDao)).build();
        }

        @Test
        void listDatasets_withStudyId_returns200() throws Exception {
            when(datasetDao.findAllByStudyId(1)).thenReturn(new ArrayList<>(List.of(createDatasetBean(1, "DS-1", 1))));

            mockMvc.perform(get("/api/legacy/datasets")
                            .param("studyId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("DS-1"));
        }

        @Test
        void getDataset_whenFound_returns200() throws Exception {
            when(datasetDao.findByPK(1)).thenReturn(createDatasetBean(1, "DS", 1));

            mockMvc.perform(get("/api/legacy/datasets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("DS"));
        }

        @Test
        void createDataset_returns200() throws Exception {
            when(datasetDao.create(any(DatasetBean.class))).thenAnswer(i -> {
                DatasetBean b = i.getArgument(0);
                b.setId(1);
                return b;
            });

            mockMvc.perform(post("/api/legacy/datasets")
                            .param("name", "New Dataset")
                            .param("studyId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Dataset"));
        }
    }

    @Nested
    @DisplayName("LegacyCrfManageController — /api/legacy/crfs")
    class CrfManageContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacyCrfManageController(crfService)).build();
        }

        private static CrfEntity createCrfEntity(int id, String name) {
            CrfEntity e = new CrfEntity();
            e.setCrfId(id);
            e.setName(name);
            e.setDescription("desc");
            e.setOcOid("CRF_OID_" + id);
            e.setStatusId(1);
            return e;
        }

        private static CrfVersionEntity createCrfVersionEntity(int id, int crfId, String name) {
            CrfVersionEntity e = new CrfVersionEntity();
            e.setCrfVersionId(id);
            e.setCrfId(crfId);
            e.setName(name);
            e.setDescription("desc");
            e.setRevisionNotes("");
            e.setStatusId(1);
            return e;
        }

        @Test
        void listCrfs_returns200() throws Exception {
            when(crfService.getAllCrfEntities()).thenReturn(
                    new ArrayList<>(List.of(createCrfEntity(1, "CRF-1"))));

            mockMvc.perform(get("/api/legacy/crfs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("CRF-1"));
        }

        @Test
        void getCrf_whenFound_returns200() throws Exception {
            when(crfService.getCrfEntity(1)).thenReturn(createCrfEntity(1, "CRF-1"));

            mockMvc.perform(get("/api/legacy/crfs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("CRF-1"));
        }

        @Test
        void getCrf_whenNotFound_returns404() throws Exception {
            when(crfService.getCrfEntity(99)).thenThrow(
                    new java.util.NoSuchElementException("not found"));

            mockMvc.perform(get("/api/legacy/crfs/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createCrf_returns200() throws Exception {
            when(crfService.createCrf(eq("New CRF"), eq("desc"), eq(1)))
                    .thenReturn(createCrfEntity(1, "New CRF"));

            CreateCrfRequest req = new CreateCrfRequest();
            req.setName("New CRF");
            req.setDescription("desc");

            mockMvc.perform(post("/api/legacy/crfs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New CRF"));
        }

        @Test
        void updateCrf_returns200() throws Exception {
            when(crfService.updateCrf(eq(1), eq("Updated"), any()))
                    .thenReturn(createCrfEntity(1, "Updated"));

            CreateCrfRequest req = new CreateCrfRequest();
            req.setName("Updated");

            mockMvc.perform(put("/api/legacy/crfs/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        @Test
        void listVersions_returns200() throws Exception {
            when(crfService.listVersionEntities(1)).thenReturn(
                    List.of(createCrfVersionEntity(1, 1, "v1.0")));

            mockMvc.perform(get("/api/legacy/crfs/1/versions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("v1.0"));
        }

        @Test
        void getVersion_whenFound_returns200() throws Exception {
            when(crfService.getCrfVersionEntity(1))
                    .thenReturn(createCrfVersionEntity(1, 1, "v1.0"));

            mockMvc.perform(get("/api/legacy/crfs/versions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("v1.0"));
        }

        @Test
        void getVersion_whenNotFound_returns404() throws Exception {
            when(crfService.getCrfVersionEntity(99))
                    .thenThrow(new java.util.NoSuchElementException("not found"));

            mockMvc.perform(get("/api/legacy/crfs/versions/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createVersion_returns200() throws Exception {
            when(crfService.createVersion(eq(1), eq("v2.0"), any(), any(), eq(1)))
                    .thenReturn(createCrfVersionEntity(1, 1, "v2.0"));

            var request = new java.util.HashMap<String, String>();
            request.put("name", "v2.0");

            mockMvc.perform(post("/api/legacy/crfs/1/versions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("v2.0"));
        }

        @Test
        void deleteVersion_returns204() throws Exception {
            mockMvc.perform(delete("/api/legacy/crfs/versions/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteVersion_whenNotFound_returns404() throws Exception {
            doThrow(new java.util.NoSuchElementException("not found"))
                    .when(crfService).deleteVersion(99);

            mockMvc.perform(delete("/api/legacy/crfs/versions/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("LegacySubjectGroupController — /api/legacy/subject-groups")
    class SubjectGroupContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacySubjectGroupController(studyGroupClassDao, studyGroupDao)).build();
        }

        @Test
        void listClasses_returns200() throws Exception {
            when(studyGroupClassDao.findAllByStudy(any())).thenReturn(
                    new ArrayList<>(List.of(createGroupClassBean(1, 1, "Class A"))));

            mockMvc.perform(get("/api/legacy/subject-groups/classes")
                            .param("studyId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Class A"));
        }

        @Test
        void getClass_whenFound_returns200() throws Exception {
            StudyGroupClassBean bean = createGroupClassBean(1, 1, "Class A");
            when(studyGroupClassDao.findByPK(1)).thenReturn(bean);

            mockMvc.perform(get("/api/legacy/subject-groups/classes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Class A"));
        }

        @Test
        void createClass_returns200() throws Exception {
            when(studyGroupClassDao.create(any(StudyGroupClassBean.class))).thenAnswer(i -> {
                StudyGroupClassBean b = i.getArgument(0);
                b.setId(1);
                return b;
            });

            SubjectGroupClassDTO dto = new SubjectGroupClassDTO();
            dto.setName("New Class");
            dto.setStudyId(1);
            dto.setSubjectAssignment("optimal");

            mockMvc.perform(post("/api/legacy/subject-groups/classes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Class"));
        }

        @Test
        void updateClass_returns200() throws Exception {
            when(studyGroupClassDao.findByPK(1)).thenReturn(createGroupClassBean(1, 1, "Old"));

            SubjectGroupClassDTO dto = new SubjectGroupClassDTO();
            dto.setName("Updated");
            dto.setSubjectAssignment("optimal");

            mockMvc.perform(put("/api/legacy/subject-groups/classes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated"));
        }

        @Test
        void listGroups_returns200() throws Exception {
            when(studyGroupClassDao.findByPK(1)).thenReturn(createGroupClassBean(1, 1, "C1"));
            when(studyGroupDao.findAllByGroupClass(any())).thenReturn(
                    new ArrayList<>(List.of(createGroupBean(1, "Group A", 1))));

            mockMvc.perform(get("/api/legacy/subject-groups/classes/1/groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Group A"));
        }

        @Test
        void createGroup_returns200() throws Exception {
            when(studyGroupDao.create(any(StudyGroupBean.class))).thenAnswer(i -> {
                StudyGroupBean b = i.getArgument(0);
                b.setId(1);
                return b;
            });

            SubjectGroupDTO dto = new SubjectGroupDTO();
            dto.setName("New Group");
            dto.setDescription("desc");

            mockMvc.perform(post("/api/legacy/subject-groups/classes/1/groups")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Group"));
        }

        @Test
        void updateGroup_returns200() throws Exception {
            when(studyGroupDao.findByPK(1)).thenReturn(createGroupBean(1, "Old", 1));

            SubjectGroupDTO dto = new SubjectGroupDTO();
            dto.setName("Updated");

            mockMvc.perform(put("/api/legacy/subject-groups/groups/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated"));
        }
    }

    @Nested
    @DisplayName("LegacyFilterController — /api/legacy/filters")
    class FilterContractTest {

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            mockMvc = MockMvcBuilders.standaloneSetup(
                    new LegacyFilterController(filterDao)).build();
        }

        @Test
        void listFilters_returns200() throws Exception {
            when(filterDao.findAll()).thenReturn(new ArrayList<>(List.of(createFilterBean(1, "Filter A"))));

            mockMvc.perform(get("/api/legacy/filters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Filter A"));
        }

        @Test
        void getFilter_whenFound_returns200() throws Exception {
            when(filterDao.findByPK(1)).thenReturn(createFilterBean(1, "Filter A"));

            mockMvc.perform(get("/api/legacy/filters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Filter A"));
        }

        @Test
        void createFilter_returns200() throws Exception {
            when(filterDao.create(any(FilterBean.class))).thenAnswer(i -> {
                FilterBean b = i.getArgument(0);
                b.setId(1);
                return b;
            });

            var request = new java.util.HashMap<String, String>();
            request.put("name", "New Filter");

            mockMvc.perform(post("/api/legacy/filters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Filter"));
        }
    }
}
