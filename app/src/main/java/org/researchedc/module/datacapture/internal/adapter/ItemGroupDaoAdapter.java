package org.researchedc.module.datacapture.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.submit.ItemGroupBean;
import org.researchedc.module.dataimport.service.ImportItemGroupPort;
import org.researchedc.module.dataimport.dto.ImportItemGroup;
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

    public void setTypesExpected() {
    }

    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(ItemGroupBean::new);
    }

    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemGroupBean bean = (ItemGroupBean) eb;
        ItemGroupEntity entity = new ItemGroupEntity();
        apply(bean, entity);
        entity.setDateCreated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    @Transactional
    public EntityBean update(EntityBean eb) {
        ItemGroupBean bean = (ItemGroupBean) eb;
        ItemGroupEntity entity = repository.findById(bean.getId())
                .orElseGet(ItemGroupEntity::new);
        entity.setItemGroupId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        entity.setDateUpdated(LocalDateTime.now());
        return toBean(repository.save(entity));
    }

    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                          String strOrderByColumn, boolean blnAscendingSort,
                                          String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
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

    public EntityBean findByName(String name) {
        return repository.findByName(name).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemGroupBean::new);
    }

    public ItemGroupBean findByOid(String oid) {
        return repository.findByOcOid(oid).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public ItemGroupBean findByOidAndCrf(String oid, int crfId) {
        return repository.findByOcOidAndCrfId(oid, crfId).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public List<ItemGroupBean> findAllByOid(String oid) {
        List<ItemGroupBean> beans = new ArrayList<>();
        repository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemGroupEntity::getItemGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    public List<ImportItemGroup> findImportItemGroupsByOid(String oid) {
        return repository.findByOcOid(oid).stream()
                .sorted(Comparator.comparing(ItemGroupEntity::getItemGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(entity -> new ImportItemGroup(entity.getItemGroupId()))
                .toList();
    }

    public String getValidOid(ItemGroupBean itemGroup, String crfName, String itemGroupLabel,
                              ArrayList<String> oidList) {
        return "";
    }

    public List<ItemGroupBean> findGroupByCRFVersionID(int Id) {
        return toBeans(repository.findGroupByCRFVersionIdNative(Id));
    }

    public List<ItemGroupBean> findGroupByCRFVersionIDMap(int Id) {
        return toBeans(repository.findGroupByCRFVersionIdNative(Id));
    }

    public List<ItemGroupBean> findOnlyGroupsByCRFVersionID(int Id) {
        return toBeans(repository.findOnlyGroupsByCRFVersionIdNative(Id));
    }

    public List<ItemGroupBean> findGroupBySectionId(int sectionId) {
        return toBeans(repository.findGroupBySectionIdNative(sectionId));
    }

    public List<ItemGroupBean> findLegitGroupBySectionId(int sectionId) {
        return toBeans(repository.findLegitGroupBySectionIdNative(sectionId));
    }

    public List<ItemGroupBean> findLegitGroupAllBySectionId(int sectionId) {
        return toBeans(repository.findLegitGroupAllBySectionIdNative(sectionId));
    }

    public ItemGroupBean findGroupByGroupNameAndCrfVersionId(String groupName, int crfVersionId) {
        return repository.findGroupByGroupNameAndCrfVersionIdNative(crfVersionId, groupName).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public ItemGroupBean findGroupByItemIdCrfVersionId(int itemId, int crfVersionId) {
        return repository.findGroupByItemIdCrfVersionIdNative(crfVersionId, itemId).stream()
                .findFirst()
                .map(this::toBean)
                .orElse(null);
    }

    public Collection findGroupsByItemID(int ID) {
        return toBeans(repository.findGroupsByItemIdNative(ID));
    }

    public ItemGroupBean findTopOneGroupBySectionId(int sectionId) {
        return repository.findTopOneGroupBySectionIdNative(sectionId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemGroupBean::new);
    }

    public void deleteTestGroup(String name) {
    }

    public Boolean isItemGroupRepeatingBasedOnAllCrfVersions(String groupOid) {
        return false;
    }

    public Boolean isItemGroupRepeatingBasedOnCrfVersion(String groupOid, Integer crfVersion) {
        return false;
    }

    private void apply(ItemGroupBean bean, ItemGroupEntity entity) {
        entity.setName(bean.getName());
        entity.setCrfId(bean.getCrfId());
        entity.setOcOid(bean.getOid());
        entity.setStatusId(bean.getStatus() != null ? bean.getStatus().getId() : Status.INVALID.getId());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private ArrayList toBeans(List<ItemGroupEntity> entities) {
        ArrayList beans = new ArrayList();
        entities.stream()
                .sorted(Comparator.comparing(ItemGroupEntity::getItemGroupId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private ItemGroupBean toBean(ItemGroupEntity entity) {
        ItemGroupBean bean = new ItemGroupBean();
        if (entity.getItemGroupId() != null) {
            bean.setId(entity.getItemGroupId());
        }
        bean.setName(entity.getName());
        bean.setCrfId(entity.getCrfId());
        bean.setOid(entity.getOcOid());
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
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
