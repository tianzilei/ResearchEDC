package org.researchedc.module.datacapture.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.ItemDataType;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.CRFVersionBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.spi.IItemDataDAO;
import org.researchedc.domain.datamap.ItemData;
import org.researchedc.module.datacapture.entity.ItemDataEntity;
import org.researchedc.module.datacapture.repository.ItemDataRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("itemDataDAO")
@Primary
@Transactional(readOnly = true)
public class ItemDataDaoAdapter implements IItemDataDAO {

    private final ItemDataRepository repository;

    public ItemDataDaoAdapter(ItemDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemDataBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemDataBean bean = (ItemDataBean) eb;
        ItemDataEntity entity = new ItemDataEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        return upsert(eb);
    }

    @Override
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

    @Override
    public Collection<ItemDataBean> findAll() {
        return toBeans(repository.findAll());
    }

    @Override
    public ArrayList<ItemDataBean> findAllByEventCRFId(int eventCRFId) {
        return toBeans(repository.findByEventCrfId(eventCRFId));
    }

    @Override
    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemId(int eventCRFId, int itemId) {
        return toBeans(repository.findByEventCrfIdAndItemId(eventCRFId, itemId));
    }

    @Override
    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemIdNoStatus(int eventCRFId, int itemId) {
        return toBeans(repository.findByEventCrfIdAndItemId(eventCRFId, itemId));
    }

    @Override
    public ArrayList<ItemDataBean> findAllByEventCRFIdAndItemGroupId(int eventCRFId, int itemGroupId) {
        return toBeans(repository.findByEventCrfId(eventCRFId));
    }

    @Override
    public ArrayList<ItemDataBean> findAllBySectionIdAndEventCRFId(int sectionId, int eventCRFId) {
        return toBeans(repository.findByEventCrfId(eventCRFId));
    }

    @Override
    public ArrayList<ItemDataBean> findAllActiveBySectionIdAndEventCRFId(int sectionId, int eventCRFId) {
        List<ItemDataEntity> all = repository.findByEventCrfId(eventCRFId);
        ArrayList<ItemDataBean> result = new ArrayList<>();
        for (ItemDataEntity entity : all) {
            if (entity.getDeleted() == null || !entity.getDeleted()) {
                if (entity.getStatusId() == null ||
                        (entity.getStatusId() != Status.DELETED.getId() && entity.getStatusId() != Status.AUTO_DELETED.getId())) {
                    result.add(toBean(entity));
                }
            }
        }
        return result;
    }

    @Override
    public ArrayList<ItemDataBean> findAllBlankRequiredByEventCRFId(int eventCRFId, int crfVersionId) {
        return new ArrayList<>();
    }

