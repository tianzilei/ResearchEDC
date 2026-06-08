package org.researchedc.module.rule.internal.adapter;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.researchedc.dao.spi.RuleSetAuditDomainDao;
import org.researchedc.domain.rule.RuleSetAuditBean;
import org.researchedc.domain.rule.RuleSetBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleSetAuditDao")
@Primary
@Transactional(readOnly = true)
public class RuleSetAuditDaoAdapter implements RuleSetAuditDomainDao {

    private final JdbcTemplate jdbc;

    private final RowMapper<RuleSetAuditBean> rowMapper = (rs, rowNum) -> {
        RuleSetAuditBean bean = new RuleSetAuditBean();
        bean.setId(rs.getInt("id"));
        bean.setUpdaterId(rs.getInt("updater_id"));
        bean.setDateUpdated(rs.getTimestamp("date_updated"));
        return bean;
    };

    public RuleSetAuditDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public RuleSetAuditBean saveOrUpdate(RuleSetAuditBean entity) {
        int ruleSetId = entity.getRuleSetBean() != null ? entity.getRuleSetBean().getId() : 0;
        int statusId = 1; // AVAILABLE
        int updaterId = entity.getUpdaterId() != null ? entity.getUpdaterId() : 0;
        if (entity.getId() != null && entity.getId() > 0) {
            String sql = "UPDATE rule_set_audit SET rule_set_id = ?, status_id = ?, updater_id = ?, date_updated = now() WHERE id = ?";
            jdbc.update(sql, ruleSetId, statusId, updaterId, entity.getId());
        } else {
            String sql = "INSERT INTO rule_set_audit (id, rule_set_id, status_id, updater_id, date_updated) "
                    + "VALUES (nextval('rule_set_audit_id_seq'), ?, ?, ?, now())";
            jdbc.update(sql, ruleSetId, statusId, updaterId);
        }
        return entity;
    }

    @Override
    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        String sql = "SELECT id, rule_set_id, updater_id, date_updated FROM rule_set_audit WHERE rule_set_id = ? ORDER BY date_updated DESC";
        return new ArrayList<>(jdbc.query(sql, rowMapper, ruleSet.getId()));
    }
}
