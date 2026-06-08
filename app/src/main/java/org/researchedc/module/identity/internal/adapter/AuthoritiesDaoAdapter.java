package org.researchedc.module.identity.internal.adapter;

import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import org.researchedc.dao.spi.AuthoritiesDao;
import org.researchedc.domain.user.AuthoritiesBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("authoritiesDao")
@Primary
@Transactional(readOnly = true)
public class AuthoritiesDaoAdapter implements AuthoritiesDao {

    private final JdbcTemplate jdbcTemplate;

    public AuthoritiesDaoAdapter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public AuthoritiesBean findByUsername(String username) {
        List<AuthoritiesBean> results = jdbcTemplate.query(
                "SELECT * FROM authorities WHERE username = ?",
                (rs, rowNum) -> {
                    AuthoritiesBean bean = new AuthoritiesBean();
                    bean.setId(rs.getInt("id"));
                    bean.setUsername(rs.getString("username"));
                    bean.setAuthority(rs.getString("authority"));
                    return bean;
                },
                username);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public AuthoritiesBean saveOrUpdate(AuthoritiesBean bean) {
        Integer existingId = bean.getId();
        if (existingId != null && existingId > 0) {
            jdbcTemplate.update(
                    "UPDATE authorities SET username = ?, authority = ? WHERE id = ?",
                    bean.getUsername(), bean.getAuthority(), existingId);
        } else {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(
                    connection -> {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO authorities (id, username, authority) VALUES (nextval('authorities_id_seq'), ?, ?)",
                                new String[]{"id"});
                        ps.setString(1, bean.getUsername());
                        ps.setString(2, bean.getAuthority());
                        return ps;
                    },
                    keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                bean.setId(key.intValue());
            }
        }
        return bean;
    }
}
