package org.researchedc.module.datacapture.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.module.datacapture.dto.ItemDataDTO;
import org.researchedc.app.dto.Status;
import org.researchedc.module.dataimport.service.ImportItemDataPort;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("itemDataDAO")
@Primary
@Transactional(readOnly = true)
public class ItemDataDaoAdapter implements ImportItemDataPort {
    private final ItemDataRepository repository;

    public ItemDataDaoAdapter(ItemDataRepository repository) {
        this.repository = repository;
    }

    public ItemDataDTO findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemDataDTO::new);
    }

    @Transactional
    public ItemDataDTO create(ItemDataDTO dto) {
        ItemDataEntity entity = new ItemDataEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public ItemDataDTO update(ItemDataDTO dto) {
        return upsert(dto);
    }

    @Transactional
    public ItemDataDTO upsert(ItemDataDTO dto) {
        ItemDataEntity entity = repository.findById(dto.getId())
                .orElseGet(ItemDataEntity::new);
        entity.setItemDataId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        if (dto.getId() > 0 && entity.getDateCreated() == null) {
            entity.setDateUpdated(LocalDateTime.now());
        } else {
            entity.setDateCreated(LocalDateTime.now());
        }
        return toBean(repository.save(entity));
    }

    @Transactional
    public void upsertImportItemData(
            int itemId,
            int eventCrfId,
            int ordinal,
            int ownerId,
            int statusId,
            String value) {
        ItemDataDTO dto = new ItemDataDTO();
        dto.setItemId(itemId);
        dto.setEventCRFId(eventCrfId);
        dto.setOrdinal(ordinal);
        dto.setOwnerId(ownerId);
        dto.setStatus(Status.getFromMap(statusId));
        dto.setValue(value);
        upsert(dto);
    }

    public Collection<ItemDataDTO> findAll() {
        return toBeans(repository.findAll());
    }

    public ArrayList<ItemDataDTO> findAllByEventCRFId(int eventCRFId) {
        return toBeans(repository.findByEventCrfId(eventCRFId));
    }

    public ArrayList<ItemDataDTO> findAllByEventCRFIdAndItemId(int eventCRFId, int itemId) {
        return toBeans(repository.findByEventCrfIdAndItemId(eventCRFId, itemId));
    }

    public ArrayList<ItemDataDTO> findAllByEventCRFIdAndItemIdNoStatus(int eventCRFId, int itemId) {
        return toBeans(repository.findByEventCrfIdAndItemId(eventCRFId, itemId));
    }

    public ItemDataDTO findByItemIdAndEventCRFId(int itemId, int eventCRFId) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfId(itemId, eventCRFId);
        if (entities.isEmpty()) {
            return new ItemDataDTO();
        }
        return toBean(entities.get(0));
    }

    public ItemDataDTO findByItemIdAndEventCRFIdAndOrdinal(int itemId, int eventCRFId, int ordinal) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfIdAndOrdinal(itemId, eventCRFId, ordinal);
        if (entities.isEmpty()) {
            return new ItemDataDTO();
        }
        return toBean(entities.get(0));
    }

    public Object getEntityFromHashMap(HashMap hm) {
        ItemDataDTO dto = new ItemDataDTO();
        if (hm.get("item_data_id") instanceof Integer) {
            dto.setId((Integer) hm.get("item_data_id"));
        }
        if (hm.get("event_crf_id") instanceof Integer) {
            dto.setEventCRFId((Integer) hm.get("event_crf_id"));
        }
        if (hm.get("item_id") instanceof Integer) {
            dto.setItemId((Integer) hm.get("item_id"));
        }
        if (hm.get("value") instanceof String) {
            dto.setValue((String) hm.get("value"));
        }
        if (hm.get("status_id") instanceof Integer) {
            dto.setStatus(Status.get((Integer) hm.get("status_id")));
        }
        if (hm.get("ordinal") instanceof Integer) {
            dto.setOrdinal((Integer) hm.get("ordinal"));
        }
        if (hm.get("deleted") instanceof Boolean) {
            dto.setDeleted((Boolean) hm.get("deleted"));
        }
        if (hm.get("date_created") instanceof Date) {
            dto.setCreatedDate((Date) hm.get("date_created"));
        }
        if (hm.get("date_updated") instanceof Date) {
            dto.setUpdatedDate((Date) hm.get("date_updated"));
        }
        if (hm.get("owner_id") instanceof Integer) {
            dto.setOwnerId((Integer) hm.get("owner_id"));
        }
        if (hm.get("update_id") instanceof Integer) {
            dto.setUpdaterId((Integer) hm.get("update_id"));
        }
        return dto;
    }

    public void delete(int itemDataId) {
        repository.deleteById(itemDataId);
    }

    private void apply(ItemDataDTO dto, ItemDataEntity entity) {
        entity.setEventCrfId(dto.getEventCRFId());
        entity.setItemId(dto.getItemId());
        entity.setValue(dto.getValue());
        entity.setOrdinal(dto.getOrdinal());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setDeleted(dto.isDeleted());
        entity.setOwnerId(dto.getOwnerId() > 0 ? dto.getOwnerId() : null);
        entity.setUpdateId(dto.getUpdaterId() > 0 ? dto.getUpdaterId() : null);
    }

    private ItemDataDTO toBean(ItemDataEntity entity) {
        ItemDataDTO dto = new ItemDataDTO();
        if (entity.getItemDataId() != null) {
            dto.setId(entity.getItemDataId());
        }
        dto.setEventCRFId(valueOrZero(entity.getEventCrfId()));
        dto.setItemId(valueOrZero(entity.getItemId()));
        dto.setValue(entity.getValue() != null ? entity.getValue() : "");
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        dto.setOrdinal(valueOrZero(entity.getOrdinal()));
        dto.setDeleted(entity.getDeleted() != null && entity.getDeleted());
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
        return dto;
    }

    private ArrayList<ItemDataDTO> toBeans(List<ItemDataEntity> entities) {
        ArrayList<ItemDataDTO> dtos = new ArrayList<>();
        for (ItemDataEntity entity : entities) {
            dtos.add(toBean(entity));
        }
        return dtos;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}
