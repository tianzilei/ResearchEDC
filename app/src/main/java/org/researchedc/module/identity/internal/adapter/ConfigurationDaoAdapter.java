package org.researchedc.module.identity.internal.adapter;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.dao.spi.ConfigurationDao;
import org.researchedc.domain.technicaladmin.ConfigurationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("configurationDao")
@Primary
@Transactional(readOnly = true)
public class ConfigurationDaoAdapter implements ConfigurationDao {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationDaoAdapter.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ConfigurationBean> rowMapper = (rs, rowNum) -> {
        ConfigurationBean bean = new ConfigurationBean();
        bean.setId(rs.getInt("id"));
        if (rs.wasNull()) {
            bean.setId(null);
        }
        bean.setKey(rs.getString("key"));
        bean.setValue(rs.getString("value"));
        bean.setDescription(rs.getString("description"));
        return bean;
    };

    public ConfigurationDaoAdapter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public ArrayList<ConfigurationBean> findAll() {
        String sql = "SELECT * FROM configuration";
        List<ConfigurationBean> results = jdbcTemplate.query(sql, rowMapper);
        return new ArrayList<>(results);
    }

    @Override
    public ConfigurationBean findByKey(String key) {
        String sql = "SELECT * FROM configuration WHERE key = ?";
        List<ConfigurationBean> results = jdbcTemplate.query(sql, rowMapper, key);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public ConfigurationBean saveOrUpdate(ConfigurationBean entity) {
        if (entity.getId() != null && entity.getId() > 0) {
            String sql = "UPDATE configuration SET key = ?, value = ?, description = ? WHERE id = ?";
            jdbcTemplate.update(sql,
                    entity.getKey(),
                    entity.getValue(),
                    entity.getDescription(),
                    entity.getId());
        } else {
            String sql = "INSERT INTO configuration (id, key, value, description) "
                    + "VALUES (nextval('configuration_id_seq'), ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, entity.getKey());
                ps.setString(2, entity.getValue());
                ps.setString(3, entity.getDescription());
                return ps;
            }, keyHolder);
            Number generatedKey = keyHolder.getKey();
            if (generatedKey != null) {
                entity.setId(generatedKey.intValue());
            }
        }
        return entity;
    }
}
