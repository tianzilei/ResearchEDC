package org.researchedc.module.datacapture.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.researchedc.app.dto.Status;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.module.datacapture.dto.AttachmentDTO;
import org.researchedc.module.datacapture.dto.BatchSaveItemsRequest;
import org.researchedc.module.crf.entity.CrfVersionEntity;
import org.researchedc.module.crf.repository.CrfVersionRepository;
import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO.OptionDTO;
import org.researchedc.module.datacapture.dto.RuleEvalResponse;
import org.researchedc.module.datacapture.dto.RuleEvalResponse.RuleInfo;
import org.researchedc.module.datacapture.dto.SaveItemDataRequest;
import org.researchedc.module.event.entity.EventCrfEntity;
import org.researchedc.module.event.entity.StudyEventEntity;
import org.researchedc.module.event.repository.EventCrfRepository;
import org.researchedc.module.event.repository.StudyEventRepository;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.researchedc.module.subject.entity.StudySubjectEntity;
import org.researchedc.module.subject.repository.StudySubjectRepository;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.entity.ItemGroupMetadataEntity;
import org.researchedc.module.datacapture.entity.ResponseSetEntity;
import org.researchedc.module.datacapture.internal.adapter.AttachmentStorageAdapter;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupMetadataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;
import org.researchedc.module.datacapture.repository.ResponseSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class DataCaptureService {

    private static final int ITEM_DATA_TYPE_STRING = 5;
    private static final int ITEM_DATA_TYPE_INTEGER = 6;
    private static final int ITEM_DATA_TYPE_REAL = 7;
    private static final int ITEM_DATA_TYPE_DATE = 9;
    private static final int ITEM_DATA_TYPE_PARTIAL_DATE = 10;
    private static final int RESPONSE_TYPE_CHECKBOX = 3;
    private static final int RESPONSE_TYPE_RADIO = 5;
    private static final int RESPONSE_TYPE_SELECT = 6;
    private static final int RESPONSE_TYPE_SELECT_MULTI = 7;
    private static final String FULL_DATE_FORMAT = "yyyy-MM-dd";
    private static final String PARTIAL_DATE_YEAR_MONTH_FORMAT = "yyyy-MM";
    private static final String PARTIAL_DATE_YEAR_FORMAT = "yyyy";
    private static final Pattern FULL_DATE_PATTERN = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}");
    private static final Pattern PARTIAL_DATE_YEAR_MONTH_PATTERN = Pattern.compile("\\d{4}-\\d{1,2}");
    private static final Pattern PARTIAL_DATE_YEAR_PATTERN = Pattern.compile("\\d{4}");

    private final ItemDataRepository itemDataRepository;
    private final ResponseSetRepository responseSetRepository;
    private final ItemGroupRepository itemGroupRepository;
    private final ItemGroupMetadataRepository itemGroupMetadataRepository;
    private final AuditService auditService;
    private final AttachmentStorageAdapter attachmentStorageAdapter;
    private final EventCrfRepository eventCrfRepository;
    private final StudyEventRepository studyEventRepository;
    private final StudySubjectRepository studySubjectRepository;
    private final CrfVersionRepository crfVersionRepository;
    private final ItemRepository itemRepository;
    private final ItemFormMetadataRepository itemFormMetadataRepository;
    private final RuleSetRepository ruleSetRepository;
    private final RuleSetRuleRepository ruleSetRuleRepository;
    private final RuleRepository ruleRepository;
    private final RuleExpressionRepository ruleExpressionRepository;

    public DataCaptureService(ItemDataRepository itemDataRepository,
                               ResponseSetRepository responseSetRepository,
                               ItemGroupRepository itemGroupRepository,
                               ItemGroupMetadataRepository itemGroupMetadataRepository,
                               AuditService auditService,
                               AttachmentStorageAdapter attachmentStorageAdapter,
                                EventCrfRepository eventCrfRepository,
                                StudyEventRepository studyEventRepository,
                                StudySubjectRepository studySubjectRepository,
                                CrfVersionRepository crfVersionRepository,
                                ItemRepository itemRepository,
                                ItemFormMetadataRepository itemFormMetadataRepository,
                                RuleSetRepository ruleSetRepository,
                                RuleSetRuleRepository ruleSetRuleRepository,
                                RuleRepository ruleRepository,
                                RuleExpressionRepository ruleExpressionRepository) {
        this.itemDataRepository = itemDataRepository;
        this.responseSetRepository = responseSetRepository;
        this.itemGroupRepository = itemGroupRepository;
        this.itemGroupMetadataRepository = itemGroupMetadataRepository;
        this.auditService = auditService;
        this.attachmentStorageAdapter = attachmentStorageAdapter;
        this.eventCrfRepository = eventCrfRepository;
        this.studyEventRepository = studyEventRepository;
        this.studySubjectRepository = studySubjectRepository;
        this.crfVersionRepository = crfVersionRepository;
        this.itemRepository = itemRepository;
        this.itemFormMetadataRepository = itemFormMetadataRepository;
        this.ruleSetRepository = ruleSetRepository;
        this.ruleSetRuleRepository = ruleSetRuleRepository;
        this.ruleRepository = ruleRepository;
        this.ruleExpressionRepository = ruleExpressionRepository;
    }

    public List<ItemDataDTO> getItemDataByEventCrf(Integer eventCrfId) {
        return itemDataRepository.findByEventCrfIdOrderByItemId(eventCrfId)
            .stream()
            .map(this::toItemDataDto)
            .toList();
    }

    public ResponseSetDTO getResponseSet(Integer responseSetId) {
        ResponseSetEntity entity = responseSetRepository.findById(responseSetId)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "ResponseSet not found: " + responseSetId));
        return toResponseSetDto(entity);
    }

    public List<ItemGroupDTO> getItemGroupsByCrf(Integer crfId) {
        return itemGroupRepository.findByCrfId(crfId)
            .stream()
            .map(this::toItemGroupDto)
            .toList();
    }

    public List<ItemGroupDTO> getItemGroupsByCrfVersion(Integer crfVersionId) {
        List<ItemGroupEntity> groups = itemGroupRepository.findOnlyGroupsByCRFVersionIdNative(crfVersionId);
        return groups.stream()
            .map(g -> {
                ItemGroupDTO dto = toItemGroupDto(g);
                List<Integer> itemIds = itemGroupMetadataRepository
                    .findByItemGroupIdAndCrfVersionId(g.getItemGroupId(), crfVersionId)
                    .stream()
                    .map(ItemGroupMetadataEntity::getItemId)
                    .distinct()
                    .toList();
                dto.setItems(itemIds);
                return dto;
            })
            .toList();
    }

    @Transactional
    public ItemDataDTO saveItemData(SaveItemDataRequest request, Integer userId) {
        validateItemSave(request);

        List<ItemDataEntity> existing;
        if (request.getOrdinal() != null) {
            existing = itemDataRepository.findByItemIdAndEventCrfIdAndOrdinal(
                request.getItemId(), request.getEventCrfId(), request.getOrdinal());
        } else {
            existing = itemDataRepository.findByEventCrfIdAndItemId(
                request.getEventCrfId(), request.getItemId());
        }

        ItemDataEntity entity;
        boolean isUpdate;
        if (!existing.isEmpty()) {
            entity = existing.getFirst();
            isUpdate = true;
            entity.setValue(request.getValue());
            if (request.getStatusId() != null) {
                entity.setStatusId(request.getStatusId());
            }
            entity.setDateUpdated(LocalDateTime.now());
            entity.setUpdateId(userId);
        } else {
            isUpdate = false;
            entity = new ItemDataEntity();
            entity.setEventCrfId(request.getEventCrfId());
            entity.setItemId(request.getItemId());
            entity.setValue(request.getValue());
            entity.setOrdinal(request.getOrdinal());
            entity.setStatusId(request.getStatusId() != null ? request.getStatusId() : 1);
            entity.setDeleted(false);
            entity.setDateCreated(LocalDateTime.now());
            entity.setOwnerId(userId);
        }

        ItemDataEntity saved = itemDataRepository.save(entity);

        auditService.recordAudit(
                resolveStudyId(saved.getEventCrfId()),
                isUpdate ? AuditEventType.UPDATE : AuditEventType.CREATE, "ItemData",
                saved.getItemDataId().longValue(), "Item #" + saved.getItemId(),
                null, saved.getValue(), userId, null, "datacapture");

        return toItemDataDto(saved);
    }

    private void validateItemSave(SaveItemDataRequest request) {
        EventCrfEntity eventCrf = requireWritableEventCrf(request.getEventCrfId());
        Integer crfVersionId = eventCrf.getCrfVersionId();
        ItemFormMetadataEntity metadata = requireItemMetadata(request.getItemId(), crfVersionId);
        validateRequiredValue(request, metadata);
        validateRegexp(request, metadata);
        validateResponseSetMembership(request, metadata);
        validateItemDataType(request);
    }

    private EventCrfEntity requireWritableEventCrf(Integer eventCrfId) {
        EventCrfEntity eventCrf = eventCrfRepository.findById(eventCrfId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "EventCRF not found: " + eventCrfId));
        Integer statusId = eventCrf.getStatusId();
        if (statusId != null
                && (statusId == Status.DELETED.getId()
                || statusId == Status.LOCKED.getId()
                || statusId == Status.AUTO_DELETED.getId()
                || statusId == Status.SIGNED.getId()
                || statusId == Status.FROZEN.getId())) {
            throw new IllegalStateException("EventCRF is not writable: " + eventCrfId);
        }
        Integer crfVersionId = eventCrf.getCrfVersionId();
        if (crfVersionId == null) {
            throw new IllegalArgumentException("EventCRF has no CRF version: " + eventCrfId);
        }
        return eventCrf;
    }

    private ItemFormMetadataEntity requireItemMetadata(Integer itemId, Integer crfVersionId) {
        boolean itemBelongsToVersion = !itemGroupMetadataRepository
                .findByItemIdAndCrfVersionId(itemId, crfVersionId)
                .isEmpty();
        if (!itemBelongsToVersion) {
            throw new IllegalArgumentException(
                    "Item " + itemId + " is not part of CRF version " + crfVersionId);
        }
        return itemFormMetadataRepository.findByItemIdAndCrfVersionId(itemId, crfVersionId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item " + itemId + " has no form metadata for CRF version " + crfVersionId));
    }

    private void validateRequiredValue(SaveItemDataRequest request, ItemFormMetadataEntity metadata) {
        if (Boolean.TRUE.equals(metadata.getRequired()) && isBlank(request.getValue())) {
            throw new IllegalArgumentException("Item " + request.getItemId() + " is required");
        }
    }

    private void validateRegexp(SaveItemDataRequest request, ItemFormMetadataEntity metadata) {
        if (isBlank(request.getValue()) || isBlank(metadata.getRegexp())) {
            return;
        }
        try {
            if (!Pattern.compile(metadata.getRegexp()).matcher(request.getValue()).matches()) {
                String message = !isBlank(metadata.getRegexpErrorMsg())
                        ? metadata.getRegexpErrorMsg()
                        : "Item " + request.getItemId() + " does not match required format";
                throw new IllegalArgumentException(message);
            }
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException(
                    "Invalid regexp configured for item " + request.getItemId(), e);
        }
    }

    private void validateResponseSetMembership(SaveItemDataRequest request, ItemFormMetadataEntity metadata) {
        if (isBlank(request.getValue()) || metadata.getResponseSetId() == null) {
            return;
        }
        ResponseSetEntity responseSet = responseSetRepository.findById(metadata.getResponseSetId())
                .orElseThrow(() -> new IllegalStateException(
                        "Response set not found for item " + request.getItemId()
                                + ": " + metadata.getResponseSetId()));
        Integer responseTypeId = responseSet.getResponseTypeId();
        if (responseTypeId == null || !isControlledResponseType(responseTypeId)) {
            return;
        }

        Set<String> allowedValues = parseResponseSetValues(responseSet.getOptionsValues());
        List<String> submittedValues = isMultiValueResponseType(responseTypeId)
                ? parseSubmittedMultiValues(request.getValue())
                : List.of(request.getValue().trim());

        boolean allValuesAllowed = submittedValues.stream()
                .filter(value -> !value.isBlank())
                .allMatch(allowedValues::contains);
        if (!allValuesAllowed) {
            throw new IllegalArgumentException(
                    "Item " + request.getItemId() + " value is not in the configured response set");
        }
    }

    private boolean isControlledResponseType(Integer responseTypeId) {
        return responseTypeId == RESPONSE_TYPE_CHECKBOX
                || responseTypeId == RESPONSE_TYPE_RADIO
                || responseTypeId == RESPONSE_TYPE_SELECT
                || responseTypeId == RESPONSE_TYPE_SELECT_MULTI;
    }

    private boolean isMultiValueResponseType(Integer responseTypeId) {
        return responseTypeId == RESPONSE_TYPE_CHECKBOX || responseTypeId == RESPONSE_TYPE_SELECT_MULTI;
    }

    private Set<String> parseResponseSetValues(String optionsValues) {
        if (optionsValues == null) {
            return Set.of();
        }
        Set<String> values = new HashSet<>();
        for (String value : optionsValues.split("\\\\n|\\n|,")) {
            if (value != null && !value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }

    private List<String> parseSubmittedMultiValues(String value) {
        List<String> values = new ArrayList<>();
        for (String part : value.split("\\\\n|\\n|,")) {
            if (part != null && !part.isBlank()) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private void validateItemDataType(SaveItemDataRequest request) {
        if (isBlank(request.getValue())) {
            return;
        }
        ItemEntity item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "Item not found: " + request.getItemId()));
        Integer dataTypeId = item.getItemDataTypeId();
        if (dataTypeId == null) {
            return;
        }
        String value = request.getValue();
        switch (dataTypeId) {
            case ITEM_DATA_TYPE_STRING -> validateStringValue(request.getItemId(), value);
            case ITEM_DATA_TYPE_INTEGER -> validateIntegerValue(request.getItemId(), value);
            case ITEM_DATA_TYPE_REAL -> validateRealValue(request.getItemId(), value);
            case ITEM_DATA_TYPE_DATE -> validateFullDateValue(request.getItemId(), value);
            case ITEM_DATA_TYPE_PARTIAL_DATE -> validatePartialDateValue(request.getItemId(), value);
            default -> {
            }
        }
    }

    private void validateStringValue(Integer itemId, String value) {
        if (value.length() > 3999) {
            throw new IllegalArgumentException("Item " + itemId + " value exceeds 3999 characters");
        }
    }

    private void validateIntegerValue(Integer itemId, String value) {
        try {
            Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Item " + itemId + " requires an integer value", e);
        }
    }

    private void validateRealValue(Integer itemId, String value) {
        try {
            Float.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Item " + itemId + " requires a real number value", e);
        }
    }

    private void validateFullDateValue(Integer itemId, String value) {
        if (!isExactDate(value, FULL_DATE_FORMAT, FULL_DATE_PATTERN)) {
            throw new IllegalArgumentException(
                    "Item " + itemId + " requires a date value in yyyy-MM-dd format");
        }
    }

    private void validatePartialDateValue(Integer itemId, String value) {
        if (!isExactDate(value, FULL_DATE_FORMAT, FULL_DATE_PATTERN)
                && !isExactDate(value, PARTIAL_DATE_YEAR_MONTH_FORMAT, PARTIAL_DATE_YEAR_MONTH_PATTERN)
                && !isExactDate(value, PARTIAL_DATE_YEAR_FORMAT, PARTIAL_DATE_YEAR_PATTERN)) {
            throw new IllegalArgumentException("Item " + itemId + " requires a partial date value");
        }
    }

    private boolean isExactDate(String value, String format, Pattern pattern) {
        if (!pattern.matcher(value).matches()) {
            return false;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Transactional
    public List<ItemDataDTO> batchSaveItems(BatchSaveItemsRequest request, Integer userId) {
        List<ItemDataDTO> results = new ArrayList<>();
        for (SaveItemDataRequest itemRequest : request.getItems()) {
            itemRequest.setEventCrfId(request.getEventCrfId());
            results.add(saveItemData(itemRequest, userId));
        }
        return results;
    }

    /**
     * Retrieves applicable RuleSets, rules, and expressions for a given EventCRF
     * without performing full expression evaluation.
     */
    public RuleEvalResponse evaluateRules(int eventCrfId) {
        EventCrfEntity eventCrf = eventCrfRepository.findById(eventCrfId)
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "EventCRF not found: " + eventCrfId));
        StudyEventEntity studyEvent = studyEventRepository.findById(eventCrf.getStudyEventId())
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "StudyEvent not found: " + eventCrf.getStudyEventId()));
        StudySubjectEntity studySubject = studySubjectRepository.findById(eventCrf.getStudySubjectId())
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "StudySubject not found: " + eventCrf.getStudySubjectId()));
        CrfVersionEntity crfVersion = crfVersionRepository.findById(eventCrf.getCrfVersionId())
                .orElseThrow(() -> new java.util.NoSuchElementException(
                        "CrfVersion not found: " + eventCrf.getCrfVersionId()));

        int crfId = crfVersion.getCrfId();
        int studyId = studySubject.getStudyId();
        int studyEventDefinitionId = studyEvent.getStudyEventDefinitionId();

        List<RuleSetEntity> ruleSets = ruleSetRepository
                .findByCrfVersionIdOrCrfIdAndStudyIdAndStudyEventDefinitionId(
                        eventCrf.getCrfVersionId(), crfId, studyId, studyEventDefinitionId);

        List<RuleInfo> rules = ruleSets.stream()
                .flatMap(ruleSet -> {
                    List<RuleSetRuleEntity> mappings = ruleSetRuleRepository
                            .findByRuleSetId(ruleSet.getRuleSetId());
                    return mappings.stream()
                            .map(mapping -> {
                                RuleEntity rule = ruleRepository.findById(mapping.getRuleId())
                                        .orElse(null);
                                if (rule == null) return null;
                                RuleExpressionEntity expr = rule.getRuleExpressionId() != null
                                        ? ruleExpressionRepository.findById(rule.getRuleExpressionId())
                                                .orElse(null)
                                        : null;

                                RuleInfo info = new RuleInfo();
                                info.setRuleName(rule.getName());
                                info.setRuleDescription(rule.getDescription());
                                info.setExpressionValue(expr != null ? expr.getValue() : null);
                                info.setEnabled(rule.getEnabled() != null && rule.getEnabled());
                                return info;
                            })
                            .filter(info -> info != null);
                })
                .toList();

        RuleEvalResponse response = new RuleEvalResponse();
        response.setEventCrfId(eventCrfId);
        response.setRuleSetCount(ruleSets.size());
        response.setRules(rules);
        return response;
    }

    private Integer resolveStudyId(Integer eventCrfId) {
        if (eventCrfId == null) {
            return null;
        }
        return eventCrfRepository.findById(eventCrfId)
                .flatMap(eventCrf -> studySubjectRepository.findById(eventCrf.getStudySubjectId()))
                .map(StudySubjectEntity::getStudyId)
                .orElse(null);
    }

    private ItemDataDTO toItemDataDto(ItemDataEntity e) {
        ItemDataDTO dto = new ItemDataDTO();
        dto.setItemDataId(e.getItemDataId());
        dto.setItemId(e.getItemId());
        dto.setEventCrfId(e.getEventCrfId());
        dto.setValue(e.getValue());
        if (e.getOrdinal() != null) {
            dto.setOrdinal(e.getOrdinal());
        }
        dto.setStatusId(e.getStatusId());
        dto.setDeleted(e.getDeleted());
        dto.setDateCreated(e.getDateCreated());
        dto.setDateUpdated(e.getDateUpdated());
        return dto;
    }

    private ResponseSetDTO toResponseSetDto(ResponseSetEntity e) {
        ResponseSetDTO dto = new ResponseSetDTO();
        dto.setResponseSetId(e.getResponseSetId());
        dto.setResponseTypeId(e.getResponseTypeId());
        dto.setLabel(e.getLabel());
        dto.setOptions(parseOptions(e.getOptionsText(), e.getOptionsValues()));
        return dto;
    }

    private ItemGroupDTO toItemGroupDto(ItemGroupEntity e) {
        ItemGroupDTO dto = new ItemGroupDTO();
        dto.setItemGroupId(e.getItemGroupId());
        dto.setCrfId(e.getCrfId());
        dto.setName(e.getName());
        dto.setOcOid(e.getOcOid());
        return dto;
    }

    private static final Logger log = LoggerFactory.getLogger(DataCaptureService.class);

    public void downloadAttachmentByEventCrf(int eventCrfId, String attachmentId,
                                             Integer currentUserId, HttpServletResponse response) {
        if (!attachmentStorageAdapter.canViewEventCrfData(eventCrfId, currentUserId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String fileName = decodeAttachmentId(attachmentId);
        if (fileName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String studyOid = attachmentStorageAdapter.getStudyOidByEventCrf(eventCrfId);
        if (studyOid == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        for (String candidateStudyOid : attachmentStorageAdapter.getCandidateStudyOids(studyOid)) {
            File file = attachmentStorageAdapter.resolveAttachmentFile(fileName, candidateStudyOid);
            if (file != null && file.exists() && file.length() > 0) {
                streamFile(file, response);
                return;
            }
        }

        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void streamFile(File file, HttpServletResponse response) {
        try {
            response.setContentType("application/download");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setHeader("Cache-Control", "max-age=0");
            response.setContentLengthLong(file.length());
            try (FileInputStream in = new FileInputStream(file)) {
                in.transferTo(response.getOutputStream());
                response.flushBuffer();
            }
        } catch (IOException e) {
            log.warn("Failed to stream attachment: {}", file.getAbsolutePath(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public File resolveAttachmentFile(String fileName, String studyOid) {
        return attachmentStorageAdapter.resolveAttachmentFile(fileName, studyOid);
    }

    public List<AttachmentDTO> listAttachmentsByEventCrf(int eventCrfId, Integer currentUserId) {
        if (!attachmentStorageAdapter.canViewEventCrfData(eventCrfId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this event CRF");
        }
        String studyOid = attachmentStorageAdapter.getStudyOidByEventCrf(eventCrfId);
        if (studyOid == null) {
            return List.of();
        }

        List<AttachmentDTO> files = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        for (String candidateStudyOid : attachmentStorageAdapter.getCandidateStudyOids(studyOid)) {
            File studyDir = attachmentStorageAdapter.studyDirectory(candidateStudyOid);
            if (studyDir.exists() && studyDir.isDirectory()) {
                File[] dirFiles = studyDir.listFiles();
                if (dirFiles != null) {
                    for (File f : dirFiles) {
                        if (f.isFile() && !seen.contains(f.getName())) {
                            seen.add(f.getName());
                            files.add(new AttachmentDTO(encodeAttachmentId(f.getName()), f.getName(), f.length()));
                        }
                    }
                }
            }
        }
        return files;
    }

    /**
     * Uploads a file attachment for an event CRF.
     * Stores the file in the study's attachment directory,
     * creating the directory if it does not exist.
     */
    @Transactional
    public void uploadAttachment(int eventCrfId, MultipartFile file, Integer currentUserId) throws IOException {
        if (!attachmentStorageAdapter.canViewEventCrfData(eventCrfId, currentUserId)) {
            throw new AccessDeniedException("You do not have access to this event CRF");
        }
        String studyOid = attachmentStorageAdapter.getStudyOidByEventCrf(eventCrfId);
        if (studyOid == null || file.isEmpty()) return;

        String originalName = file.getOriginalFilename();
        if (!isSafeAttachmentName(originalName)) {
            throw new IOException("Invalid attachment file name");
        }
        String safeName = new File(originalName).getName();
        File studyDir = attachmentStorageAdapter.studyDirectory(studyOid);
        if (!studyDir.exists()) {
            studyDir.mkdirs();
        }
        File dest = new File(studyDir, safeName);
        file.transferTo(dest);
    }

    private static String encodeAttachmentId(String fileName) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(fileName.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeAttachmentId(String attachmentId) {
        if (attachmentId == null || attachmentId.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(attachmentId), StandardCharsets.UTF_8);
            return isSafeAttachmentName(decoded) ? decoded : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static boolean isSafeAttachmentName(String fileName) {
        return fileName != null
                && !fileName.isBlank()
                && fileName.equals(new File(fileName).getName())
                && !fileName.contains("/")
                && !fileName.contains("\\");
    }

    private static List<OptionDTO> parseOptions(String optionsText, String optionsValues) {
        List<OptionDTO> options = new ArrayList<>();
        if (optionsText == null || optionsValues == null) {
            return options;
        }
        String[] texts = optionsText.split("\\\\n|\\n");
        String[] values = optionsValues.split("\\\\n|\\n");
        int len = Math.min(texts.length, values.length);
        for (int i = 0; i < len; i++) {
            options.add(new OptionDTO(texts[i].trim(), values[i].trim()));
        }
        return options;
    }
}
