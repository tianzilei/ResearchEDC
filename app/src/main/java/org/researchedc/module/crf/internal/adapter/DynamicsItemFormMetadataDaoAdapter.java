package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemDataBean;
import org.researchedc.bean.submit.ItemFormMetadataBean;
import org.researchedc.dao.spi.DynamicsItemFormMetadataDao;
import org.researchedc.domain.crfdata.DynamicsItemFormMetadataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("dynamicsItemFormMetadataDao")
@Primary
@Transactional(readOnly = true)
public class DynamicsItemFormMetadataDaoAdapter implements DynamicsItemFormMetadataDao {

    private static final Logger log = LoggerFactory.getLogger(DynamicsItemFormMetadataDaoAdapter.class);

    private final JdbcTemplate jdbc;

    private final RowMapper<DynamicsItemFormMetadataBean> rowMapper = (rs, rowNum) -> {
        DynamicsItemFormMetadataBean bean = new DynamicsItemFormMetadataBean();
        bean.setId(rs.getInt("id"));
        bean.setShowItem(rs.getBoolean("show_item"));
        bean.setEventCrfId(rs.getInt("event_crf_id"));
        bean.setItemId(rs.getInt("item_id"));
        bean.setItemFormMetadataId(rs.getInt("item_form_metadata_id"));
        bean.setCrfVersionId(rs.getInt("crf_version_id"));
        bean.setItemDataId(rs.getInt("item_data_id"));
        bean.setPassedDde(rs.getInt("passed_dde"));
        return bean;
    };

    public DynamicsItemFormMetadataDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public DynamicsItemFormMetadataBean findByMetadataBean(ItemFormMetadataBean metadataBean,
            EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        String sql = "SELECT * FROM dyn_item_form_metadata "
                + "WHERE item_id = ? AND event_crf_id = ? AND item_data_id = ? "
                + "ORDER BY id DESC";
        List<DynamicsItemFormMetadataBean> results = jdbc.query(sql, rowMapper,
                metadataBean.getItemId(), eventCrfBean.getId(), itemDataBean.getId());
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public ArrayList<DynamicsItemFormMetadataBean> findByItemAndEventCrfShown(EventCRFBean eventCrfBean, int itemId) {
        String sql = "SELECT * FROM dyn_item_form_metadata "
                + "WHERE item_id = ? AND event_crf_id = ? AND show_item = true "
                + "ORDER BY id DESC";
        return new ArrayList<>(jdbc.query(sql, rowMapper, itemId, eventCrfBean.getId()));
    }

    @Override
    public DynamicsItemFormMetadataBean findByItemDataBean(ItemDataBean itemDataBean) {
        String sql = "SELECT * FROM dyn_item_form_metadata WHERE item_data_id = ?";
        List<DynamicsItemFormMetadataBean> results = jdbc.query(sql, rowMapper, itemDataBean.getId());
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Integer> findItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT DISTINCT ditem.item_id FROM dyn_item_form_metadata ditem "
                + "WHERE ditem.item_data_id IN ("
                + " SELECT idata.item_data_id FROM item_data idata"
                + " WHERE idata.event_crf_id = ? AND idata.item_id IN ("
                + "  SELECT DISTINCT igm.item_id FROM item_group_metadata igm"
                + "  WHERE igm.item_group_id = ? AND igm.crf_version_id = ? AND igm.item_id IN ("
                + "   SELECT ifm.item_id FROM item_form_metadata ifm WHERE ifm.show_item = 'false' AND ifm.section_id = ?"
                + "   AND ifm.crf_version_id = ?))"
                + " AND (idata.status_id != 5 AND idata.status_id != 7))";
        return jdbc.queryForList(sql, Integer.class, eventCrfId, groupId, crfVersionId, sectionId, crfVersionId);
    }

    @Override
    public List<Integer> findShowItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT DISTINCT ditem.item_id FROM dyn_item_form_metadata ditem "
                + "WHERE ditem.item_data_id IN ("
                + " SELECT idata.item_data_id FROM item_data idata"
                + " WHERE idata.event_crf_id = ? AND idata.item_id IN ("
                + "  SELECT DISTINCT igm.item_id FROM item_group_metadata igm"
                + "  WHERE igm.item_group_id = ? AND igm.crf_version_id = ? AND igm.item_id IN ("
                + "   SELECT ifm.item_id FROM item_form_metadata ifm WHERE ifm.show_item = 'false' AND ifm.section_id = ?"
                + "   AND ifm.crf_version_id = ?))"
                + " AND (idata.status_id != 5 AND idata.status_id != 7))"
                + " AND ditem.show_item = 'true'";
        return jdbc.queryForList(sql, Integer.class, eventCrfId, groupId, crfVersionId, sectionId, crfVersionId);
    }

