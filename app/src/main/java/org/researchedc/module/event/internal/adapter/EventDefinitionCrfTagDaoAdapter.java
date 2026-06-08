package org.researchedc.module.event.internal.adapter;

import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.dao.spi.EventDefinitionCrfTagDao;
import org.researchedc.domain.datamap.EventDefinitionCrfTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("eventDefinitionCrfTagDao")
@Primary
@Transactional(readOnly = true)
public class EventDefinitionCrfTagDaoAdapter implements EventDefinitionCrfTagDao {

    private static final Logger log = LoggerFactory.getLogger(EventDefinitionCrfTagDaoAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<EventDefinitionCrfTag> rowMapper = (rs, rowNum) -> {
        EventDefinitionCrfTag tag = new EventDefinitionCrfTag();
        tag.setId(rs.getInt("id"));
        if (rs.wasNull()) {
            tag.setId(null);
        }
        tag.setPath(rs.getString("path"));
        tag.setTagId(rs.getInt("tag_id"));
        tag.setActive(rs.getBoolean("active"));
        return tag;
    };

    public EventDefinitionCrfTagDaoAdapter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public EventDefinitionCrfTag findByCrfPath(int tagId, String path, boolean active) {
        String sql = "SELECT * FROM event_definition_crf_tag WHERE tag_id = ? AND path = ? AND active = ?";
        List<EventDefinitionCrfTag> results = jdbcTemplate.query(sql, rowMapper, tagId, path, active);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    @Transactional
    public EventDefinitionCrfTag saveOrUpdate(EventDefinitionCrfTag entity) {
        if (entity.getId() != null && entity.getId() > 0) {
            String sql = "UPDATE event_definition_crf_tag SET path = ?, tag_id = ?, active = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    entity.getPath(),
                    entity.getTagId(),
                    entity.isActive(),
                    entity.getId());
        } else {
            String sql = "INSERT INTO event_definition_crf_tag (id, path, tag_id, active) "
                    + "VALUES (nextval('event_definition_crf_tag_id_seq'), ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, entity.getPath());
                ps.setInt(2, entity.getTagId());
                ps.setBoolean(3, entity.isActive());
                return ps;
            }, keyHolder);
            Number generatedKey = keyHolder.getKey();
            if (generatedKey != null) {
                entity.setId(generatedKey.intValue());
            }
        }
        return entity;
    }

    @Override
    public EventDefinitionCrfTag findById(int id) {
        String sql = "SELECT * FROM event_definition_crf_tag WHERE id = ?";
        List<EventDefinitionCrfTag> results = jdbcTemplate.query(sql, rowMapper, id);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }
}
