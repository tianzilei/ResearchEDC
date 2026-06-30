package org.researchedc.module.rule.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.researchedc.config.CurrentStudyAccessService;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.repository.RuleExpressionRepository;
import org.researchedc.module.rule.repository.RuleRepository;
import org.researchedc.module.rule.repository.RuleSetRepository;
import org.researchedc.module.rule.repository.RuleSetRuleRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RuleService {

    private final RuleSetRepository ruleSetRepository;
    private final RuleSetRuleRepository ruleSetRuleRepository;
    private final RuleRepository ruleRepository;
    private final RuleExpressionRepository ruleExpressionRepository;
    private final CurrentStudyAccessService currentStudyAccessService;

    public RuleService(RuleSetRepository ruleSetRepository,
                       RuleSetRuleRepository ruleSetRuleRepository,
                       RuleRepository ruleRepository,
                       RuleExpressionRepository ruleExpressionRepository,
                       CurrentStudyAccessService currentStudyAccessService) {
        this.ruleSetRepository = ruleSetRepository;
        this.ruleSetRuleRepository = ruleSetRuleRepository;
        this.ruleRepository = ruleRepository;
        this.ruleExpressionRepository = ruleExpressionRepository;
        this.currentStudyAccessService = currentStudyAccessService;
    }

    public List<RuleSetEntity> listAllRuleSets(Integer currentUserId) {
        if (currentStudyAccessService.canReadAllStudies(currentUserId)) {
            return ruleSetRepository.findAll();
        }
        Set<Integer> studyIds = currentStudyAccessService.readableStudyIds(currentUserId);
        if (studyIds.isEmpty()) {
            return List.of();
        }
        return ruleSetRepository.findByStudyIdInOrderByStudyIdAscRuleSetIdAsc(studyIds);
    }

    public List<RuleSetEntity> listRuleSetsByStudy(Integer studyId, Integer currentUserId) {
        requireReadAccess(currentUserId, studyId);
        return ruleSetRepository.findByStudyIdOrderByRuleSetId(studyId);
    }

    public RuleSetEntity getRuleSet(Integer ruleSetId, Integer currentUserId) {
        RuleSetEntity ruleSet = ruleSetRepository.findById(ruleSetId)
                .orElseThrow(() -> new NoSuchElementException("RuleSet not found: " + ruleSetId));
        requireReadAccess(currentUserId, ruleSet.getStudyId());
        return ruleSet;
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
    public void addRuleToRuleSet(Integer ruleSetId, Integer ruleId, Integer currentUserId) {
        RuleSetEntity ruleSet = ruleSetRepository.findById(ruleSetId)
                .orElseThrow(() -> new NoSuchElementException("RuleSet not found: " + ruleSetId));
        requireWriteAccess(currentUserId, ruleSet.getStudyId());
        ruleRepository.findById(ruleId)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + ruleId));
        RuleSetRuleEntity entity = new RuleSetRuleEntity();
        entity.setRuleSetId(ruleSetId);
        entity.setRuleId(ruleId);
        ruleSetRuleRepository.save(entity);
    }

    @Transactional
    public void removeRuleFromRuleSet(Integer ruleSetId, Integer ruleId, Integer currentUserId) {
        RuleSetEntity ruleSet = ruleSetRepository.findById(ruleSetId)
                .orElseThrow(() -> new NoSuchElementException("RuleSet not found: " + ruleSetId));
        requireWriteAccess(currentUserId, ruleSet.getStudyId());
        ruleSetRuleRepository.deleteByRuleSetIdAndRuleId(ruleSetId, ruleId);
    }

    private void requireReadAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canReadStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have read access to this study");
        }
    }

    private void requireWriteAccess(Integer currentUserId, Integer studyId) {
        if (!currentStudyAccessService.canWriteStudy(currentUserId, studyId)) {
            throw new AccessDeniedException("You do not have write access to this study");
        }
    }
}