    @Override
    public List<Integer> findShowItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT ditem.item_data_id FROM dyn_item_form_metadata ditem "
                + "WHERE ditem.item_data_id IN ("
                + " SELECT idata.item_data_id FROM item_data idata"
                + " WHERE idata.event_crf_id = ? AND idata.item_id IN ("
                + "  SELECT DISTINCT igm.item_id FROM item_group_metadata igm"
                + "  WHERE igm.item_group_id = ? AND igm.crf_version_id = ? AND igm.item_id IN ("
                + "   SELECT ifm.item_id FROM item_form_metadata ifm WHERE ifm.show_item = 'false' AND ifm.section_id = ?"
                + "   AND ifm.crf_version_id = ?))"
                + " AND (idata.status_id != 5 AND idata.status_id != 7))"
                + " AND ditem.show_item = 'true'";
        return jdbc.queryForList(sql, Integer.class, eventCrfId, groupId, crfVersionId, sectionId, crfVersionId);
    }

    @Override
    public List<Integer> findHideItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT ditem.item_data_id FROM dyn_item_form_metadata ditem "
                + "WHERE ditem.item_data_id IN ("
                + " SELECT idata.item_data_id FROM item_data idata"
                + " WHERE idata.event_crf_id = ? AND idata.item_id IN ("
                + "  SELECT DISTINCT igm.item_id FROM item_group_metadata igm"
                + "  WHERE igm.item_group_id = ? AND igm.crf_version_id = ? AND igm.item_id IN ("
                + "   SELECT ifm.item_id FROM item_form_metadata ifm WHERE ifm.show_item = 'false' AND ifm.section_id = ?"
                + "   AND ifm.crf_version_id = ?))"
                + " AND (idata.status_id != 5 AND idata.status_id != 7))"
                + " AND ditem.show_item = 'false'";
        return jdbc.queryForList(sql, Integer.class, eventCrfId, groupId, crfVersionId, sectionId, crfVersionId);
    }

    @Override
    public List<Integer> findShowItemDataIdsInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT ditem.item_data_id FROM dyn_item_form_metadata ditem "
                + "WHERE ditem.item_data_id IN ("
                + " SELECT idata.item_data_id FROM item_data idata"
                + " WHERE idata.event_crf_id = ? AND idata.item_id IN ("
                + "  SELECT ifm.item_id FROM item_form_metadata ifm WHERE ifm.show_item = 'false' AND ifm.section_id = ?"
                + "  AND ifm.crf_version_id = ?)"
                + " AND (idata.status_id != 5 AND idata.status_id != 7))"
                + " AND ditem.show_item = 'true'";
        return jdbc.queryForList(sql, Integer.class, eventCrfId, sectionId, crfVersionId);
    }

    @Override
    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT di.item_id FROM dyn_item_form_metadata di WHERE di.item_data_id IN ("
                + " SELECT ida.item_data_id FROM item_data ida WHERE ida.event_crf_id = ? AND ida.item_id IN"
                + "  (SELECT ifm.item_id FROM item_form_metadata ifm WHERE ifm.section_id = ? AND ifm.crf_version_id = ?"
                + "   AND ifm.item_id NOT IN (SELECT DISTINCT igm.item_id FROM item_group_metadata igm"
                + "    WHERE igm.crf_version_id = ? AND igm.show_group = 'false'"
                + "    AND igm.item_id IN (SELECT im.item_id FROM item_form_metadata im"
                + "     WHERE im.section_id = ? AND im.crf_version_id = ?)))"
                + " AND (ida.status_id != 5 AND ida.status_id != 7)) AND di.show_item = 'true' LIMIT 1";
        List<Integer> results = jdbc.queryForList(sql, Integer.class,
                eventCrfId, sectionId, crfVersionId, crfVersionId, sectionId, crfVersionId);
        return !results.isEmpty();
    }

    @Override
    @Transactional
    public void delete(int eventCrfId) {
        String sql = "DELETE FROM dyn_item_form_metadata WHERE event_crf_id = ?";
        jdbc.update(sql, eventCrfId);
    }

    @Override
    @Transactional
    public DynamicsItemFormMetadataBean saveOrUpdate(DynamicsItemFormMetadataBean entity) {
        if (entity.getId() != null && entity.getId() > 0) {
            String sql = "UPDATE dyn_item_form_metadata SET show_item = ?, event_crf_id = ?, "
                    + "item_id = ?, item_form_metadata_id = ?, crf_version_id = ?, "
                    + "item_data_id = ?, passed_dde = ? WHERE id = ?";
            jdbc.update(sql,
                    entity.isShowItem(),
                    entity.getEventCrfId(),
                    entity.getItemId(),
                    entity.getItemFormMetadataId(),
                    entity.getCrfVersionId(),
                    entity.getItemDataId(),
                    entity.getPassedDde(),
                    entity.getId());
            return entity;
        } else {
            String sql = "INSERT INTO dyn_item_form_metadata "
                    + "(id, show_item, event_crf_id, item_id, item_form_metadata_id, crf_version_id, item_data_id, passed_dde) "
                    + "VALUES (nextval('dyn_item_form_metadata_id_seq'), ?, ?, ?, ?, ?, ?, ?)";
            jdbc.update(sql,
                    entity.isShowItem(),
                    entity.getEventCrfId(),
                    entity.getItemId(),
                    entity.getItemFormMetadataId(),
                    entity.getCrfVersionId(),
                    entity.getItemDataId(),
                    entity.getPassedDde());
            return entity;
        }
    }
}
