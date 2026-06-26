package org.researchedc.module.rule.repository;

import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleSetRuleRepository extends JpaRepository<RuleSetRuleEntity, Integer> {

    List<RuleSetRuleEntity> findByRuleSetId(Integer ruleSetId);

    void deleteByRuleSetIdAndRuleId(Integer ruleSetId, Integer ruleId);
}