    @Override
    public ItemDataBean findByItemIdAndEventCRFId(int itemId, int eventCRFId) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfId(itemId, eventCRFId);
        if (entities.isEmpty()) {
            return new ItemDataBean();
        }
        return toBean(entities.get(0));
    }

    @Override
    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinal(int itemId, int eventCRFId, int ordinal) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfIdAndOrdinal(itemId, eventCRFId, ordinal);
        if (entities.isEmpty()) {
            return new ItemDataBean();
        }
        return toBean(entities.get(0));
    }

    @Override
    public ItemDataBean findByItemIdAndEventCRFIdAndOrdinalRaw(int itemId, int eventCRFId, int ordinal) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfIdAndOrdinal(itemId, eventCRFId, ordinal);
        if (entities.isEmpty()) {
            return new ItemDataBean();
        }
        return toBean(entities.get(0));
    }

    @Override
    public ItemDataBean findByEventCRFIdAndItemName(EventCRFBean eventCrfBean, String itemName) {
        return null;
    }

    @Override
    public int findAllRequiredByEventCRFId(EventCRFBean ecb) {
        return 0;
    }

    @Override
    public int getMaxOrdinalForGroup(EventCRFBean ecb, SectionBean sb, ItemGroupBean igb) {
        return 0;
    }

    @Override
    public int getMaxOrdinalForGroupByGroupOID(String itemGroupOid, int eventCrfId) {
        return 0;
    }

    @Override
    public int getMaxOrdinalForGroupByItemAndEventCrf(Integer itemId, EventCRFBean ec) {
        return 0;
    }

    @Override
    public boolean isItemExists(int itemId, int ordinalForRepeatingGroupField, int eventCrfId) {
        List<ItemDataEntity> entities = repository.findByItemIdAndEventCrfIdAndOrdinal(itemId, eventCrfId, ordinalForRepeatingGroupField);
        return !entities.isEmpty();
    }

    @Override
    public int getGroupSize(int itemId, int eventcrfId) {
        return 0;
    }

    @Override
    public List<String> findValuesByItemOID(String itoid) {
        return new ArrayList<>();
    }

    @Override
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

    @Override
    public Object getKeyFromHashMap(HashMap hm) {
        StringBuilder key = new StringBuilder();
        if (hm.get("study_event_id") != null) {
            key.append(hm.get("study_event_id"));
        }
        if (hm.get("ig_ocoid") != null) {
            key.append(hm.get("ig_ocoid"));
        }
        if (hm.get("item_ocoid") != null) {
            key.append(hm.get("item_ocoid"));
        }
        return key.toString();
    }

    @Override
    public boolean isFormatDates() {
        return false;
    }

    @Override
    public void setFormatDates(boolean formatDates) {
    }

    @Override
    public Collection findMinMaxDates() {
        return new ArrayList<>();
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    public void setExtraTypesExpected() {
    }

    @Override
    public HashMap findByStudySubjectAndOids(Integer studyId, String itemOid, String itemGroupOid, int studySubjectId) {
        return new HashMap();
    }

    @Override
    public void setExtraTypesExpectedForStudyLevelSql() {
    }

    @Override
    @Transactional
    public EntityBean updateValue(EntityBean eb) {
        return eb;
    }

    @Override
    @Transactional
    public EntityBean updateValueForRemoved(EntityBean eb) {
        return eb;
    }

    @Override
    @Transactional
    public EntityBean updateStatus(EntityBean eb) {
        return eb;
    }

    @Override
    public ItemDataBean setItemDataBeanIfDateOrPdate(ItemDataBean idb, String currentDfString, ItemDataType dataType) {
        return idb;
    }

    @Override
    @Transactional
    public EntityBean updateValue(EntityBean eb, String currentDfString) {
        return eb;
    }

    @Override
    @Transactional
    public EntityBean updateUser(EntityBean eb) {
        return eb;
    }

    @Override
    public ItemDataType getDataType(int itemId) {
        return ItemDataType.ST;
    }

    @Override
    public String formatPDate(String pDate) {
        return pDate;
    }

    @Override
    public String reFormatPDate(String pDate) {
        return pDate;
    }

    @Override
    public List<ItemDataBean> findByStudyEventAndOids(Integer studyEventId, String itemOid, String itemGroupOid) {
        return new ArrayList<>();
    }

    @Override
    public HashMap findCountByStudyEventAndOIDs(Integer studyId, String itemOid, String itemGroupOid) {
        return new HashMap();
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public void delete(int itemDataId) {
        repository.deleteById(itemDataId);
    }

    @Override
    public void deleteDnMap(int itemDataId) {
    }

    @Override
    public ArrayList<ItemDataBean> findByCRFVersion(CRFVersionBean crfVersionBean) {
        return new ArrayList<>();
    }

    @Override
    public void updateStatusByEventCRF(EventCRFBean eventCRF, Status s) {
    }

    @Override
    public void undelete(int itemDataId, int updaterId) {
    }

    @Override
    public List<ItemData> findByEventCrfId(int eventCRFId) {
        ArrayList beans = findAllByEventCRFId(eventCRFId);
        return new ArrayList(beans);
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
