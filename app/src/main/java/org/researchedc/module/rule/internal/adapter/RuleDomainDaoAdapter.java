package org.researchedc.module.rule.internal.adapter;

import org.researchedc.dao.spi.RuleDomainDao;
import org.researchedc.domain.rule.RuleBean;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.repository.RuleRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Primary
@Transactional(readOnly = true)
public class RuleDomainDaoAdapter implements RuleDomainDao {

    private final RuleRepository ruleRepository;

    public RuleDomainDaoAdapter(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public RuleBean findByOid(RuleBean ruleBean) {
        if (ruleBean == null) {
            return null;
        }
        return findByOid(ruleBean.getOid(), ruleBean.getStudyId());
    }

    @Override
    public RuleBean findByOid(String oid, Integer studyId) {
        return ruleRepository.findByOcOid(oid)
                .map(this::toBean)
                .orElse(null);
    }

    @Override
    @Transactional
    public RuleBean saveOrUpdate(RuleBean ruleBean) {
        RuleEntity entity;
        if (ruleBean.getId() > 0) {
            entity = ruleRepository.findById(ruleBean.getId()).orElseGet(RuleEntity::new);
        } else {
            entity = new RuleEntity();
        }
        apply(ruleBean, entity);
        return toBean(ruleRepository.save(entity));
    }

    private void apply(RuleBean bean, RuleEntity entity) {
        entity.setRuleId(bean.getId() > 0 ? bean.getId() : null);
        entity.setOcOid(bean.getOid());
        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setEnabled(bean.isEnabled());
    }

    private RuleBean toBean(RuleEntity entity) {
        RuleBean bean = new RuleBean();
        bean.setId(entity.getRuleId());
        bean.setOid(entity.getOcOid());
        bean.setName(entity.getName());
        bean.setDescription(entity.getDescription());
        bean.setEnabled(entity.getEnabled());
        return bean;
    }
}
