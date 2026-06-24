package org.researchedc.module.crf.internal.adapter;

import org.researchedc.module.crf.dto.ItemDTO;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.repository.ItemRepository;
import org.researchedc.app.dto.Status;
import org.researchedc.module.dataimport.service.ImportItemPort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component("itemDAO")
@Primary
@Transactional(readOnly = true)
public class ItemDaoAdapter implements ImportItemPort {

    private final ItemRepository itemRepository;

    public ItemDaoAdapter(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public ItemDTO create(ItemDTO dto) {
        ItemEntity entity = new ItemEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(itemRepository.save(entity));
    }

    @Transactional
    public ItemDTO update(ItemDTO dto) {
        ItemEntity entity = itemRepository.findById(dto.getId())
                .orElseGet(ItemEntity::new);
        entity.setItemId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(itemRepository.save(entity));
    }

    public Object getEntityFromHashMap(HashMap hm) {
        ItemEntity entity = new ItemEntity();
        entity.setItemId((Integer) hm.get("item_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setUnits((String) hm.get("units"));
        entity.setItemDataTypeId((Integer) hm.get("item_data_type_id"));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setPhiStatus((Boolean) hm.get("phi_status"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setUpdateId((Integer) hm.get("update_id"));
        return toBean(entity);
    }

    public List<ItemDTO> findByOid(String oid) {
        List<ItemDTO> dtos = new ArrayList<>();
        itemRepository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemEntity::getItemId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    @Override
    public List<ImportItem> findImportItemsByOid(String oid) {
        return itemRepository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemEntity::getItemId, Comparator.nullsLast(Integer::compareTo)))
                .map(entity -> new ImportItem(entity.getItemId(), entity.getOcOid(), entity.getItemDataTypeId()))
                .toList();
    }

    public Collection findAll() {
        return toBeans(itemRepository.findByStatusId(Status.AVAILABLE.getId()));
    }

    public ArrayList findAllBySectionId(int sectionId) {
        return toBeans(itemRepository.findBySectionId(sectionId));
    }

    public ArrayList findAllBySectionIdOrderedByItemFormMetadataOrdinal(int sectionId) {
        return toBeans(itemRepository.findBySectionIdOrderedByOrdinal(sectionId));
    }

    public ItemDTO findByPK(int ID) {
        return itemRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemDTO::new);
    }

    public ItemDTO findByName(String name) {
        return itemRepository.findByName(name).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemDTO::new);
    }

    public ItemDTO findByNameAndCRFId(String name, int crfId) {
        return itemRepository.findByNameAndCrfId(name, crfId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemDTO::new);
    }

    public ArrayList findAllByParentIdAndCRFVersionId(int parentId, int crfVersionId) {
        return toBeans(itemRepository.findByParentIdAndCrfVersionId(parentId, crfVersionId));
    }

    private void apply(ItemDTO dto, ItemEntity entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setUnits(dto.getUnits());
        entity.setItemDataTypeId(dto.getItemDataTypeId());
        entity.setOcOid(dto.getOid());
        entity.setPhiStatus(dto.isPhiStatus());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
    }

    private ArrayList<ItemDTO> toBeans(List<ItemEntity> entities) {
        ArrayList<ItemDTO> dtos = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(ItemEntity::getItemId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private ItemDTO toBean(ItemEntity entity) {
        ItemDTO dto = new ItemDTO();
        if (entity.getItemId() != null) {
            dto.setId(entity.getItemId());
        }
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setUnits(entity.getUnits());
        dto.setItemDataTypeId(valueOrZero(entity.getItemDataTypeId()));
        dto.setOid(entity.getOcOid());
        dto.setPhiStatus(entity.getPhiStatus() != null && entity.getPhiStatus());
        dto.setStatusId(valueOrZero(entity.getStatusId()));
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setUpdaterId(valueOrZero(entity.getUpdateId()));
        return dto;
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
