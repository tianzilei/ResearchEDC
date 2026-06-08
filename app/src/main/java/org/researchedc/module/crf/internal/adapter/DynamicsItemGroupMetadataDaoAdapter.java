package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.bean.submit.EventCRFBean;
import org.researchedc.bean.submit.ItemGroupMetadataBean;
import org.researchedc.dao.spi.DynamicsItemGroupMetadataDao;
import org.researchedc.domain.crfdata.DynamicsItemGroupMetadataBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("dynamicsItemGroupMetadataDao")
@Primary
@Transactional(readOnly = true)
public class DynamicsItemGroupMetadataDaoAdapter implements DynamicsItemGroupMetadataDao {

    private final JdbcTemplate jdbc;

    private final RowMapper<DynamicsItemGroupMetadataBean> rowMapper = (rs, rowNum) -> {
        DynamicsItemGroupMetadataBean bean = new DynamicsItemGroupMetadataBean();
        bean.setId(rs.getInt("id"));
        bean.setShowGroup(rs.getBoolean("show_group"));
        bean.setEventCrfId(rs.getInt("event_crf_id"));
        bean.setItemGroupMetadataId(rs.getInt("item_group_metadata_id"));
        bean.setItemGroupId(rs.getInt("item_group_id"));
        bean.setPassedDde(rs.getInt("passed_dde"));
        return bean;
    };

    public DynamicsItemGroupMetadataDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean,
            EventCRFBean eventCrfBean) {
        String sql = "SELECT * FROM dyn_item_group_metadata WHERE item_group_metadata_id = ? AND item_group_id = ? AND event_crf_id = ?";
        List<DynamicsItemGroupMetadataBean> results = jdbc.query(sql, rowMapper,
                metadataBean.getId(), metadataBean.getItemGroupId(), eventCrfBean.getId());
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, int eventCrfBeanId) {
        String sql = "SELECT * FROM dyn_item_group_metadata WHERE item_group_metadata_id = ? AND item_group_id = ? AND event_crf_id = ?";
        List<DynamicsItemGroupMetadataBean> results = jdbc.query(sql, rowMapper,
                metadataBean.getId(), metadataBean.getItemGroupId(), eventCrfBeanId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String sql = "SELECT dg.item_group_id FROM dyn_item_group_metadata dg WHERE dg.event_crf_id = ? "
                + "AND dg.item_group_metadata_id IN ("
                + " SELECT DISTINCT igm.item_group_metadata_id FROM item_group_metadata igm WHERE igm.crf_version_id = ?"
                + " AND igm.show_group = 'false'"
                + " AND igm.item_id IN (SELECT im.item_id FROM item_form_metadata im WHERE im.section_id = ? AND im.crf_version_id = ?))"
                + " AND dg.show_group = 'true' LIMIT 1";
        List<Integer> results = jdbc.queryForList(sql, Integer.class, eventCrfId, crfVersionId, sectionId, crfVersionId);
        return !results.isEmpty();
    }

    @Override
    @Transactional
    public void delete(int eventCrfId) {
        String sql = "DELETE FROM dyn_item_group_metadata WHERE event_crf_id = ?";
        jdbc.update(sql, eventCrfId);
    }
}
