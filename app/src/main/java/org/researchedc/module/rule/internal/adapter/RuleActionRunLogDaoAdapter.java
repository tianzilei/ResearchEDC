package org.researchedc.module.rule.internal.adapter;

import java.sql.PreparedStatement;
import javax.sql.DataSource;

import org.researchedc.dao.spi.RuleActionRunLogDomainDao;
import org.researchedc.domain.rule.action.ActionType;
import org.researchedc.domain.rule.action.RuleActionRunLogBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleActionRunLogDao")
@Primary
@Transactional(readOnly = true)
public class RuleActionRunLogDaoAdapter implements RuleActionRunLogDomainDao {

    private static final Logger log = LoggerFactory.getLogger(RuleActionRunLogDaoAdapter.class);
    private final JdbcTemplate jdbc;

    public RuleActionRunLogDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public RuleActionRunLogBean saveOrUpdate(RuleActionRunLogBean entity) {
        if (entity.getId() != null && entity.getId() > 0) {
            String sql = "UPDATE rule_action_run_log SET item_data_id = ?, value = ?, rule_oc_oid = ?, "
                    + "action_type = ? WHERE id = ?";
            jdbc.update(sql,
                    entity.getItemDataId(),
                    entity.getValue(),
                    entity.getRuleOid(),
                    entity.getActionType() != null ? entity.getActionType().getCode() : null,
                    entity.getId());
        } else {
            String sql = "INSERT INTO rule_action_run_log (id, item_data_id, value, rule_oc_oid, action_type) "
                    + "VALUES (nextval('rule_action_run_log_id_seq'), ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setInt(1, entity.getItemDataId() != null ? entity.getItemDataId() : 0);
                ps.setString(2, entity.getValue());
                ps.setString(3, entity.getRuleOid());
                ps.setInt(4, entity.getActionType() != null ? entity.getActionType().getCode() : null);
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
    public Integer findCountByRuleActionRunLogBean(RuleActionRunLogBean ruleActionRunLog) {
        String sql = "SELECT COUNT(*) FROM rule_action_run_log WHERE id = ?";
        Integer result = jdbc.queryForObject(sql, Integer.class, ruleActionRunLog.getId());
        return result != null ? result : 0;
    }

    @Override
    @Transactional
    public void delete(int itemDataId) {
        String sql = "DELETE FROM rule_action_run_log WHERE item_data_id = ?";
        jdbc.update(sql, itemDataId);
    }
}
