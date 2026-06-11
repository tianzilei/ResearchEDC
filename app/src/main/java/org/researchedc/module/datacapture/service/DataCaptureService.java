package org.researchedc.module.datacapture.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.researchedc.bean.core.Utils;
import org.researchedc.module.audit.enums.AuditEventType;
import org.researchedc.module.audit.service.AuditService;
import org.researchedc.module.datacapture.dto.BatchSaveItemsRequest;
import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO;
import org.researchedc.module.datacapture.dto.ResponseSetDTO.OptionDTO;
import org.researchedc.module.datacapture.dto.SaveItemDataRequest;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.entity.ResponseSetEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;
import org.researchedc.module.datacapture.repository.ResponseSetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DataCaptureService {

    private final ItemDataRepository itemDataRepository;
    private final ResponseSetRepository responseSetRepository;
    private final ItemGroupRepository itemGroupRepository;
    private final AuditService auditService;

    public DataCaptureService(ItemDataRepository itemDataRepository,
                               ResponseSetRepository responseSetRepository,
                               ItemGroupRepository itemGroupRepository,
                               AuditService auditService) {
        this.itemDataRepository = itemDataRepository;
        this.responseSetRepository = responseSetRepository;
        this.itemGroupRepository = itemGroupRepository;
        this.auditService = auditService;
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

    @Transactional
    public ItemDataDTO saveItemData(SaveItemDataRequest request, Integer userId) {
        List<ItemDataEntity> existing = itemDataRepository.findByEventCrfIdAndItemId(
            request.getEventCrfId(), request.getItemId());

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
            entity.setStatusId(request.getStatusId() != null ? request.getStatusId() : 1);
            entity.setDeleted(false);
            entity.setDateCreated(LocalDateTime.now());
            entity.setOwnerId(userId);
        }

        ItemDataEntity saved = itemDataRepository.save(entity);

        auditService.recordAudit(
                null, isUpdate ? AuditEventType.UPDATE : AuditEventType.CREATE, "ItemData",
                saved.getItemDataId().longValue(), "Item #" + saved.getItemId(),
                null, saved.getValue(), userId, null, "datacapture");

        return toItemDataDto(saved);
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

    private ItemDataDTO toItemDataDto(ItemDataEntity e) {
        ItemDataDTO dto = new ItemDataDTO();
        dto.setItemDataId(e.getItemDataId());
        dto.setItemId(e.getItemId());
        dto.setEventCrfId(e.getEventCrfId());
        dto.setValue(e.getValue());
        dto.setOrdinal(e.getOrdinal());
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

    /**
     * Downloads a file attachment from a study's attachment directory.
     * Path is resolved as {@code rootPath + studyOid + File.separator + safeFileName}
     * with canonical-path verification to prevent directory traversal.
     */
    public void downloadAttachment(String fileName, String studyOid, HttpServletResponse response) {
        if (studyOid == null || studyOid.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        File file = resolveAttachmentFile(fileName, studyOid);
        if (file == null || !file.exists() || file.length() <= 0) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
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
            log.warn("Failed to stream attachment: {} (study={})", fileName, studyOid, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Resolves an attachment file within a study's directory. The resolved path is
     * {@code rootPath + studyOid + File.separator + safeFileName}, validated via
     * canonical-path comparison to prevent directory traversal.
     *
     * @return the resolved File, or a non-existent file if resolution fails
     */
    public File resolveAttachmentFile(String fileName, String studyOid) {
        if (fileName == null || fileName.isEmpty() || studyOid == null || studyOid.isEmpty()) {
            return new File("");
        }
        String safeName = new File(fileName).getName();
        String rootPath = Utils.getAttachedFileRootPath();
        File resolved = new File(rootPath, studyOid + File.separator + safeName);
        try {
            String canonical = resolved.getCanonicalPath();
            String expectedPrefix = new File(rootPath).getCanonicalPath();
            if (!canonical.startsWith(expectedPrefix)) {
                log.warn("Path traversal attempt blocked: {} (study={})", fileName, studyOid);
                return new File("");
            }
        } catch (IOException e) {
            log.warn("Failed to resolve canonical path: {} (study={})", fileName, studyOid);
            return new File("");
        }
        return resolved;
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
