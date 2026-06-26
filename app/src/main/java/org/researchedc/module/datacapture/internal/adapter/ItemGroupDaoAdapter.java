package org.researchedc.module.datacapture.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.module.datacapture.dto.ItemGroupDTO;
import org.researchedc.module.dataimport.service.ImportItemGroupPort;
import org.researchedc.module.dataimport.dto.ImportItemGroup;
import org.researchedc.app.dto.Status;
import org.researchedc.module.datacapture.entity.ItemGroupEntity;
import org.researchedc.module.datacapture.repository.ItemGroupRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("itemGroupDAO")
@Primary
@Transactional(readOnly = true)
public class ItemGroupDaoAdapter implements ImportItemGroupPort {

    private final ItemGroupRepository repository;

    public ItemGroupDaoAdapter(ItemGroupRepository repository) {
        this.repository = repository;
    }

    public ItemGroupDTO findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemGroupDTO::new);
    }

    @Transactional
    public ItemGroupDTO create(ItemGroupDTO dto) {
        ItemGroupEntity entity = new ItemGroupEntity();
        apply(dto, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public ItemGroupDTO update(ItemGroupDTO dto) {
        ItemGroupEntity entity = repository.findById(dto.getId())
                .orElseGet(ItemGroupEntity::new);
        entity.setItemGroupId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    public Object getEntityFromHashMap(HashMap hm) {
        ItemGroupEntity entity = new ItemGroupEntity();
        entity.setItemGroupId((Integer) hm.get("item_group_id"));
        entity.setName((String) hm.get("name"));
        entity.setCrfId((Integer) hm.get("crf_id"));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setStatusId((Integer) hm.get("status_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setUpdateId((Integer) hm.get("update_id"));
        return toBean(entity);
    }

    public ItemGroupDTO findByName(String name) {
        return repository.findByName(name).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemGroupDTO::new);
    }

    public ItemGroupDTO findByOid(String oid) {
        return repository.findByOcOid(oid).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public ItemGroupDTO findByOidAndCrf(String oid, int crfId) {
        return repository.findByOcOidAndCrfId(oid, crfId).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public List<ItemGroupDTO> findAllByOid(String oid) {
        List<ItemGroupDTO> dtos = new ArrayList<>();
        repository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemGroupEntity::getItemGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    public List<ImportItemGroup> findImportItemGroupsByOid(String oid) {
        return repository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemGroupEntity::getItemGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(entity -> new ImportItemGroup(entity.getItemGroupId()))
                .toList();
    }

    public List<ItemGroupDTO> findGroupByCRFVersionID(int Id) {
        return toBeans(repository.findGroupByCRFVersionIdNative(Id));
    }

    public List<ItemGroupDTO> findGroupByCRFVersionIDMap(int Id) {
        return toBeans(repository.findGroupByCRFVersionIdNative(Id));
    }

    public List<ItemGroupDTO> findOnlyGroupsByCRFVersionID(int Id) {
        return toBeans(repository.findOnlyGroupsByCRFVersionIdNative(Id));
    }

    public List<ItemGroupDTO> findGroupBySectionId(int sectionId) {
        return toBeans(repository.findGroupBySectionIdNative(sectionId));
    }

    public List<ItemGroupDTO> findLegitGroupBySectionId(int sectionId) {
        return toBeans(repository.findLegitGroupBySectionIdNative(sectionId));
    }

    public List<ItemGroupDTO> findLegitGroupAllBySectionId(int sectionId) {
        return toBeans(repository.findLegitGroupAllBySectionIdNative(sectionId));
    }

    public ItemGroupDTO findGroupByGroupNameAndCrfVersionId(String groupName, int crfVersionId) {
        return repository.findGroupByGroupNameAndCrfVersionIdNative(crfVersionId, groupName).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public ItemGroupDTO findGroupByItemIdCrfVersionId(int itemId, int crfVersionId) {
        return repository.findGroupByItemIdCrfVersionIdNative(crfVersionId, itemId).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public Collection findGroupsByItemID(int ID) {
        return toBeans(repository.findGroupsByItemIdNative(ID));
    }

    public ItemGroupDTO findTopOneGroupBySectionId(int sectionId) {
        return repository.findTopOneGroupBySectionIdNative(sectionId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemGroupDTO::new);
    }

    private void apply(ItemGroupDTO dto, ItemGroupEntity entity) {
        entity.setName(dto.getName());
        entity.setCrfId(dto.getCrfId());
        entity.setOcOid(dto.getOid());
        entity.setStatusId(dto.getStatus() != null ? dto.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(dto.getOwnerId());
        entity.setUpdateId(dto.getUpdaterId());
    }

    private ArrayList<ItemGroupDTO> toBeans(List<ItemGroupEntity> entities) {
        ArrayList<ItemGroupDTO> dtos = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(ItemGroupEntity::getItemGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private ItemGroupDTO toBean(ItemGroupEntity entity) {
        ItemGroupDTO dto = new ItemGroupDTO();
        if (entity.getItemGroupId() != null) {
            dto.setId(entity.getItemGroupId());
        }
        dto.setName(entity.getName());
        dto.setCrfId(entity.getCrfId());
        dto.setOid(entity.getOcOid());
        dto.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        dto.setCreatedDate(toDate(entity.getDateCreated()));
        dto.setUpdatedDate(toDate(entity.getDateUpdated()));
        dto.setOwnerId(valueOrZero(entity.getOwnerId()));
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
