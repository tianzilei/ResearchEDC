package org.researchedc.module.crf.internal.adapter;

import org.researchedc.bean.admin.CRFBean;
import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.ItemBean;
import org.researchedc.core.util.ItemGroupCrvVersionUtil;
import org.researchedc.dao.spi.IItemDAO;
import org.researchedc.module.crf.entity.ItemEntity;
import org.researchedc.module.crf.repository.ItemRepository;
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
import java.util.Map;

@Component("itemDAO")
@Primary
@Transactional(readOnly = true)
public class ItemDaoAdapter implements IItemDAO {

    private final ItemRepository itemRepository;

    public ItemDaoAdapter(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemBean bean = (ItemBean) eb;
        ItemEntity entity = new ItemEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(itemRepository.save(entity));
    }

    @Override
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

    @Override
    public Integer getCountofActiveItems() {
        return 0;
    }

    @Override
    public String getValidOid(ItemBean itemBean, String crfName, String itemLabel, ArrayList<String> oidList) {
        return "";
    }

    @Override
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

    @Override
    public List<ItemBean> findByOid(String oid) {
        List<ItemBean> beans = new ArrayList<>();
        itemRepository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemEntity::getItemId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    @Override
    public Collection findAll() {
        return toBeans(itemRepository.findByStatusId(Status.AVAILABLE.getId()));
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllParentsBySectionId(int sectionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllNonRepeatingParentsBySectionId(int sectionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllBySectionId(int sectionId) {
        return toBeans(itemRepository.findBySectionId(sectionId));
    }

    @Override
    public ArrayList findAllBySectionIdOrderedByItemFormMetadataOrdinal(int sectionId) {
        return toBeans(itemRepository.findBySectionIdOrderedByOrdinal(sectionId));
    }

    @Override
    public ArrayList findAllUngroupedParentsBySectionId(int sectionId, int crfVersionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllItemsByVersionId(int versionId) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllVersionsByItemId(int itemId) {
        return new ArrayList();
    }

    @Override
    public List<ItemBean> findAllItemsByGroupId(int id, int crfVersionId) {
        return new ArrayList<>();
    }

    @Override
    public List<ItemBean> findAllItemsByGroupIdOrdered(int id, int crfVersionId) {
        return new ArrayList<>();
    }

    @Override
    public List<ItemBean> findAllItemsByGroupIdAndSectionIdOrdered(int id, int crfVersionId, int sectionId) {
        return new ArrayList<>();
    }

    @Override
    public List<ItemBean> findAllItemsByGroupIdForPrint(int id, int crfVersionId, int sectionId) {
        return new ArrayList<>();
    }

    @Override
    public ItemBean findItemByGroupIdandItemOid(int id, String itemOid) {
        return null;
    }

    @Override
    public ArrayList findAllActiveByCRF(CRFBean crf) {
        return new ArrayList();
    }

    @Override
    public EntityBean findByPK(int ID) {
        return itemRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemBean::new);
    }

    @Override
    public EntityBean findByName(String name) {
        return itemRepository.findByName(name).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemBean::new);
    }

    @Override
    public EntityBean findByNameAndCRFId(String name, int crfId) {
        return itemRepository.findByNameAndCrfId(name, crfId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemBean::new);
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort,
                                          String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public ArrayList findAllByParentIdAndCRFVersionId(int parentId, int crfVersionId) {
        return toBeans(itemRepository.findByParentIdAndCrfVersionId(parentId, crfVersionId));
    }

    @Override
    public int findAllRequiredByCRFVersionId(int crfVersionId) {
        return 0;
    }

    @Override
    public ArrayList findAllRequiredBySectionId(int sectionId) {
        return new ArrayList();
    }

    @Override
    public Map<String, Integer> mapAllItemNameAndItemIdInSection(Integer sectionId) {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> mapAllChildAndParentNameInSection(Integer sectionId) {
        return new HashMap<>();
    }

    @Override
    public ArrayList<ItemBean> findAllWithItemDataByCRFVersionId(int crfVersionId, int eventCRFId) {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<ItemGroupCrvVersionUtil> findAllWithItemGroupCRFVersionMetadataByCRFId(String crfName) {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<ItemGroupCrvVersionUtil> findAllWithItemDetailsGroupCRFVersionMetadataByCRFId(String crfName) {
        return new ArrayList<>();
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
