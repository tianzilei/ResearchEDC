package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.dao.spi.IItemFormMetadataDAO;
import org.researchedc.dao.submit.ItemFormMetadataDAO;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("itemFormMetadataDAO")
@Primary
@Transactional(readOnly = true)
public class ItemFormMetadataDaoAdapter extends ItemFormMetadataDAO<String, ArrayList> implements IItemFormMetadataDAO {

    private final ItemFormMetadataRepository repository;

    public ItemFormMetadataDaoAdapter(ItemFormMetadataRepository repository, DataSource dataSource) {
        super(dataSource);
        this.repository = repository;
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        ItemFormMetadataEntity entity = new ItemFormMetadataEntity();
        entity.setItemFormMetadataId(asInteger(hm.get("item_form_metadata_id")));
        entity.setItemId(asInteger(hm.get("item_id")));
        entity.setCrfVersionId(asInteger(hm.get("crf_version_id")));
        entity.setSectionId(asInteger(hm.get("section_id")));
        entity.setOrdinal(asInteger(hm.get("ordinal")));
        entity.setRequired(asBoolean(hm.get("required")));
        entity.setDefaultValue((String) hm.get("default_value"));
        entity.setRegexp((String) hm.get("regexp"));
        entity.setRegexpErrorMsg((String) hm.get("regexp_error_msg"));
        entity.setResponseLayout((String) hm.get("response_layout"));
        entity.setWidthDecimal((String) hm.get("width_decimal"));
        entity.setShowItem(asBoolean(hm.get("show_item")));
        return toBean(entity);
    }

    @Override
    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public EntityBean findByPK(int id) {
        return repository.findById(id)
                .map(this::toBean)
                .orElseGet(ItemFormMetadataBean::new);
    }

    @Override
    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionId(int crfVersionId) {
        return toBeans(repository.findByCrfVersionIdOrderByOrdinal(crfVersionId));
    }

    @Override
    public ArrayList<ItemFormMetadataBean> findAllBySectionId(int sectionId) {
        return toBeans(repository.findBySectionIdOrderByOrdinal(sectionId));
    }

    @Override
    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndSectionId(int crfVersionId, int sectionId) {
        return toBeans(repository.findByCrfVersionIdAndSectionIdOrderByOrdinal(crfVersionId, sectionId));
    }

    @Override
    public ItemFormMetadataBean findByItemIdAndCRFVersionId(int itemId, int crfVersionId) {
        return repository.findByItemIdAndCrfVersionId(itemId, crfVersionId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemFormMetadataBean::new);
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemFormMetadataBean bean = (ItemFormMetadataBean) eb;
        ItemFormMetadataEntity entity = new ItemFormMetadataEntity();
        apply(bean, entity);
        ItemFormMetadataBean saved = toBean(repository.save(entity));
        eb.setId(saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        ItemFormMetadataBean bean = (ItemFormMetadataBean) eb;
        ItemFormMetadataEntity entity = repository.findById(bean.getId())
                .orElseGet(ItemFormMetadataEntity::new);
        entity.setItemFormMetadataId(bean.getId() > 0 ? bean.getId() : null);
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

    private void apply(ItemFormMetadataBean bean, ItemFormMetadataEntity entity) {
        entity.setItemId(bean.getItemId());
        entity.setCrfVersionId(bean.getCrfVersionId());
        entity.setSectionId(bean.getSectionId());
        entity.setOrdinal(bean.getOrdinal());
        entity.setRequired(bean.isRequired());
        entity.setDefaultValue(bean.getDefaultValue());
        entity.setRegexp(bean.getRegexp());
        entity.setRegexpErrorMsg(bean.getRegexpErrorMsg());
        entity.setResponseLayout(bean.getResponseLayout());
        entity.setWidthDecimal(bean.getWidthDecimal());
        entity.setShowItem(bean.isShowItem());
    }

    private ArrayList<ItemFormMetadataBean> toBeans(List<ItemFormMetadataEntity> entities) {
        ArrayList<ItemFormMetadataBean> beans = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(ItemFormMetadataEntity::getOrdinal, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private ItemFormMetadataBean toBean(ItemFormMetadataEntity entity) {
        ItemFormMetadataBean bean = new ItemFormMetadataBean();
        if (entity.getItemFormMetadataId() != null) {
            bean.setId(entity.getItemFormMetadataId());
        }
        bean.setItemId(valueOrZero(entity.getItemId()));
        bean.setCrfVersionId(valueOrZero(entity.getCrfVersionId()));
        bean.setSectionId(valueOrZero(entity.getSectionId()));
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
        bean.setRequired(valueOrFalse(entity.getRequired()));
        bean.setDefaultValue(valueOrEmpty(entity.getDefaultValue()));
        bean.setRegexp(valueOrEmpty(entity.getRegexp()));
        bean.setRegexpErrorMsg(valueOrEmpty(entity.getRegexpErrorMsg()));
        bean.setResponseLayout(valueOrEmpty(entity.getResponseLayout()));
        bean.setWidthDecimal(valueOrEmpty(entity.getWidthDecimal()));
        bean.setShowItem(valueOrFalse(entity.getShowItem()));
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

    private boolean valueOrFalse(Boolean value) {
        return value != null && value;
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
