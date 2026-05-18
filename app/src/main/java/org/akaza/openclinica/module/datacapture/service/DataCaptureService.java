package org.akaza.openclinica.module.datacapture.service;

import java.util.ArrayList;
import java.util.List;
import org.akaza.openclinica.module.datacapture.dto.ItemDataDTO;
import org.akaza.openclinica.module.datacapture.dto.ItemGroupDTO;
import org.akaza.openclinica.module.datacapture.dto.ResponseSetDTO;
import org.akaza.openclinica.module.datacapture.dto.ResponseSetDTO.OptionDTO;
import org.akaza.openclinica.module.datacapture.entity.ItemDataEntity;
import org.akaza.openclinica.module.datacapture.entity.ItemGroupEntity;
import org.akaza.openclinica.module.datacapture.entity.ResponseSetEntity;
import org.akaza.openclinica.module.datacapture.repository.ItemDataRepository;
import org.akaza.openclinica.module.datacapture.repository.ItemGroupRepository;
import org.akaza.openclinica.module.datacapture.repository.ResponseSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DataCaptureService {

    private final ItemDataRepository itemDataRepository;
    private final ResponseSetRepository responseSetRepository;
    private final ItemGroupRepository itemGroupRepository;

    public DataCaptureService(ItemDataRepository itemDataRepository,
                              ResponseSetRepository responseSetRepository,
                              ItemGroupRepository itemGroupRepository) {
        this.itemDataRepository = itemDataRepository;
        this.responseSetRepository = responseSetRepository;
        this.itemGroupRepository = itemGroupRepository;
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
