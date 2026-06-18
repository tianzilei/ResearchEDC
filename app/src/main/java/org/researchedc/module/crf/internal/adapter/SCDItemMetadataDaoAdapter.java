package org.researchedc.module.crf.internal.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.domain.crfdata.SCDItemMetadataBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("scdItemMetadataDao")
@Primary
@Transactional(readOnly = true)
public class SCDItemMetadataDaoAdapter {

    public record ScdRule(Integer scdItemId,
                          Integer controlItemFormMetadataId,
                          String controlItemName,
                          String optionValue,
                          String message) {
    }

    private final JdbcTemplate jdbc;

    private final RowMapper<SCDItemMetadataBean> rowMapper = (rs, rowNum) -> {
        SCDItemMetadataBean bean = new SCDItemMetadataBean();
        bean.setId(rs.getInt("scd_item_metadata_id"));
        bean.setScdItemFormMetadataId(rs.getInt("scd_item_form_metadata_id"));
        bean.setControlItemFormMetadataId(rs.getInt("control_item_form_metadata_id"));
        bean.setControlItemName(rs.getString("control_item_name"));
        bean.setOptionValue(rs.getString("option_value"));
        bean.setMessage(rs.getString("message"));
        bean.setScdItemId(rs.getInt("scd_item_id"));
        return bean;
    };

    public SCDItemMetadataDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public ArrayList<SCDItemMetadataBean> findAllBySectionId(Integer sectionId) {
        String sql = "SELECT scd.* FROM scd_item_metadata scd WHERE scd.scd_item_form_metadata_id IN "
                + "(SELECT ifm.item_form_metadata_id FROM item_form_metadata ifm WHERE ifm.section_id = ?)";
        return new ArrayList<>(jdbc.query(sql, rowMapper, sectionId));
    }

    public List<ScdRule> findRulesBySectionId(Integer sectionId) {
        return findAllBySectionId(sectionId).stream()
                .map(bean -> new ScdRule(
                        bean.getScdItemFormMetadataId(),
                        bean.getControlItemFormMetadataId(),
                        bean.getControlItemName(),
                        bean.getOptionValue(),
                        bean.getMessage()))
                .toList();
    }

    public List<Integer> findAllSCDItemFormMetadataIdsBySectionId(Integer sectionId) {
        String sql = "SELECT scd.scd_item_form_metadata_id FROM scd_item_metadata scd WHERE scd.scd_item_form_metadata_id IN "
                + "(SELECT ifm.item_form_metadata_id FROM item_form_metadata ifm WHERE ifm.section_id = ?)";
        return jdbc.queryForList(sql, Integer.class, sectionId);
    }

    public ArrayList<SCDItemMetadataBean> findAllSCDByItemFormMetadataId(Integer itemFormMetadataId) {
        String sql = "SELECT scd.* FROM scd_item_metadata scd WHERE scd.scd_item_form_metadata_id = ?";
        return new ArrayList<>(jdbc.query(sql, rowMapper, itemFormMetadataId));
    }
}
