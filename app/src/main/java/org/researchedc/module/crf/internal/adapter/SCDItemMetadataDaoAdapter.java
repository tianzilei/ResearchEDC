package org.researchedc.module.crf.internal.adapter;

import java.util.List;

import javax.sql.DataSource;

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

    private final RowMapper<ScdRule> rowMapper = (rs, rowNum) -> new ScdRule(
            rs.getInt("scd_item_form_metadata_id"),
            rs.getInt("control_item_form_metadata_id"),
            rs.getString("control_item_name"),
            rs.getString("option_value"),
            rs.getString("message"));

    public SCDItemMetadataDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public List<ScdRule> findRulesBySectionId(Integer sectionId) {
        String sql = "SELECT scd.* FROM scd_item_metadata scd WHERE scd.scd_item_form_metadata_id IN "
                + "(SELECT ifm.item_form_metadata_id FROM item_form_metadata ifm WHERE ifm.section_id = ?)";
        return jdbc.query(sql, rowMapper, sectionId);
    }
}
