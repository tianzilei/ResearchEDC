package org.researchedc.module.datacapture.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.dao.spi.IItemGroupMetadataDAO;
import org.researchedc.dao.submit.ItemGroupMetadataDAO;
import org.researchedc.module.datacapture.entity.ItemGroupMetadataEntity;
import org.researchedc.module.datacapture.repository.ItemGroupMetadataRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("itemGroupMetadataDAO")
@Primary
@Transactional(readOnly = true)
public class ItemGroupMetadataDaoAdapter extends ItemGroupMetadataDAO<String, ArrayList> implements IItemGroupMetadataDAO {

    private final ItemGroupMetadataRepository repository;

    public ItemGroupMetadataDaoAdapter(ItemGroupMetadataRepository repository) {
        super((DataSource) null);
        this.repository = repository;
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        ItemGroupMetadataEntity entity = new ItemGroupMetadataEntity();
        entity.setItemGroupMetadataId(asInteger(hm.get("item_group_metadata_id")));
        entity.setItemGroupId(asInteger(hm.get("item_group_id")));
        entity.setHeader((String) hm.get("header"));
        entity.setSubheader((String) hm.get("subheader"));
        entity.setLayout((String) hm.get("layout"));
        entity.setRepeatNumber(asInteger(hm.get("repeat_number")));
        entity.setRepeatMax(asInteger(hm.get("repeat_max")));
        entity.setRepeatArray((String) hm.get("repeat_array"));
        entity.setRowStartNumber(asInteger(hm.get("row_start_number")));
        entity.setCrfVersionId(asInteger(hm.get("crf_version_id")));
        entity.setItemId(asInteger(hm.get("item_id")));
        entity.setOrdinal(asInteger(hm.get("ordinal")));
        entity.setBorders(asInteger(hm.get("borders")));
        entity.setShowGroup(asBoolean(hm.get("show_group")));
        entity.setRepeatingGroup(asBoolean(hm.get("repeating_group")));
        return toBean(entity);
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    @Override
    public EntityBean findByPK(int id) {
        return repository.findById(id)
                .map(this::toBean)
                .orElseGet(ItemGroupMetadataBean::new);
    }

    @Override
    public EntityBean findByItemAndCrfVersion(Integer itemId, Integer crfVersionId) {
        return repository.findByItemIdAndCrfVersionId(itemId, crfVersionId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemGroupMetadataBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemGroupMetadataEntity entity = new ItemGroupMetadataEntity();
        apply((ItemGroupMetadataBean) eb, entity);
        ItemGroupMetadataBean saved = toBean(repository.save(entity));
        eb.setId(saved.getId());
        return saved;
    }

    @Override
    public List<ItemGroupMetadataBean> findMetaByGroupAndSection(int itemGroupId, int crfVersionId, int sectionId) {
        return toBeans(repository.findMetaByGroupAndSection(itemGroupId, crfVersionId, sectionId));
    }

    @Override
    public List<ItemGroupMetadataBean> findMetaByGroupAndCrfVersion(int itemGroupId, int crfVersionId) {
        return toBeans(repository.findByItemGroupIdAndCrfVersionId(itemGroupId, crfVersionId));
    }

    @Override
    public List<ItemGroupMetadataBean> findMetaByGroupAndSectionForPrint(int itemGroupId, int crfVersionId, int sectionId) {
        return toBeans(repository.findMetaByGroupAndSectionForPrint(itemGroupId, crfVersionId, sectionId));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        ItemGroupMetadataBean bean = (ItemGroupMetadataBean) eb;
        ItemGroupMetadataEntity entity = repository.findById(bean.getId())
                .orElseGet(ItemGroupMetadataEntity::new);
        entity.setItemGroupMetadataId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        return toBean(repository.save(entity));
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
    public boolean versionIncluded(int crfVersionId) {
        return !repository.findByCrfVersionId(crfVersionId).isEmpty();
    }

    @Override
    public List<ItemGroupMetadataBean> findByCrfVersion(Integer crfVersionId) {
        return toBeans(repository.findByCrfVersionId(crfVersionId));
    }

    private void apply(ItemGroupMetadataBean bean, ItemGroupMetadataEntity entity) {
        entity.setItemGroupId(bean.getItemGroupId());
        entity.setHeader(bean.getHeader());
        entity.setSubheader(bean.getSubheader());
        entity.setLayout(bean.getLayout());
        entity.setRepeatNumber(bean.getRepeatNum());
        entity.setRepeatMax(bean.getRepeatMax());
        entity.setRepeatArray(bean.getRepeatArray());
        entity.setRowStartNumber(bean.getRowStartNumber());
        entity.setCrfVersionId(bean.getCrfVersionId());
        entity.setItemId(bean.getItemId());
        entity.setOrdinal(bean.getOrdinal());
        entity.setBorders(bean.getBorders());
        entity.setShowGroup(bean.isShowGroup());
        entity.setRepeatingGroup(bean.isRepeatingGroup());
    }

    private List<ItemGroupMetadataBean> toBeans(List<ItemGroupMetadataEntity> entities) {
        List<ItemGroupMetadataBean> beans = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(ItemGroupMetadataEntity::getOrdinal, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ItemGroupMetadataEntity::getItemGroupMetadataId, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private ItemGroupMetadataBean toBean(ItemGroupMetadataEntity entity) {
        ItemGroupMetadataBean bean = new ItemGroupMetadataBean();
        if (entity.getItemGroupMetadataId() != null) {
            bean.setId(entity.getItemGroupMetadataId());
        }
        bean.setItemGroupId(valueOrZero(entity.getItemGroupId()));
        bean.setHeader(valueOrEmpty(entity.getHeader()));
        bean.setSubheader(valueOrEmpty(entity.getSubheader()));
        bean.setLayout(valueOrEmpty(entity.getLayout()));
        bean.setRepeatNum(valueOrZero(entity.getRepeatNumber()));
        bean.setRepeatMax(valueOrZero(entity.getRepeatMax()));
        bean.setRepeatArray(valueOrEmpty(entity.getRepeatArray()));
        bean.setRowStartNumber(valueOrZero(entity.getRowStartNumber()));
        bean.setCrfVersionId(valueOrZero(entity.getCrfVersionId()));
        bean.setItemId(valueOrZero(entity.getItemId()));
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
        bean.setBorders(valueOrOne(entity.getBorders()));
        bean.setShowGroup(valueOrTrue(entity.getShowGroup()));
        bean.setRepeatingGroup(valueOrTrue(entity.getRepeatingGroup()));
        return bean;
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private Boolean asBoolean(Object value) {
        return value instanceof Boolean bool ? bool : null;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private int valueOrOne(Integer value) {
        return value != null ? value : 1;
    }

    private boolean valueOrTrue(Boolean value) {
        return value == null || value;
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
