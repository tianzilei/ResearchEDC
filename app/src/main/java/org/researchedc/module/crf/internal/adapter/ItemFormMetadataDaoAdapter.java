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
import org.researchedc.bean.submit.ItemFormMetadataBean.ResponseSetBean;
import org.researchedc.module.dataimport.service.ImportItemFormMetadataPort;
import org.researchedc.module.dataimport.dto.ImportItemFormMetadata;
import org.researchedc.module.crf.entity.ItemFormMetadataEntity;
import org.researchedc.module.crf.repository.ItemFormMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("itemFormMetadataDAO")
@Primary
@Transactional(readOnly = true)
public class ItemFormMetadataDaoAdapter implements ImportItemFormMetadataPort {

    public record InstantOnChangePair(Integer destItemFormMetadataId,
                                      Integer destSectionId,
                                      String destItemGroupOid,
                                      Integer destItemId,
                                      Boolean destRepeating,
                                      Boolean destUngrouped,
                                      String optionValue,
                                      Integer originSectionId,
                                      String originItemGroupOid,
                                      Integer originItemId,
                                      Boolean originRepeating,
                                      Boolean originUngrouped) {
    }

    private final ItemFormMetadataRepository repository;
    private final DataSource dataSource;

    public ItemFormMetadataDaoAdapter(ItemFormMetadataRepository repository, DataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    public void setTypesExpected() {
    }

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

    public Collection findAll() {
        return toBeans(repository.findAll());
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    public EntityBean findByPK(int id) {
        return repository.findById(id)
                .map(this::toBean)
                .orElseGet(ItemFormMetadataBean::new);
    }

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionId(int crfVersionId) {
        return toBeans(repository.findByCrfVersionIdOrderByOrdinal(crfVersionId));
    }

    public ArrayList<ItemFormMetadataBean> findAllBySectionId(int sectionId) {
        return toBeans(repository.findBySectionIdOrderByOrdinal(sectionId));
    }

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndSectionId(int crfVersionId, int sectionId) {
        return toBeans(repository.findByCrfVersionIdAndSectionIdOrderByOrdinal(crfVersionId, sectionId));
    }

    public ItemFormMetadataBean findByItemIdAndCRFVersionId(int itemId, int crfVersionId) {
        return repository.findByItemIdAndCrfVersionId(itemId, crfVersionId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemFormMetadataBean::new);
    }

    @Transactional
    public EntityBean create(EntityBean eb) {
        ItemFormMetadataBean bean = (ItemFormMetadataBean) eb;
        ItemFormMetadataEntity entity = new ItemFormMetadataEntity();
        apply(bean, entity);
        ItemFormMetadataBean saved = toBean(repository.save(entity));
        eb.setId(saved.getId());
        return saved;
    }

    @Transactional
    public EntityBean update(EntityBean eb) {
        ItemFormMetadataBean bean = (ItemFormMetadataBean) eb;
        ItemFormMetadataEntity entity = repository.findById(bean.getId())
                .orElseGet(ItemFormMetadataEntity::new);
        entity.setItemFormMetadataId(bean.getId() > 0 ? bean.getId() : null);
        apply(bean, entity);
        return toBean(repository.save(entity));
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType,
                                           String strOrderByColumn, boolean blnAscendingSort,
                                           String strSearchPhrase) {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    // ── Stub overrides for inherited methods with zero callers ──────────

    public int findCountAllHiddenByCRFVersionId(int crfVersionId) {
        // No callers — method was used by legacy CRF workflow only
        return 0;
    }

    public int findCountAllHiddenButShownByEventCRFId(int eventCrfId) {
        // No callers — method was used by legacy CRF workflow only
        return 0;
    }

    public ItemFormMetadataBean findByItemIdAndCRFVersionIdNotInIGM(int itemId, int crfVersionId) {
        // No callers — returns empty bean
        return new ItemFormMetadataBean();
    }

    // ── Simple JPA native query methods ─────────────────────────────────

    private static final Logger log = LoggerFactory.getLogger(ItemFormMetadataDaoAdapter.class);

    public int findMaxId() {
        String sql = "SELECT COALESCE(MAX(ifm.item_form_metadata_id), 0) FROM item_form_metadata ifm";
        Integer result = getJdbcTemplate().queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndResponseTypeId(int crfVersionId, int responseTypeId) {
        // Legacy item-form-metadata SQL lineage: findAllByCRFVersionIdAndResponseTypeId
        String sql = "SELECT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.crf_version_id = ? AND m.response_set_id = rs.response_set_id AND rs.response_type_id = ?";
        return findAllBySql(sql, crfVersionId, responseTypeId);
    }

    public ArrayList<ItemFormMetadataBean> findAllItemsRequiredAndShownByCrfVersionId(int crfVersionId) {
        // Legacy item-form-metadata SQL lineage: findAllItemsRequiredAndShownByCrfVersionId
        String sql = "SELECT * FROM item_form_metadata WHERE crf_version_id = ? AND required = true AND show_item = true";
        return findAllBySql(sql, crfVersionId);
    }

    public ArrayList<ItemFormMetadataBean> findAllItemsRequiredAndHiddenByCrfVersionId(int crfVersionId) {
        // Legacy item-form-metadata SQL lineage: findAllItemsRequiredAndHiddenByCrfVersionId
        String sql = "SELECT * FROM item_form_metadata WHERE crf_version_id = ? AND required = true AND show_item = false";
        return findAllBySql(sql, crfVersionId);
    }

    public ArrayList<ItemFormMetadataBean> findAllByCRFIdItemIdAndHasValidations(int crfId, int itemId) {
        // Legacy item-form-metadata SQL lineage: findAllByCRFIdItemIdAndHasValidations
        String sql = "SELECT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.crf_version_id IN (SELECT crf_version_id FROM crf_version WHERE crf_id = ?) "
            + "AND m.response_set_id = rs.response_set_id "
            + "AND m.item_id = ? "
            + "AND m.regexp != ''";
        return findAllBySql(sql, crfId, itemId);
    }

    public ArrayList<ItemFormMetadataBean> findAllByItemId(int itemId) {
        // Legacy item-form-metadata SQL lineage: findAllByItemId
        String sql = "SELECT DISTINCT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.item_id = ? "
            + "AND m.response_set_id = rs.response_set_id";
        return findAllBySql(sql, itemId);
    }

    public List<ImportItemFormMetadata> findImportItemFormMetadataByItemId(int itemId) {
        String sql = "SELECT DISTINCT m.width_decimal, rs.response_type_id, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs, crf_version cv, "
            + "item_group_metadata igm, item_group ig, section sec "
            + "WHERE m.item_id = ? "
            + "AND m.response_set_id = rs.response_set_id "
            + "AND cv.crf_version_id = m.crf_version_id "
            + "AND igm.item_id = m.item_id "
            + "AND ig.item_group_id = igm.item_group_id "
            + "AND sec.section_id = m.section_id";
        return getJdbcTemplate().query(
                sql,
                (rs, rowNum) -> new ImportItemFormMetadata(
                        rs.getString("width_decimal"),
                        rs.getObject("response_type_id", Integer.class),
                        rs.getString("options_text"),
                        rs.getString("options_values")),
                itemId);
    }

    public ArrayList<ItemFormMetadataBean> findAllByItemIdAndHasValidations(int itemId) {
        // Legacy item-form-metadata SQL lineage: findAllByItemIdAndHasValidations
        String sql = "SELECT DISTINCT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.item_id = ? "
            + "AND m.response_set_id = rs.response_set_id "
            + "AND m.regexp != ''";
        return findAllBySql(sql, itemId);
    }

    public ArrayList<ItemFormMetadataBean> findSCDItemsBySectionId(Integer sectionId) {
        // Legacy item-form-metadata SQL lineage: findSCDItemsBySectionId
        String sql = "SELECT ifm.* FROM item_form_metadata ifm "
            + "WHERE ifm.section_id = ? "
            + "AND EXISTS (SELECT s.scd_item_form_metadata_id FROM scd_item_metadata s "
            + "WHERE s.scd_item_form_metadata_id = ifm.item_form_metadata_id)";
        return findAllBySql(sql, sectionId);
    }

    public boolean instantTypeExistsInSection(int sectionId) {
        // Legacy item-form-metadata SQL lineage: instantTypeExistsInSection
        String sql = "SELECT ifm.item_form_metadata_id FROM item_form_metadata ifm, response_set rs "
            + "WHERE rs.response_type_id = 10 AND ifm.section_id = ? "
            + "AND ifm.response_set_id = rs.response_set_id LIMIT 1";
        List<Integer> results = getJdbcTemplate().queryForList(sql, Integer.class, sectionId);
        return !results.isEmpty() && results.get(0) > 0;
    }

    public Map<Integer, List<InstantOnChangePair>> sectionInstantMapInSameSection(int crfVersionId) {
        // Stub — blocked on Phase 1 P8 removing DataEntryServlet callers.
        // This is the most complex inherited method (10 joins, 5 params).
        // TODO: implement with JPA native query after Phase 1 P8 data entry servlet deletion.
        return new HashMap<>();
    }

    public ArrayList<ItemFormMetadataBean> findByMultiplePKs(ArrayList ints) {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<>();
        for (Object pk : ints) {
            int id = (pk instanceof Number n) ? n.intValue() : Integer.parseInt(pk.toString());
            answer.add((ItemFormMetadataBean) findByPK(id));
        }
        return answer;
    }

    public ResponseSetBean findResponseSetByPK(int id) {
        String sql = "SELECT rs.*, rt.* FROM response_set rs, response_type rt "
            + "WHERE rs.response_type_id = rt.response_type_id AND rs.response_set_id = ?";
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, id);
        if (!rows.isEmpty()) {
            HashMap hm = new HashMap();
            hm.putAll(rows.get(0));
            return responseSetFromRow(hm);
        }
        return new ResponseSetBean();
    }

    // ── JdbcTemplate-backed native SQL helpers ──────────────────────────

    private JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    private ItemFormMetadataBean ifmBeanFromRow(HashMap hm) {
        ItemFormMetadataBean answer = new ItemFormMetadataBean();
        answer.setId(getInt(hm, "item_form_metadata_id"));
        answer.setItemId(getInt(hm, "item_id"));
        answer.setCrfVersionId(getInt(hm, "crf_version_id"));
        answer.setHeader(getString(hm, "header"));
        answer.setSubHeader(getString(hm, "subheader"));
        answer.setParentId(getInt(hm, "parent_id"));
        answer.setParentLabel(getString(hm, "parent_label"));
        answer.setColumnNumber(getInt(hm, "column_number"));
        answer.setPageNumberLabel(getString(hm, "page_number_label"));
        answer.setQuestionNumberLabel(getString(hm, "question_number_label"));
        answer.setLeftItemText(getString(hm, "left_item_text"));
        answer.setRightItemText(getString(hm, "right_item_text"));
        answer.setSectionId(getInt(hm, "section_id"));
        answer.setDescisionConditionId(getInt(hm, "decision_condition_id"));
        answer.setResponseSetId(getInt(hm, "response_set_id"));
        answer.setRegexp(getString(hm, "regexp"));
        answer.setRegexpErrorMsg(getString(hm, "regexp_error_msg"));
        answer.setOrdinal(getInt(hm, "ordinal"));
        answer.setRequired(getBoolean(hm, "required"));
        answer.setDefaultValue(getString(hm, "default_value"));
        answer.setResponseLayout(getString(hm, "response_layout"));
        answer.setWidthDecimal(getString(hm, "width_decimal"));
        answer.setShowItem(getBoolean(hm, "show_item"));
        ResponseSetBean rsb = new ResponseSetBean();
        rsb.setId(getInt(hm, "response_set_id"));
        rsb.setLabel(getString(hm, "label"));
        rsb.setResponseTypeId(getInt(hm, "response_type_id"));
        rsb.setOptions(getString(hm, "options_text"), getString(hm, "options_values"));
        answer.setResponseSet(rsb);
        return answer;
    }

    private ResponseSetBean responseSetFromRow(HashMap hm) {
        ResponseSetBean rsb = new ResponseSetBean();
        rsb.setId(getInt(hm, "response_set_id"));
        rsb.setLabel(getString(hm, "label"));
        rsb.setResponseTypeId(getInt(hm, "response_type_id"));
        rsb.setOptions(getString(hm, "options_text"), getString(hm, "options_values"));
        return rsb;
    }

    private int getInt(HashMap row, String column) {
        Object val = row.get(column);
        return val instanceof Number n ? n.intValue() : 0;
    }

    private boolean getBoolean(HashMap row, String column) {
        Object val = row.get(column);
        return val instanceof Boolean b ? b : false;
    }

    private String getString(HashMap row, String column) {
        Object val = row.get(column);
        return val != null ? val.toString() : "";
    }

    /**
     * Execute a native SQL query and convert rows to ItemFormMetadataBean list.
     */
    private ArrayList<ItemFormMetadataBean> findAllBySql(String sql, Object... params) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, params);
        ArrayList<ItemFormMetadataBean> beans = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            HashMap hm = new HashMap();
            hm.putAll(row);
            beans.add(ifmBeanFromRow(hm));
        }
        return beans;
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
