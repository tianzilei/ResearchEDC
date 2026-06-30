package org.researchedc.module.datacapture.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.datacapture.dto.BatchSaveItemsRequest;
import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO;
import org.researchedc.module.datacapture.dto.SaveItemDataRequest;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.internal.adapter.AttachmentStorageAdapter;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.entity.ItemGroupMetadataEntity;
import org.researchedc.module.datacapture.entity.ResponseSetEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupMetadataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;
import org.researchedc.module.datacapture.repository.ResponseSetRepository;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class DataCaptureServiceTest {

    @Mock private ItemDataRepository itemDataRepository;
    @Mock private ResponseSetRepository responseSetRepository;
    @Mock private ItemGroupRepository itemGroupRepository;
    @Mock private ItemGroupMetadataRepository itemGroupMetadataRepository;
    @Mock private AuditService auditService;
    @Mock private AttachmentStorageAdapter attachmentStorageAdapter;
    @Mock private EventCrfRepository eventCrfRepository;
    @Mock private StudyEventRepository studyEventRepository;
    @Mock private StudySubjectRepository studySubjectRepository;
    @Mock private CrfVersionRepository crfVersionRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ItemFormMetadataRepository itemFormMetadataRepository;
    @Mock private RuleSetRepository ruleSetRepository;
    @Mock private RuleSetRuleRepository ruleSetRuleRepository;
    @Mock private RuleRepository ruleRepository;
    @Mock private RuleExpressionRepository ruleExpressionRepository;

    @TempDir
    Path tempDir;

    private DataCaptureService service;

    @BeforeEach
    void setUp() {
        service = new DataCaptureService(itemDataRepository,
                responseSetRepository, itemGroupRepository, itemGroupMetadataRepository, auditService,
                attachmentStorageAdapter,
                eventCrfRepository, studyEventRepository, studySubjectRepository,
                crfVersionRepository, itemRepository, itemFormMetadataRepository,
                ruleSetRepository, ruleSetRuleRepository,
                ruleRepository, ruleExpressionRepository);
    }

    private static ItemDataEntity createItemData(Integer id, Integer eventCrfId,
                                                  Integer itemId, String value) {
        ItemDataEntity e = new ItemDataEntity();
        e.setItemDataId(id);
        e.setEventCrfId(eventCrfId);
        e.setItemId(itemId);
        e.setValue(value);
        e.setStatusId(1);
        e.setDeleted(false);
        e.setDateCreated(LocalDateTime.now());
        return e;
    }

    private static ResponseSetEntity createResponseSet(Integer id, String label,
                                                        String text, String values) {
        ResponseSetEntity e = new ResponseSetEntity();
        e.setResponseSetId(id);
        e.setResponseTypeId(1);
        e.setLabel(label);
        e.setOptionsText(text);
        e.setOptionsValues(values);
        return e;
    }

    private static ItemGroupEntity createItemGroup(Integer id, Integer crfId, String name) {
        ItemGroupEntity e = new ItemGroupEntity();
        e.setItemGroupId(id);
        e.setCrfId(crfId);
        e.setName(name);
        return e;
    }

    private static EventCrfEntity createEventCrf(Integer id, Integer studySubjectId) {
        EventCrfEntity e = new EventCrfEntity();
        e.setEventCrfId(id);
        e.setStudySubjectId(studySubjectId);
        e.setCrfVersionId(300);
        return e;
    }

    private static EventCrfEntity createEventCrf(Integer id, Integer studySubjectId, Integer statusId) {
        EventCrfEntity e = createEventCrf(id, studySubjectId);
        e.setStatusId(statusId);
        return e;
    }

    private static StudySubjectEntity createStudySubject(Integer id, Integer studyId) {
        StudySubjectEntity e = new StudySubjectEntity();
        e.setStudySubjectId(id);
        e.setStudyId(studyId);
        return e;
    }

    private static ItemGroupMetadataEntity createItemGroupMetadata(Integer itemId, Integer crfVersionId) {
        ItemGroupMetadataEntity e = new ItemGroupMetadataEntity();
        e.setItemGroupMetadataId(1);
        e.setItemId(itemId);
        e.setCrfVersionId(crfVersionId);
        return e;
    }

    private static ItemFormMetadataEntity createItemFormMetadata(Integer itemId, Integer crfVersionId) {
        ItemFormMetadataEntity e = new ItemFormMetadataEntity();
        e.setItemFormMetadataId(1);
        e.setItemId(itemId);
        e.setCrfVersionId(crfVersionId);
        return e;
    }

    private static ItemEntity createItem(Integer itemId, Integer dataTypeId) {
        ItemEntity e = new ItemEntity();
        e.setItemId(itemId);
        e.setItemDataTypeId(dataTypeId);
        return e;
    }

    private void mockWritableItem(Integer itemId, Integer dataTypeId) {
        when(itemGroupMetadataRepository.findByItemIdAndCrfVersionId(itemId, 300))
                .thenReturn(List.of(createItemGroupMetadata(itemId, 300)));
        when(itemFormMetadataRepository.findByItemIdAndCrfVersionId(itemId, 300))
                .thenReturn(List.of(createItemFormMetadata(itemId, 300)));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(createItem(itemId, dataTypeId)));
    }

    @Test
    void getItemDataByEventCrf_returnsData() {
        when(itemDataRepository.findByEventCrfIdOrderByItemId(100))
                .thenReturn(List.of(
                        createItemData(1, 100, 10, "Yes"),
                        createItemData(2, 100, 11, "No")));

        List<ItemDataDTO> result = service.getItemDataByEventCrf(100);

        assertEquals(2, result.size());
        assertEquals("Yes", result.get(0).getValue());
        assertEquals("No", result.get(1).getValue());
    }

    @Test
    void getResponseSet_whenFound_returnsDto() {
        when(responseSetRepository.findById(1))
                .thenReturn(Optional.of(
                        createResponseSet(1, "Gender", "Male\\nFemale", "1\\n2")));

        ResponseSetDTO result = service.getResponseSet(1);

        assertEquals("Gender", result.getLabel());
        assertEquals(2, result.getOptions().size());
        assertEquals("Male", result.getOptions().get(0).getText());
        assertEquals("1", result.getOptions().get(0).getValue());
    }

    @Test
    void getResponseSet_whenNotFound_throwsException() {
        when(responseSetRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> service.getResponseSet(99));
    }

    @Test
    void getItemGroupsByCrf_returnsGroups() {
        when(itemGroupRepository.findByCrfId(5))
                .thenReturn(List.of(
                        createItemGroup(1, 5, "Vitals"),
                        createItemGroup(2, 5, "Lab Results")));

        List<ItemGroupDTO> result = service.getItemGroupsByCrf(5);

        assertEquals(2, result.size());
        assertEquals("Vitals", result.get(0).getName());
    }

    @Test
    void saveItemData_whenNew_createsAndReturns() {
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        mockWritableItem(10, 5);
        when(itemDataRepository.findByEventCrfIdAndItemId(100, 10))
                .thenReturn(List.of());
        when(studySubjectRepository.findById(200)).thenReturn(Optional.of(createStudySubject(200, 11)));
        when(itemDataRepository.save(any(ItemDataEntity.class)))
                .thenAnswer(i -> {
                    ItemDataEntity e = i.getArgument(0);
                    if (e.getItemDataId() == null) e.setItemDataId(1);
                    return e;
                });

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("Yes");

        ItemDataDTO result = service.saveItemData(request, 42);

        assertEquals("Yes", result.getValue());
        assertEquals(1, result.getStatusId());
        verify(itemDataRepository).save(any(ItemDataEntity.class));
        verify(auditService).recordAudit(eq(11), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void saveItemData_whenExisting_updatesAndReturns() {
        ItemDataEntity existing = createItemData(1, 100, 10, "Old");
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        mockWritableItem(10, 5);
        when(itemDataRepository.findByEventCrfIdAndItemId(100, 10))
                .thenReturn(List.of(existing));
        when(studySubjectRepository.findById(200)).thenReturn(Optional.of(createStudySubject(200, 11)));
        when(itemDataRepository.save(any(ItemDataEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("Updated");

        ItemDataDTO result = service.saveItemData(request, 42);

        assertEquals("Updated", result.getValue());
        verify(auditService).recordAudit(eq(11), eq(org.researchedc.module.audit.enums.AuditEventType.UPDATE),
                any(), any(), any(), any(), eq("Updated"), any(), any(), any());
    }

    @Test
    void saveItemData_whenItemNotInEventCrfVersion_rejectsSave() {
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        when(itemGroupMetadataRepository.findByItemIdAndCrfVersionId(10, 300))
                .thenReturn(List.of());

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("Unexpected");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveItemData(request, 42));
        verify(itemDataRepository, never()).save(any());
    }

    @Test
    void saveItemData_whenEventCrfLocked_rejectsSave() {
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200, 6)));

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("Unexpected");

        assertThrows(IllegalStateException.class,
                () -> service.saveItemData(request, 42));
        verify(itemDataRepository, never()).save(any());
    }

    @Test
    void saveItemData_whenRequiredItemBlank_rejectsSave() {
        ItemFormMetadataEntity metadata = createItemFormMetadata(10, 300);
        metadata.setRequired(true);
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        when(itemGroupMetadataRepository.findByItemIdAndCrfVersionId(10, 300))
                .thenReturn(List.of(createItemGroupMetadata(10, 300)));
        when(itemFormMetadataRepository.findByItemIdAndCrfVersionId(10, 300))
                .thenReturn(List.of(metadata));

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue(" ");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveItemData(request, 42));
        verify(itemDataRepository, never()).save(any());
    }

    @Test
    void saveItemData_whenRegexpDoesNotMatch_rejectsSave() {
        ItemFormMetadataEntity metadata = createItemFormMetadata(10, 300);
        metadata.setRegexp("[A-Z]{3}");
        metadata.setRegexpErrorMsg("Use three uppercase letters");
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        when(itemGroupMetadataRepository.findByItemIdAndCrfVersionId(10, 300))
                .thenReturn(List.of(createItemGroupMetadata(10, 300)));
        when(itemFormMetadataRepository.findByItemIdAndCrfVersionId(10, 300))
                .thenReturn(List.of(metadata));

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("abc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.saveItemData(request, 42));
        assertEquals("Use three uppercase letters", ex.getMessage());
        verify(itemDataRepository, never()).save(any());
    }

    @Test
    void saveItemData_whenIntegerItemReceivesText_rejectsSave() {
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        mockWritableItem(10, 6);

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("ten");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveItemData(request, 42));
        verify(itemDataRepository, never()).save(any());
    }

    @Test
    void saveItemData_whenDateItemReceivesInvalidDate_rejectsSave() {
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        mockWritableItem(10, 9);

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("2026-02-31");

        assertThrows(IllegalArgumentException.class,
                () -> service.saveItemData(request, 42));
        verify(itemDataRepository, never()).save(any());
    }

    @Test
    void batchSaveItems_savesAllItems() {
        when(eventCrfRepository.findById(100)).thenReturn(Optional.of(createEventCrf(100, 200)));
        when(itemGroupMetadataRepository.findByItemIdAndCrfVersionId(anyInt(), eq(300)))
                .thenAnswer(invocation -> List.of(createItemGroupMetadata(invocation.getArgument(0), 300)));
        when(itemFormMetadataRepository.findByItemIdAndCrfVersionId(anyInt(), eq(300)))
                .thenAnswer(invocation -> List.of(createItemFormMetadata(invocation.getArgument(0), 300)));
        when(itemRepository.findById(anyInt()))
                .thenAnswer(invocation -> Optional.of(createItem(invocation.getArgument(0), 5)));
        when(itemDataRepository.findByEventCrfIdAndItemId(anyInt(), anyInt()))
                .thenReturn(List.of());
        when(itemDataRepository.save(any(ItemDataEntity.class)))
                .thenAnswer(i -> {
                    ItemDataEntity e = i.getArgument(0);
                    if (e.getItemDataId() == null) e.setItemDataId(1);
                    return e;
                });

        SaveItemDataRequest item1 = new SaveItemDataRequest();
        item1.setItemId(10);
        item1.setValue("A");

        SaveItemDataRequest item2 = new SaveItemDataRequest();
        item2.setItemId(11);
        item2.setValue("B");

        BatchSaveItemsRequest batch = new BatchSaveItemsRequest();
        batch.setEventCrfId(100);
        batch.setItems(List.of(item1, item2));

        List<ItemDataDTO> results = service.batchSaveItems(batch, 42);

        assertEquals(2, results.size());
        verify(itemDataRepository, times(2)).save(any(ItemDataEntity.class));
    }


    @Test
    void listAttachmentsByEventCrf_whenAuthorized_returnsOpaqueAttachmentIds() throws Exception {
        Path file = tempDir.resolve("lab.pdf");
        Files.writeString(file, "pdf-data");
        when(attachmentStorageAdapter.canViewEventCrfData(100, 42)).thenReturn(true);
        when(attachmentStorageAdapter.getStudyOidByEventCrf(100)).thenReturn("S_STUDY");
        when(attachmentStorageAdapter.getCandidateStudyOids("S_STUDY")).thenReturn(List.of("S_STUDY"));
        when(attachmentStorageAdapter.studyDirectory("S_STUDY")).thenReturn(tempDir.toFile());

        var attachments = service.listAttachmentsByEventCrf(100, 42);

        assertEquals(1, attachments.size());
        assertEquals("lab.pdf", attachments.getFirst().fileName());
        assertEquals(8L, attachments.getFirst().size());
        assertEquals(attachmentId("lab.pdf"), attachments.getFirst().id());
    }

    @Test
    void listAttachmentsByEventCrf_whenUnauthorized_throwsAccessDenied() {
        when(attachmentStorageAdapter.canViewEventCrfData(100, 7)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.listAttachmentsByEventCrf(100, 7));
    }

    @Test
    void downloadAttachmentByEventCrf_whenAuthorized_streamsResolvedFile() throws Exception {
        Path file = tempDir.resolve("lab.pdf");
        Files.writeString(file, "pdf-data");
        when(attachmentStorageAdapter.canViewEventCrfData(100, 42)).thenReturn(true);
        when(attachmentStorageAdapter.getStudyOidByEventCrf(100)).thenReturn("S_STUDY");
        when(attachmentStorageAdapter.getCandidateStudyOids("S_STUDY")).thenReturn(List.of("S_STUDY"));
        when(attachmentStorageAdapter.resolveAttachmentFile("lab.pdf", "S_STUDY")).thenReturn(file.toFile());
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.downloadAttachmentByEventCrf(100, attachmentId("lab.pdf"), 42, response);

        assertEquals(200, response.getStatus());
        assertEquals("pdf-data", response.getContentAsString());
        assertEquals("attachment; filename=\"lab.pdf\"", response.getHeader("Content-Disposition"));
    }

    @Test
    void downloadAttachmentByEventCrf_whenUnauthorized_returnsForbidden() {
        when(attachmentStorageAdapter.canViewEventCrfData(100, 7)).thenReturn(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.downloadAttachmentByEventCrf(100, attachmentId("lab.pdf"), 7, response);

        assertEquals(403, response.getStatus());
        verify(attachmentStorageAdapter, never()).getStudyOidByEventCrf(anyInt());
    }

    @Test
    void downloadAttachmentByEventCrf_whenAttachmentIdDecodesToTraversal_returnsBadRequest() {
        when(attachmentStorageAdapter.canViewEventCrfData(100, 42)).thenReturn(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.downloadAttachmentByEventCrf(100, attachmentId("../secret.txt"), 42, response);

        assertEquals(400, response.getStatus());
        verify(attachmentStorageAdapter, never()).getStudyOidByEventCrf(anyInt());
    }

    @Test
    void downloadAttachmentByEventCrf_whenMissingFile_returnsNotFound() {
        File missing = tempDir.resolve("missing.pdf").toFile();
        when(attachmentStorageAdapter.canViewEventCrfData(100, 42)).thenReturn(true);
        when(attachmentStorageAdapter.getStudyOidByEventCrf(100)).thenReturn("S_STUDY");
        when(attachmentStorageAdapter.getCandidateStudyOids("S_STUDY")).thenReturn(List.of("S_STUDY"));
        when(attachmentStorageAdapter.resolveAttachmentFile("missing.pdf", "S_STUDY")).thenReturn(missing);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.downloadAttachmentByEventCrf(100, attachmentId("missing.pdf"), 42, response);

        assertEquals(404, response.getStatus());
    }

    @Test
    void uploadAttachment_whenUnauthorized_throwsAccessDenied() {
        MockMultipartFile file = new MockMultipartFile("file", "lab.pdf", "application/pdf", "data".getBytes(StandardCharsets.UTF_8));
        when(attachmentStorageAdapter.canViewEventCrfData(100, 7)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.uploadAttachment(100, file, 7));
    }

    @Test
    void uploadAttachment_whenFilenameContainsTraversal_rejectsUpload() {
        MockMultipartFile file = new MockMultipartFile("file", "../lab.pdf", "application/pdf", "data".getBytes(StandardCharsets.UTF_8));
        when(attachmentStorageAdapter.canViewEventCrfData(100, 42)).thenReturn(true);
        when(attachmentStorageAdapter.getStudyOidByEventCrf(100)).thenReturn("S_STUDY");

        assertThrows(java.io.IOException.class,
                () -> service.uploadAttachment(100, file, 42));
    }

    @Test
    void uploadAttachment_whenAuthorized_writesToStudyDirectory() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "lab.pdf", "application/pdf", "data".getBytes(StandardCharsets.UTF_8));
        when(attachmentStorageAdapter.canViewEventCrfData(100, 42)).thenReturn(true);
        when(attachmentStorageAdapter.getStudyOidByEventCrf(100)).thenReturn("S_STUDY");
        when(attachmentStorageAdapter.studyDirectory("S_STUDY")).thenReturn(tempDir.toFile());

        service.uploadAttachment(100, file, 42);

        assertEquals("data", Files.readString(tempDir.resolve("lab.pdf")));
    }

    private static String attachmentId(String fileName) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void parseOptions_parsesNewlineSeparated() {
        ResponseSetEntity entity = createResponseSet(1, "Test", "Opt1\\nOpt2", "1\\n2");
        when(responseSetRepository.findById(1)).thenReturn(Optional.of(entity));

        ResponseSetDTO result = service.getResponseSet(1);

        assertEquals(2, result.getOptions().size());
        assertEquals("Opt1", result.getOptions().get(0).getText());
        assertEquals("1", result.getOptions().get(0).getValue());
    }

    @Test
    void parseOptions_whenNull_returnsEmpty() {
        ResponseSetEntity entity = createResponseSet(1, "Empty", null, null);
        when(responseSetRepository.findById(1)).thenReturn(Optional.of(entity));

        ResponseSetDTO result = service.getResponseSet(1);

        assertTrue(result.getOptions().isEmpty());
    }
}
