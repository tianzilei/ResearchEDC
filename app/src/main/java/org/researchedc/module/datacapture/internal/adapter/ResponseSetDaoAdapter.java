package org.researchedc.module.datacapture.internal.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.domain.datamap.ResponseSet;
import org.researchedc.domain.datamap.ResponseType;
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

    public ResponseSet findByLabelVersion(String label, Integer version) {
        String sql = "SELECT * FROM response_set WHERE label = ? AND version_id = ?";
        List<ResponseSet> results = jdbcTemplate.query(sql, RESPONSE_SET_ROW_MAPPER, label, version);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<ImportResponseSet> findAllByItemId(int itemId) {
        String sql = "SELECT rs.* FROM item_form_metadata ifm"
                + " JOIN response_set rs ON ifm.response_set_id = rs.response_set_id"
                + " WHERE ifm.item_id = ?";
        return jdbcTemplate.query(sql, IMPORT_RESPONSE_SET_ROW_MAPPER, itemId);
    }

    @Transactional
    public ResponseSet saveOrUpdate(ResponseSet responseSet) {
        Integer responseTypeId = responseSet.getResponseType() != null
                ? responseSet.getResponseType().getResponseTypeId()
                : null;

        if (responseSet.getResponseSetId() > 0) {
            String sql = "UPDATE response_set SET label = ?, options_text = ?, options_values = ?,"
                    + " version_id = ?, response_type_id = ?"
                    + " WHERE response_set_id = ?";
            jdbcTemplate.update(sql,
                    responseSet.getLabel(),
                    responseSet.getOptionsText(),
                    responseSet.getOptionsValues(),
                    responseSet.getVersionId(),
                    responseTypeId,
                    responseSet.getResponseSetId());
        } else {
            Number newId = jdbcTemplate.queryForObject(
                    "SELECT nextval('response_set_response_set_id_seq')", Number.class);
            int id = newId != null ? newId.intValue() : 0;

            String sql = "INSERT INTO response_set (response_set_id, label, options_text,"
                    + " options_values, version_id, response_type_id)"
                    + " VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, id,
                    responseSet.getLabel(),
                    responseSet.getOptionsText(),
                    responseSet.getOptionsValues(),
                    responseSet.getVersionId(),
                    responseTypeId);

            responseSet.setResponseSetId(id);
        }
        return responseSet;
    }

    private static final RowMapper<ImportResponseSet> IMPORT_RESPONSE_SET_ROW_MAPPER = (ResultSet rs, int rowNum) ->
            new ImportResponseSet(
                    rs.getObject("response_type_id", Integer.class),
                    rs.getString("options_text"),
                    rs.getString("options_values"));

    private static final RowMapper<ResponseSet> RESPONSE_SET_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        ResponseSet responseSet = new ResponseSet();
        responseSet.setResponseSetId(rs.getInt("response_set_id"));
        responseSet.setLabel(rs.getString("label"));
        responseSet.setOptionsText(rs.getString("options_text"));
        responseSet.setOptionsValues(rs.getString("options_values"));

        int versionId = rs.getInt("version_id");
        if (!rs.wasNull()) {
            responseSet.setVersionId(versionId);
        }

        int responseTypeId = rs.getInt("response_type_id");
        if (!rs.wasNull()) {
            ResponseType stub = new ResponseType();
            stub.setResponseTypeId(responseTypeId);
            responseSet.setResponseType(stub);
        }

        return responseSet;
    };
}
