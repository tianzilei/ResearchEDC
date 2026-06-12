package org.researchedc.module.datacapture.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.datacapture.dto.BatchSaveItemsRequest;
import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO;
import org.researchedc.module.datacapture.dto.SaveItemDataRequest;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.internal.adapter.AttachmentStorageAdapter;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.entity.ResponseSetEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupMetadataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;
import org.researchedc.module.datacapture.repository.ResponseSetRepository;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock private RuleSetRepository ruleSetRepository;
    @Mock private RuleSetRuleRepository ruleSetRuleRepository;
    @Mock private RuleRepository ruleRepository;
    @Mock private RuleExpressionRepository ruleExpressionRepository;

    private DataCaptureService service;

    @BeforeEach
    void setUp() {
        service = new DataCaptureService(itemDataRepository,
                responseSetRepository, itemGroupRepository, itemGroupMetadataRepository, auditService,
                attachmentStorageAdapter,
                eventCrfRepository, studyEventRepository, studySubjectRepository,
                crfVersionRepository, ruleSetRepository, ruleSetRuleRepository,
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
        when(itemDataRepository.findByEventCrfIdAndItemId(100, 10))
                .thenReturn(List.of());
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
        verify(auditService).recordAudit(any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any());
    }

    @Test
    void saveItemData_whenExisting_updatesAndReturns() {
        ItemDataEntity existing = createItemData(1, 100, 10, "Old");
        when(itemDataRepository.findByEventCrfIdAndItemId(100, 10))
                .thenReturn(List.of(existing));
        when(itemDataRepository.save(any(ItemDataEntity.class)))
                .thenAnswer(i -> i.getArgument(0));

        SaveItemDataRequest request = new SaveItemDataRequest();
        request.setEventCrfId(100);
        request.setItemId(10);
        request.setValue("Updated");

        ItemDataDTO result = service.saveItemData(request, 42);

        assertEquals("Updated", result.getValue());
        verify(auditService).recordAudit(any(), eq(org.researchedc.module.audit.enums.AuditEventType.UPDATE),
                any(), any(), any(), any(), eq("Updated"), any(), any(), any());
    }

    @Test
    void batchSaveItems_savesAllItems() {
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
