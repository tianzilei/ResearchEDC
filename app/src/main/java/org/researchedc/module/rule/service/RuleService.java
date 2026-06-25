package org.researchedc.module.rule.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RuleService {

    private final RuleSetRepository ruleSetRepository;
    private final RuleSetRuleRepository ruleSetRuleRepository;
    private final RuleRepository ruleRepository;
    private final RuleExpressionRepository ruleExpressionRepository;

    public RuleService(RuleSetRepository ruleSetRepository,
                       RuleSetRuleRepository ruleSetRuleRepository,
                       RuleRepository ruleRepository,
                       RuleExpressionRepository ruleExpressionRepository) {
        this.ruleSetRepository = ruleSetRepository;
        this.ruleSetRuleRepository = ruleSetRuleRepository;
        this.ruleRepository = ruleRepository;
        this.ruleExpressionRepository = ruleExpressionRepository;
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

    public RuleEntity getRule(int id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + id));
    }

    public RuleExpressionEntity getRuleExpression(Integer id) {
        return ruleExpressionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("RuleExpression not found: " + id));
    }

    @Transactional
    public RuleEntity createRule(String name, String description, Boolean enabled,
                                  String expressionValue, Integer expressionContext, Integer ownerId) {
        RuleExpressionEntity expr = new RuleExpressionEntity();
        expr.setValue(expressionValue);
        expr.setContext(expressionContext);
        expr = ruleExpressionRepository.save(expr);

        RuleEntity rule = new RuleEntity();
        rule.setName(name);
        rule.setDescription(description);
        rule.setEnabled(enabled);
        rule.setRuleExpressionId(expr.getRuleExpressionId());
        return ruleRepository.save(rule);
    }

    @Transactional
    public RuleEntity updateRule(int id, String name, String description, Boolean enabled,
                                  String expressionValue, Integer expressionContext, Integer updaterId) {
        RuleEntity rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + id));
        rule.setName(name);
        rule.setDescription(description);
        rule.setEnabled(enabled);

        RuleExpressionEntity expr = ruleExpressionRepository.findById(rule.getRuleExpressionId())
                .orElseThrow(() -> new NoSuchElementException("RuleExpression not found: " + rule.getRuleExpressionId()));
        expr.setValue(expressionValue);
        expr.setContext(expressionContext);
        ruleExpressionRepository.save(expr);

        return ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(int id) {
        RuleEntity rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + id));
        ruleRepository.delete(rule);
    }

    @Transactional
    public void addRuleToRuleSet(Integer ruleSetId, Integer ruleId) {
        ruleSetRepository.findById(ruleSetId)
                .orElseThrow(() -> new NoSuchElementException("RuleSet not found: " + ruleSetId));
        ruleRepository.findById(ruleId)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + ruleId));
        RuleSetRuleEntity entity = new RuleSetRuleEntity();
        entity.setRuleSetId(ruleSetId);
        entity.setRuleId(ruleId);
        ruleSetRuleRepository.save(entity);
    }

    @Transactional
    public void removeRuleFromRuleSet(Integer ruleSetId, Integer ruleId) {
        ruleSetRuleRepository.deleteByRuleSetIdAndRuleId(ruleSetId, ruleId);
    }
}
