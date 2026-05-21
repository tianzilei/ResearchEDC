package org.researchedc.module.rule.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RuleService {

    private final RuleSetRepository ruleSetRepository;
    private final RuleSetRuleRepository ruleSetRuleRepository;

    public RuleService(RuleSetRepository ruleSetRepository,
                       RuleSetRuleRepository ruleSetRuleRepository) {
        this.ruleSetRepository = ruleSetRepository;
        this.ruleSetRuleRepository = ruleSetRuleRepository;
    }

    public List<RuleSetEntity> listAllRuleSets() {
        return ruleSetRepository.findAll();
    }

    public List<RuleSetEntity> listRuleSetsByStudy(Integer studyId) {
        return ruleSetRepository.findByStudyIdOrderByRuleSetId(studyId);
    }

    public RuleSetEntity getRuleSet(Integer ruleSetId) {
        return ruleSetRepository.findById(ruleSetId)
                .orElseThrow(() -> new NoSuchElementException("RuleSet not found: " + ruleSetId));
    }

    public List<RuleSetRuleEntity> listRuleSetRules(Integer ruleSetId) {
        return ruleSetRuleRepository.findByRuleSetId(ruleSetId);
    }
}
