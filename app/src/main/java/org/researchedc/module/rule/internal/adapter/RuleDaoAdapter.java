package org.researchedc.module.rule.internal.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.researchedc.bean.core.EntityBean;
import org.researchedc.bean.core.Status;
import org.researchedc.bean.rule.RuleBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.rule.expression.Context;
import org.researchedc.bean.rule.expression.ExpressionBean;
import org.researchedc.dao.spi.IRuleDAO;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDAO")
@Primary
@Transactional(readOnly = true)
public class RuleDaoAdapter implements IRuleDAO {

    private final RuleRepository ruleRepository;
    private final RuleExpressionRepository expressionRepository;

    public RuleDaoAdapter(RuleRepository ruleRepository, RuleExpressionRepository expressionRepository) {
        this.ruleRepository = ruleRepository;
        this.expressionRepository = expressionRepository;
    }

    @Override
    public EntityBean findByPK(int ID) {
        return ruleRepository.findById(ID)
                .map(this::toBean)
                .orElseGet(RuleBean::new);
    }

    @Override
    public Collection findAll() {
        return toBeans(ruleRepository.findAll());
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
                                          boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        return new ArrayList();
    }

    @Override
    public RuleBean findByOid(RuleBean ruleBean) {
        if (ruleBean == null) {
            return null;
        }
        return findByOid(ruleBean.getOid());
    }

    @Override
    public RuleBean findByOid(String oid) {
        return ruleRepository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    public ArrayList<RuleBean> findByRuleSet(RuleSetBean ruleSet) {
        return toBeans(ruleRepository.findByRuleSetId(ruleSet.getId()));
    }

    @Override
    @Transactional
    public EntityBean create(EntityBean eb) {
        RuleBean bean = (RuleBean) eb;
        RuleExpressionEntity expression = saveExpression(bean.getExpression(), true);
        RuleEntity entity = new RuleEntity();
        apply(bean, entity);
        entity.setRuleExpressionId(expression.getRuleExpressionId());
        entity.setStatusId(Status.AVAILABLE.getId());
        entity.setDateCreated(LocalDateTime.now());
        return toBean(ruleRepository.save(entity));
    }

    @Override
    @Transactional
    public EntityBean update(EntityBean eb) {
        RuleBean bean = (RuleBean) eb;
        RuleEntity entity = ruleRepository.findById(bean.getId()).orElseGet(RuleEntity::new);
        entity.setRuleId(bean.getId() > 0 ? bean.getId() : null);
        RuleExpressionEntity expression = saveExpression(bean.getExpression(), false);
        apply(bean, entity);
        entity.setRuleExpressionId(expression.getRuleExpressionId());
        entity.setDateUpdated(LocalDateTime.now());
        entity.setUpdateId(bean.getUpdaterId());
        RuleBean saved = toBean(ruleRepository.save(entity));
        saved.setActive(true);
        return saved;
    }

    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        RuleEntity entity = new RuleEntity();
        entity.setRuleId((Integer) hm.get("rule_id"));
        entity.setName((String) hm.get("name"));
        entity.setDescription((String) hm.get("description"));
        entity.setOcOid((String) hm.get("oc_oid"));
        entity.setEnabled((Boolean) hm.get("enabled"));
        entity.setRuleExpressionId((Integer) hm.get("rule_expression_id"));
        entity.setOwnerId((Integer) hm.get("owner_id"));
        entity.setDateCreated(toLocalDateTime((Date) hm.get("date_created")));
        entity.setDateUpdated(toLocalDateTime((Date) hm.get("date_updated")));
        entity.setUpdateId((Integer) hm.get("update_id"));
        entity.setStatusId((Integer) hm.get("status_id"));
        return toBean(entity);
    }

    private void apply(RuleBean bean, RuleEntity entity) {
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setOcOid(bean.getOid());
        entity.setEnabled(bean.isEnabled());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
    }

    private RuleExpressionEntity saveExpression(ExpressionBean bean, boolean create) {
        if (bean == null) {
            return new RuleExpressionEntity();
        }
        RuleExpressionEntity entity = bean.getId() > 0
                ? expressionRepository.findById(bean.getId()).orElseGet(RuleExpressionEntity::new)
                : new RuleExpressionEntity();
        entity.setRuleExpressionId(bean.getId() > 0 ? bean.getId() : null);
        entity.setContext(bean.getContext() != null ? bean.getContext().getCode() : null);
        entity.setValue(bean.getValue());
        entity.setOwnerId(bean.getOwnerId());
        entity.setUpdateId(bean.getUpdaterId());
        entity.setStatusId(Status.AVAILABLE.getId());
        if (create || entity.getDateCreated() == null) {
            entity.setDateCreated(LocalDateTime.now());
        } else {
            entity.setDateUpdated(LocalDateTime.now());
        }
        return expressionRepository.save(entity);
    }

    private ArrayList<RuleBean> toBeans(List<RuleEntity> entities) {
        ArrayList<RuleBean> beans = new ArrayList<>();
        entities.stream()
                .map(this::toBean)
                .forEach(beans::add);
        return beans;
    }

    private RuleBean toBean(RuleEntity entity) {
        RuleBean bean = new RuleBean();
        if (entity.getRuleId() != null) {
            bean.setId(entity.getRuleId());
        }
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setOid(entity.getOcOid());
        bean.setEnabled(Boolean.TRUE.equals(entity.getEnabled()));
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        bean.setExpression(toExpressionBean(entity.getRuleExpressionId()));
        return bean;
    }

    private ExpressionBean toExpressionBean(Integer expressionId) {
        if (expressionId == null || expressionId <= 0) {
            return new ExpressionBean();
        }
        return expressionRepository.findById(expressionId)
                .map(this::toExpressionBean)
                .orElseGet(ExpressionBean::new);
    }

    private ExpressionBean toExpressionBean(RuleExpressionEntity entity) {
        ExpressionBean bean = new ExpressionBean();
        if (entity.getRuleExpressionId() != null) {
            bean.setId(entity.getRuleExpressionId());
        }
        bean.setContext(entity.getContext() != null ? Context.getByCode(entity.getContext()) : null);
        bean.setValue(entity.getValue());
        bean.setOwnerId(valueOrZero(entity.getOwnerId()));
        bean.setUpdaterId(valueOrZero(entity.getUpdateId()));
        bean.setCreatedDate(toDate(entity.getDateCreated()));
        bean.setUpdatedDate(toDate(entity.getDateUpdated()));
        bean.setStatus(Status.getFromMap(valueOrZero(entity.getStatusId())));
        return bean;
    }

    private int valueOrZero(Integer value) {
        return value != null ? value : 0;
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return new Date(0);
        }
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
    }
}
