package org.researchedc.module.datacapture.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.ItemDataBean;
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

    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemDataBean::new);
    }

    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemDataBean bean = (ItemDataBean) eb;
        ItemDataEntity entity = new ItemDataEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public EntityBean update(EntityBean eb) {
        return upsert(eb);
    }

    @Transactional
    public EntityBean upsert(EntityBean eb) {
        ItemDataBean bean = (ItemDataBean) eb;
        ItemDataEntity entity = repository.findById(bean.getId())
                .orElseGet(ItemDataEntity::new);
        entity.setItemDataId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        if (bean.getId() > 0 && entity.getDateCreated() == null) {
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
        ItemDataBean bean = new ItemDataBean();
        bean.setItemId(itemId);
        bean.setEventCRFId(eventCrfId);
        bean.setOrdinal(ordinal);
        bean.setOwnerId(ownerId);
        bean.setStatus(Status.getFromMap(statusId));
        bean.setValue(value);
        upsert(bean);
    }

    public Collection<ItemDataBean> findAll() {
        return toBeans(repository.findAll());
    }

    public ArrayList<ItemDataBean> findAllByEventCRFId(int eventCRFId) {
        return toBeans(repository.findByEventCrfId(eventCRFId));
    }

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemId(int eventCRFId, int itemId) {
        return toBeans(repository.findByEventCrfIdAndItemId(eventCRFId, itemId));
    }

    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemIdNoStatus(int eventCRFId, int itemId) {
        return toBeans(repository.findByEventCrfIdAndItemId(eventCRFId, itemId));
    }

    public ItemDataBean findByItemIdAndEventCRFId(int itemId, int eventCRFId) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfId(itemId, eventCRFId);
        if (entities.isEmpty()) {
            return new ItemDataBean();
        }
        return toBean(entities.get(0));
    }

    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinal(int itemId, int eventCRFId, int ordinal) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfIdAndOrdinal(itemId, eventCRFId, ordinal);
        if (entities.isEmpty()) {
            return new ItemDataBean();
        }
        return toBean(entities.get(0));
    }

    public Object getEntityFromHashMap(HashMap hm) {
        ItemDataBean bean = new ItemDataBean();
        if (hm.get("item_data_id") instanceof Integer) {
            bean.setId((Integer) hm.get("item_data_id"));
        }
        if (hm.get("event_crf_id") instanceof Integer) {
            bean.setEventCRFId((Integer) hm.get("event_crf_id"));
        }
        if (hm.get("item_id") instanceof Integer) {
            bean.setItemId((Integer) hm.get("item_id"));
        }
        if (hm.get("value") instanceof String) {
            bean.setValue((String) hm.get("value"));
        }
        if (hm.get("status_id") instanceof Integer) {
            bean.setStatus(Status.get((Integer) hm.get("status_id")));
        }
        if (hm.get("ordinal") instanceof Integer) {
            bean.setOrdinal((Integer) hm.get("ordinal"));
        }
        if (hm.get("deleted") instanceof Boolean) {
            bean.setDeleted((Boolean) hm.get("deleted"));
        }
        if (hm.get("date_created") instanceof Date) {
            bean.setCreatedDate((Date) hm.get("date_created"));
        }
        if (hm.get("date_updated") instanceof Date) {
            bean.setUpdatedDate((Date) hm.get("date_updated"));
        }
        if (hm.get("owner_id") instanceof Integer) {
            bean.setOwnerId((Integer) hm.get("owner_id"));
        }
        if (hm.get("update_id") instanceof Integer) {
            bean.setUpdaterId((Integer) hm.get("update_id"));
        }
        return bean;
    }

    public void delete(int itemDataId) {
        repository.deleteById(itemDataId);
    }

    private void apply(ItemDataBean bean, ItemDataEntity entity) {
        entity.setEventCrfId(bean.getEventCRFId());
        entity.setItemId(bean.getItemId());
        entity.setValue(bean.getValue());
        entity.setOrdinal(bean.getOrdinal());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setDeleted(bean.isDeleted());
        entity.setOwnerId(bean.getOwnerId() > 0 ? bean.getOwnerId() : null);
        entity.setUpdateId(bean.getUpdaterId() > 0 ? bean.getUpdaterId() : null);
    }

    private ItemDataBean toBean(ItemDataEntity entity) {
        ItemDataBean bean = new ItemDataBean();
        if (entity.getItemDataId() != null) {
            bean.setId(entity.getItemDataId());
        }
        bean.setEventCRFId(valueOrZero(entity.getEventCrfId()));
        bean.setItemId(valueOrZero(entity.getItemId()));
        bean.setValue(entity.getValue() != null ? entity.getValue() : "");
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
        bean.setDeleted(entity.getDeleted() != null && entity.getDeleted());
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        return bean;
    }

    private ArrayList<ItemDataBean> toBeans(List<ItemDataEntity> entities) {
        ArrayList<ItemDataBean> beans = new ArrayList<>();
        for (ItemDataEntity entity : entities) {
            beans.add(toBean(entity));
        }
        return beans;
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
