package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.SectionBean;
import org.researchedc.dao.spi.ISectionDAO;
import org.researchedc.domain.datamap.Section;
import org.researchedc.module.crf.entity.SectionEntity;
import org.researchedc.module.crf.repository.SectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("sectionDAO")
@Primary
@Transactional(readOnly = true)
public class SectionDaoAdapter implements ISectionDAO {

    private final SectionRepository repository;
    private final DataSource dataSource;

    public SectionDaoAdapter(SectionRepository repository, DataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    @Override
    public void setTypesExpected() {
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        SectionEntity entity = new SectionEntity();
        entity.setSectionId(asInteger(hm.get("section_id")));
        entity.setCrfVersionId(asInteger(hm.get("crf_version_id")));
        entity.setLabel((String) hm.get("label"));
        entity.setTitle((String) hm.get("title"));
        entity.setSubtitle((String) hm.get("subtitle"));
        entity.setInstructions((String) hm.get("instructions"));
        entity.setPageNumberLabel((String) hm.get("page_number_label"));
        entity.setOrdinal(asInteger(hm.get("ordinal")));
        entity.setParentId(asInteger(hm.get("parent_id")));
        entity.setBorders(asInteger(hm.get("borders")));
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
    public Collection findByVersionId(int ID) {
        return toBeans(repository.findByCrfVersionIdOrderByOrdinal(ID));
    }

    @Override
    public EntityBean findByPK(int ID) {
        return repository.findById(ID)
                .map(this::toBean)
                .orElseGet(SectionBean::new);
    }

    @Override
    public ArrayList findAllByCRFVersionId(int crfVersionId) {
        ArrayList<SectionBean> result = new ArrayList<>();
        toBeans(repository.findByCrfVersionIdOrderByOrdinal(crfVersionId)).forEach(result::add);
        return result;
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        SectionBean bean = (SectionBean) eb;
        SectionEntity entity = new SectionEntity();
        apply(bean, entity);
        SectionBean saved = toBean(repository.save(entity));
        eb.setId(saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        SectionBean bean = (SectionBean) eb;
        SectionEntity entity = repository.findById(bean.getId())
                .orElseGet(SectionEntity::new);
        entity.setSectionId(bean.getId() > 0 ? bean.getId() : null);
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

    private static final Logger log = LoggerFactory.getLogger(SectionDaoAdapter.class);

    // ── Stub override for inherited method with zero callers ────────────

    @Override
    public void deleteTestSection(String label) {
        // No callers — no-op
    }

    // ── Simple JPA native query methods ─────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsBySectionId() {
        String sql = "SELECT section_id, COUNT(*) AS num_items FROM item_form_metadata GROUP BY section_id";
        return queryForSectionItemCounts(sql);
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getSectionIdForTabId(int crfVersionId, int tabId) {
        String sql = "SELECT section_id FROM section WHERE crf_version_id = ? AND ordinal = ?";
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, crfVersionId, tabId);
        HashMap result = new HashMap();
        if (!rows.isEmpty()) {
            result.putAll(rows.get(0));
        }
        return result;
    }

    // ── Complex analytical methods — all inlined from SectionDAO SQL ────

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsBySection(SectionBean sb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.event_crf_id = ? AND M.section_id = ? "
            + "GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, 0, sb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsPlusRepeatBySectionId(EventCRFBean ecb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND (D.status_id != 5 AND D.status_id != 7) "
            + "AND D.event_crf_id = ? GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsCompletedBySectionId(EventCRFBean ecb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.status_id = 2 "
            + "AND D.event_crf_id = ? GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsCompletedBySection(EventCRFBean ecb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.event_crf_id = ? "
            + "GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsPendingBySectionId(EventCRFBean ecb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.status_id = 4 "
            + "AND D.event_crf_id = ? GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsPendingBySection(EventCRFBean ecb, SectionBean sb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.event_crf_id = ? "
            + "AND M.section_id = ? GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId(), sb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsBlankBySectionId(EventCRFBean ecb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.status_id = 1 "
            + "AND D.event_crf_id = ? GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashMap getNumItemsBlankBySection(EventCRFBean ecb, SectionBean sb) {
        String sql = "SELECT M.section_id, COUNT(*) AS num_items "
            + "FROM item_data D, item_form_metadata M "
            + "WHERE D.item_id = M.item_id AND D.event_crf_id = ? "
            + "AND M.section_id = ? GROUP BY M.section_id";
        return queryForSectionItemCounts(sql, ecb.getId(), sb.getId());
    }

    @Override
    public SectionBean findNext(EventCRFBean ecb, SectionBean current) {
        String sql = "SELECT n.* FROM section n, section c "
            + "WHERE n.crf_version_id = c.crf_version_id "
            + "AND n.ordinal = c.ordinal + 1 "
            + "AND c.crf_version_id = ? AND c.ordinal = ?";
        return findSectionBySql(sql, ecb.getCRFVersionId(), current.getOrdinal());
    }

    @Override
    public SectionBean findPrevious(EventCRFBean ecb, SectionBean current) {
        String sql = "SELECT n.* FROM section n, section c "
            + "WHERE n.crf_version_id = c.crf_version_id "
            + "AND n.ordinal = c.ordinal - 1 "
            + "AND c.crf_version_id = ? AND c.ordinal = ?";
        return findSectionBySql(sql, ecb.getCRFVersionId(), current.getOrdinal());
    }

    @Override
    public boolean hasSCDItem(Integer sectionId) {
        return countSCDItemBySectionId(sectionId) > 0;
    }

    @Override
    public int countSCDItemBySectionId(Integer sectionId) {
        String sql = "SELECT COUNT(scd.id) FROM scd_item_metadata scd "
            + "WHERE scd.scd_item_form_metadata_id IN "
            + "(SELECT ifm.item_form_metadata_id FROM item_form_metadata ifm WHERE ifm.section_id = ?)";
        Integer count = getJdbcTemplate().queryForObject(sql, Integer.class, sectionId);
        return count != null ? count : 0;
    }

    @Override
    public boolean containNormalItem(Integer crfVersionId, Integer sectionId) {
        String sql = "SELECT ifm.item_id FROM item_form_metadata ifm "
            + "WHERE ifm.section_id = ? AND ifm.crf_version_id = ? "
            + "AND ifm.show_item = 't' "
            + "AND ifm.item_id NOT IN ("
            + "SELECT DISTINCT igm.item_id FROM item_group_metadata igm "
            + "WHERE igm.crf_version_id = ? AND igm.show_group = 'f' "
            + "AND igm.item_id IN ("
            + "SELECT im.item_id FROM item_form_metadata im "
            + "WHERE im.section_id = ? AND im.crf_version_id = ?)) "
            + "LIMIT 1";
        List<Integer> results = getJdbcTemplate().queryForList(sql, Integer.class,
            sectionId, crfVersionId, crfVersionId, sectionId, crfVersionId);
        return !results.isEmpty() && results.get(0) > 0;
    }

    // ── JdbcTemplate-backed native SQL helpers ──────────────────────────

    private JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @SuppressWarnings("unchecked")
    private SectionBean findSectionBySql(String sql, Object... params) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, params);
        if (!rows.isEmpty()) {
            HashMap hm = new HashMap();
            hm.putAll(rows.get(0));
            return toBeanFromRow(hm);
        }
        return new SectionBean();
    }

    private SectionBean toBeanFromRow(HashMap hm) {
        SectionEntity entity = new SectionEntity();
        entity.setSectionId(asInteger(hm.get("section_id")));
        entity.setCrfVersionId(asInteger(hm.get("crf_version_id")));
        entity.setLabel((String) hm.get("label"));
        entity.setTitle((String) hm.get("title"));
        entity.setSubtitle((String) hm.get("subtitle"));
        entity.setInstructions((String) hm.get("instructions"));
        entity.setPageNumberLabel((String) hm.get("page_number_label"));
        entity.setOrdinal(asInteger(hm.get("ordinal")));
        entity.setParentId(asInteger(hm.get("parent_id")));
        entity.setBorders(asInteger(hm.get("borders")));
        return toBean(entity);
    }

    @SuppressWarnings("unchecked")
    private HashMap queryForSectionItemCounts(String sql, Object... params) {
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql, params);
        HashMap<Integer, Integer> answer = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer sectionId = row.get("section_id") instanceof Number n ? n.intValue() : null;
            Integer numItems = row.get("num_items") instanceof Number n ? n.intValue() : 0;
            if (sectionId != null) {
                answer.put(sectionId, numItems);
            }
        }
        return answer;
    }

    /*
     * Complex analytical methods below are NOT overridden —
     * they delegate to the parent SectionDAO (which uses the legacy `section` table via SQL).
     * The bidirectional sync triggers keep legacy and module tables in sync.
     */

    private void apply(SectionBean bean, SectionEntity entity) {
        entity.setCrfVersionId(bean.getCRFVersionId());
        entity.setLabel(bean.getLabel());
        entity.setTitle(bean.getTitle());
        entity.setSubtitle(bean.getSubtitle());
        entity.setInstructions(bean.getInstructions());
        entity.setPageNumberLabel(bean.getPageNumberLabel());
        entity.setOrdinal(bean.getOrdinal());
        entity.setParentId(bean.getParentId());
        entity.setBorders(bean.getBorders());
    }

    private List<SectionBean> toBeans(List<SectionEntity> entities) {
        List<SectionBean> beans = new ArrayList<>();
        entities.stream()
                .sorted(Comparator.comparing(SectionEntity::getOrdinal, Comparator.nullsLast(Integer::compareTo)))
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private SectionBean toBean(SectionEntity entity) {
        SectionBean bean = new SectionBean();
        if (entity.getSectionId() != null) {
            bean.setId(entity.getSectionId());
        }
        bean.setCRFVersionId(valueOrZero(entity.getCrfVersionId()));
        bean.setLabel(valueOrEmpty(entity.getLabel()));
        bean.setTitle(valueOrEmpty(entity.getTitle()));
        bean.setSubtitle(valueOrEmpty(entity.getSubtitle()));
        bean.setInstructions(valueOrEmpty(entity.getInstructions()));
        bean.setPageNumberLabel(valueOrEmpty(entity.getPageNumberLabel()));
        bean.setOrdinal(valueOrZero(entity.getOrdinal()));
        bean.setParentId(valueOrZero(entity.getParentId()));
        bean.setBorders(valueOrZero(entity.getBorders()));
        return bean;
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }
}
