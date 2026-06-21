package org.researchedc.module.datacapture.internal.adapter;

import java.sql.ResultSet;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.module.dataimport.service.ImportResponseSetPort;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("responseSetDao")
@Primary
@Transactional(readOnly = true)
public class ResponseSetDaoAdapter implements ImportResponseSetPort {

    private final JdbcTemplate jdbcTemplate;

    public ResponseSetDaoAdapter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<ImportResponseSet> findAllByItemId(int itemId) {
        String sql = "SELECT rs.* FROM item_form_metadata ifm"
                + " JOIN response_set rs ON ifm.response_set_id = rs.response_set_id"
                + " WHERE ifm.item_id = ?";
        return jdbcTemplate.query(sql, IMPORT_RESPONSE_SET_ROW_MAPPER, itemId);
    }

    private static final RowMapper<ImportResponseSet> IMPORT_RESPONSE_SET_ROW_MAPPER = (ResultSet rs, int rowNum) ->
            new ImportResponseSet(
                    rs.getObject("response_type_id", Integer.class),
                    rs.getString("options_text"),
                    rs.getString("options_values"));
}
