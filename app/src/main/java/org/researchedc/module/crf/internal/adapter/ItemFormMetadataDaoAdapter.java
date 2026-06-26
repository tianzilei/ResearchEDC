package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.researchedc.module.crf.dto.ItemFormMetadataDto;
import org.researchedc.module.crf.dto.ItemFormMetadataDto.ResponseSetDto;
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

    public ItemFormMetadataDto findByPK(int id) {
        return repository.findById(id)
                .map(this::toBean)
                .orElseGet(ItemFormMetadataDto::new);
    }

    public ArrayList<ItemFormMetadataDto> findAllByCRFVersionId(int crfVersionId) {
        return toBeans(repository.findByCrfVersionIdOrderByOrdinal(crfVersionId));
    }

    public ArrayList<ItemFormMetadataDto> findAllBySectionId(int sectionId) {
        return toBeans(repository.findBySectionIdOrderByOrdinal(sectionId));
    }

    public ArrayList<ItemFormMetadataDto> findAllByCRFVersionIdAndSectionId(int crfVersionId, int sectionId) {
        return toBeans(repository.findByCrfVersionIdAndSectionIdOrderByOrdinal(crfVersionId, sectionId));
    }

    public ItemFormMetadataDto findByItemIdAndCRFVersionId(int itemId, int crfVersionId) {
        return repository.findByItemIdAndCrfVersionId(itemId, crfVersionId).stream()
                .findFirst()
                .map(this::toBean)
                .orElseGet(ItemFormMetadataDto::new);
    }

    @Transactional
    public ItemFormMetadataDto create(ItemFormMetadataDto dto) {
        ItemFormMetadataEntity entity = new ItemFormMetadataEntity();
        apply(dto, entity);
        ItemFormMetadataDto saved = toBean(repository.save(entity));
        dto.setId(saved.getId());
        return saved;
    }

    @Transactional
    public ItemFormMetadataDto update(ItemFormMetadataDto dto) {
        ItemFormMetadataEntity entity = repository.findById(dto.getId())
                .orElseGet(ItemFormMetadataEntity::new);
        entity.setItemFormMetadataId(dto.getId() > 0 ? dto.getId() : null);
        apply(dto, entity);
        return toBean(repository.save(entity));
    }

    // ── Simple JPA native query methods ─────────────────────────────────

    private static final Logger log = LoggerFactory.getLogger(ItemFormMetadataDaoAdapter.class);

    public int findMaxId() {
        String sql = "SELECT COALESCE(MAX(ifm.item_form_metadata_id), 0) FROM item_form_metadata ifm";
        Integer result = getJdbcTemplate().queryForObject(sql, Integer.class);
        return result != null ? result : 0;
    }

    public ArrayList<ItemFormMetadataDto> findAllByCRFVersionIdAndResponseTypeId(int crfVersionId, int responseTypeId) {
        String sql = "SELECT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.crf_version_id = ? AND m.response_set_id = rs.response_set_id AND rs.response_type_id = ?";
        return findAllBySql(sql, crfVersionId, responseTypeId);
    }

    public ArrayList<ItemFormMetadataDto> findAllItemsRequiredAndShownByCrfVersionId(int crfVersionId) {
        String sql = "SELECT * FROM item_form_metadata WHERE crf_version_id = ? AND required = true AND show_item = true";
        return findAllBySql(sql, crfVersionId);
    }

    public ArrayList<ItemFormMetadataDto> findAllItemsRequiredAndHiddenByCrfVersionId(int crfVersionId) {
        String sql = "SELECT * FROM item_form_metadata WHERE crf_version_id = ? AND required = true AND show_item = false";
        return findAllBySql(sql, crfVersionId);
    }

    public ArrayList<ItemFormMetadataDto> findAllByCRFIdItemIdAndHasValidations(int crfId, int itemId) {
        String sql = "SELECT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.crf_version_id IN (SELECT crf_version_id FROM crf_version WHERE crf_id = ?) "
            + "AND m.response_set_id = rs.response_set_id "
            + "AND m.item_id = ? "
            + "AND m.regexp != ''";
        return findAllBySql(sql, crfId, itemId);
    }

    public ArrayList<ItemFormMetadataDto> findAllByItemId(int itemId) {
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

    public ArrayList<ItemFormMetadataDto> findAllByItemIdAndHasValidations(int itemId) {
        String sql = "SELECT DISTINCT m.*, rs.response_type_id, rs.label, rs.options_text, rs.options_values "
            + "FROM item_form_metadata m, response_set rs "
            + "WHERE m.item_id = ? "
            + "AND m.response_set_id = rs.response_set_id "
            + "AND m.regexp != ''";
        return findAllBySql(sql, itemId);
    }

    public ArrayList<ItemFormMetadataDto> findSCDItemsBySectionId(Integer sectionId) {
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

    public ArrayList<ItemFormMetadataDto> findByMultiplePKs(ArrayList ints) {
        ArrayList<ItemFormMetadataDto> answer = new ArrayList<>();
        for (Object pk : ints) {
            int id = (pk instanceof Number n) ? n.intValue() : Integer.parseInt(pk.toString());
            answer.add(findByPK(id));
        }
        return answer;
    }

    public ResponseSetDto findResponseSetByPK(int id) {
        String sql = "SELECT rs.*, rt.* FROM response_set rs, response_type rt "
            + "WHERE rs.response_type_id = rt.response_type_id AND rs.response_set_id = ?";
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, id);
        if (!rows.isEmpty()) {
            HashMap hm = new HashMap();
            hm.putAll(rows.get(0));
            return responseSetFromRow(hm);
        }
        return new ResponseSetDto();
    }

    // ── JdbcTemplate-backed native SQL helpers ──────────────────────────

    private JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    private ItemFormMetadataDto ifmBeanFromRow(HashMap hm) {
        ItemFormMetadataDto answer = new ItemFormMetadataDto();
        answer.setId(getInt(hm, "item_form_metadata_id"));
        answer.setItemId(getInt(hm, "item_id"));
        answer.setCrfVersionId(getInt(hm, "crf_version_id"));
        answer.setSectionId(getInt(hm, "section_id"));
        answer.setOrdinal(getInt(hm, "ordinal"));
        answer.setRequired(getBoolean(hm, "required"));
        answer.setDefaultValue(getString(hm, "default_value"));
        answer.setRegexp(getString(hm, "regexp"));
        answer.setRegexpErrorMsg(getString(hm, "regexp_error_msg"));
        answer.setResponseLayout(getString(hm, "response_layout"));
        answer.setWidthDecimal(getString(hm, "width_decimal"));
        answer.setShowItem(getBoolean(hm, "show_item"));
        answer.setResponseSetId(getInt(hm, "response_set_id"));
        ResponseSetDto rsb = new ResponseSetDto();
        rsb.setId(getInt(hm, "response_set_id"));
        rsb.setLabel(getString(hm, "label"));
        rsb.setResponseTypeId(getInt(hm, "response_type_id"));
        rsb.setOptions(getString(hm, "options_text"), getString(hm, "options_values"));
        answer.setResponseSet(rsb);
        return answer;
    }

    private ResponseSetDto responseSetFromRow(HashMap hm) {
        ResponseSetDto rsb = new ResponseSetDto();
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

    private ArrayList<ItemFormMetadataDto> findAllBySql(String sql, Object... params) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, params);
        ArrayList<ItemFormMetadataDto> dtos = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            HashMap hm = new HashMap();
            hm.putAll(row);
            dtos.add(ifmBeanFromRow(hm));
        }
        return dtos;
    }

    private void apply(ItemFormMetadataDto dto, ItemFormMetadataEntity entity) {
        entity.setItemId(dto.getItemId());
        entity.setCrfVersionId(dto.getCrfVersionId());
        entity.setSectionId(dto.getSectionId());
        entity.setOrdinal(dto.getOrdinal());
        entity.setRequired(dto.isRequired());
        entity.setDefaultValue(dto.getDefaultValue());
        entity.setRegexp(dto.getRegexp());
        entity.setRegexpErrorMsg(dto.getRegexpErrorMsg());
        entity.setResponseLayout(dto.getResponseLayout());
        entity.setWidthDecimal(dto.getWidthDecimal());
        entity.setShowItem(dto.isShowItem());
    }

    private ArrayList<ItemFormMetadataDto> toBeans(List<ItemFormMetadataEntity> entities) {
        ArrayList<ItemFormMetadataDto> dtos = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(ItemFormMetadataEntity::getOrdinal, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(dtos::add);
        return dtos;
    }

    private ItemFormMetadataDto toBean(ItemFormMetadataEntity entity) {
        ItemFormMetadataDto dto = new ItemFormMetadataDto();
        if (entity.getItemFormMetadataId() != null) {
            dto.setId(entity.getItemFormMetadataId());
        }
        dto.setItemId(valueOrZero(entity.getItemId()));
        dto.setCrfVersionId(valueOrZero(entity.getCrfVersionId()));
        dto.setSectionId(valueOrZero(entity.getSectionId()));
        dto.setOrdinal(valueOrZero(entity.getOrdinal()));
        dto.setRequired(valueOrFalse(entity.getRequired()));
        dto.setDefaultValue(valueOrEmpty(entity.getDefaultValue()));
        dto.setRegexp(valueOrEmpty(entity.getRegexp()));
        dto.setRegexpErrorMsg(valueOrEmpty(entity.getRegexpErrorMsg()));
        dto.setResponseLayout(valueOrEmpty(entity.getResponseLayout()));
        dto.setWidthDecimal(valueOrEmpty(entity.getWidthDecimal()));
        dto.setShowItem(valueOrFalse(entity.getShowItem()));
        return dto;
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
