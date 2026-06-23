package org.researchedc.module.crf.internal.adapter;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.repository.ItemRepository;
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
    public EntityBean create(EntityBean eb) {
        ItemBean bean = (ItemBean) eb;
        ItemEntity entity = new ItemEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(itemRepository.save(entity));
    }

    @Transactional
    public EntityBean update(EntityBean eb) {
        ItemBean bean = (ItemBean) eb;
        ItemEntity entity = itemRepository.findById(bean.getId())
                .orElseGet(ItemEntity::new);
        entity.setItemId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
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

    public List<ItemBean> findByOid(String oid) {
        List<ItemBean> beans = new ArrayList<>();
        itemRepository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemEntity::getItemId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
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

    public EntityBean findByPK(int ID) {
        return itemRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemBean::new);
    }

    public EntityBean findByName(String name) {
        return itemRepository.findByName(name).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemBean::new);
    }

    public EntityBean findByNameAndCRFId(String name, int crfId) {
        return itemRepository.findByNameAndCrfId(name, crfId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemBean::new);
    }

    public ArrayList findAllByParentIdAndCRFVersionId(int parentId, int crfVersionId) {
        return toBeans(itemRepository.findByParentIdAndCrfVersionId(parentId, crfVersionId));
    }

    private void apply(ItemBean bean, ItemEntity entity) {
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setUnits(bean.getUnits());
        entity.setItemDataTypeId(bean.getItemDataTypeId());
        entity.setOcOid(bean.getOid());
        entity.setPhiStatus(bean.isPhiStatus());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList toBeans(List<ItemEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(ItemEntity::getItemId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private ItemBean toBean(ItemEntity entity) {
        ItemBean bean = new ItemBean();
        if (entity.getItemId() != null) {
            bean.setId(entity.getItemId());
        }
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setUnits(entity.getUnits());
        bean.setItemDataTypeId(valueOrZero(entity.getItemDataTypeId()));
        bean.setOid(entity.getOcOid());
        bean.setPhiStatus(entity.getPhiStatus() != null && entity.getPhiStatus());
        bean.setStatusId(valueOrZero(entity.getStatusId()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        return bean;
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
