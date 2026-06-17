package org.researchedc.module.rule.internal.adapter;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.spi.IRuleSetRuleDAO;
import org.researchedc.domain.rule.RuleBean;
import org.researchedc.domain.rule.RuleSetBean;
import org.researchedc.domain.rule.RuleSetRuleBean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleSetRuleDao")
@Primary
@Transactional(readOnly = true)
public class RuleSetRuleDaoAdapter implements IRuleSetRuleDAO {
    private final JdbcTemplate jdbc;

    private final RowMapper<RuleSetRuleBean> rowMapper = (rs, rowNum) -> {
        RuleSetRuleBean bean = new RuleSetRuleBean();
        bean.setId(rs.getInt("id"));
        return bean;
    };

    public RuleSetRuleDaoAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public ArrayList<RuleSetRuleBean> findByRuleSetBeanAndRuleBean(RuleSetBean ruleSetBean, RuleBean ruleBean) {
        String sql = "SELECT id, status_id FROM rule_set_rule WHERE rule_set_id = ? AND rule_id = ?";
        return new ArrayList<>(jdbc.query(sql, rowMapper, ruleSetBean.getId(), ruleBean.getId()));
    }

    @Override
    @Transactional
    public ArrayList<RuleSetRuleBean> findByRuleSetStudyIdAndStatusAvail(Integer studyId) {
        String sql = "SELECT rsr.id, rsr.status_id FROM rule_set_rule rsr "
                + "JOIN rule_set rs ON rs.id = rsr.rule_set_id "
                + "WHERE rs.study_id = ? AND rsr.status_id = 1";
        return new ArrayList<>(jdbc.query(sql, rowMapper, studyId));
    }

    @Override
    public int getCountByStudy(StudyBean study) {
        String sql = "SELECT COUNT(*) FROM rule_set_rule rsr "
                + "JOIN rule_set rs ON rs.id = rsr.rule_set_id "
                + "WHERE rs.study_id = ? AND rsr.status_id = 1";
        Integer result = jdbc.queryForObject(sql, Integer.class, study.getId());
        return result != null ? result : 0;
    }

    @Override
    public RuleSetRuleBean findById(Integer id) {
        String sql = "SELECT id, status_id FROM rule_set_rule WHERE id = ?";
        var results = jdbc.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    @Transactional
    public RuleSetRuleBean saveOrUpdate(RuleSetRuleBean entity) {
        if (entity.getId() != null && entity.getId() > 0) {
            String sql = "UPDATE rule_set_rule SET rule_set_id = ?, rule_id = ?, status_id = ? WHERE id = ?";
            jdbc.update(sql,
                    entity.getRuleSetBean() != null ? entity.getRuleSetBean().getId() : null,
                    entity.getRuleBean() != null ? entity.getRuleBean().getId() : null,
                    1,
                    entity.getId());
        } else {
            String sql = "INSERT INTO rule_set_rule (id, rule_set_id, rule_id, status_id) "
                    + "VALUES (nextval('rule_set_rule_id_seq'), ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setInt(1, entity.getRuleSetBean() != null ? entity.getRuleSetBean().getId() : 0);
                ps.setInt(2, entity.getRuleBean() != null ? entity.getRuleBean().getId() : 0);
                ps.setInt(3, 1);
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
