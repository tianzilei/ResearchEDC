package org.researchedc.module.rule.controller;

import java.util.List;

import org.researchedc.config.CoreEdcAuthorityExpressions;
import org.researchedc.module.rule.dto.AddRuleToRuleSetRequest;
import org.researchedc.module.rule.dto.CreateRuleRequest;
import org.researchedc.module.rule.dto.RuleDetailDTO;
import org.researchedc.module.rule.dto.RuleSetDTO;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.service.RuleService;
import org.researchedc.config.CurrentUserUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService ruleService;
    private final CurrentUserUtils currentUserUtils;

    public RuleController(RuleService ruleService, CurrentUserUtils currentUserUtils) {
        this.ruleService = ruleService;
        this.currentUserUtils = currentUserUtils;
    }

    @GetMapping("/rule-sets")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<List<RuleSetDTO>> listRuleSets(@RequestParam(required = false) Integer studyId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        List<RuleSetEntity> entities = studyId != null
                ? ruleService.listRuleSetsByStudy(studyId, currentUserId)
                : ruleService.listAllRuleSets(currentUserId);
        return ResponseEntity.ok(entities.stream().map(this::toRuleSetDto).toList());
    }

    @GetMapping("/rule-sets/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<RuleSetDTO> getRuleSet(@PathVariable int id) {
        try {
            Integer currentUserId = currentUserUtils.getCurrentUserId();
            return ResponseEntity.ok(toRuleSetDto(ruleService.getRuleSet(id, currentUserId)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/rule-sets/{id}/rules")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> addRuleToRuleSet(
            @PathVariable int id, @RequestBody AddRuleToRuleSetRequest request) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        ruleService.addRuleToRuleSet(id, request.getRuleId(), currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rule-sets/{id}/rules/{ruleId}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> removeRuleFromRuleSet(
            @PathVariable int id, @PathVariable int ruleId) {
        Integer currentUserId = currentUserUtils.getCurrentUserId();
        ruleService.removeRuleFromRuleSet(id, ruleId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.READ_EDC_DATA)
    public ResponseEntity<RuleDetailDTO> getRule(@PathVariable int id) {
        try {
            return ResponseEntity.ok(toRuleDetailDto(ruleService.getRule(id)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<RuleDetailDTO> createRule(@RequestBody CreateRuleRequest request) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        RuleEntity entity = ruleService.createRule(
                request.getName(), request.getDescription(), request.getEnabled(),
                request.getExpressionValue(), request.getExpressionContext(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toRuleDetailDto(entity));
    }

    @PostMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<RuleDetailDTO> updateRule(
            @PathVariable int id, @RequestBody CreateRuleRequest request) {
        try {
            Integer updaterId = currentUserUtils.getCurrentUserId();
            RuleEntity entity = ruleService.updateRule(
                    id, request.getName(), request.getDescription(), request.getEnabled(),
                    request.getExpressionValue(), request.getExpressionContext(), updaterId);
            return ResponseEntity.ok(toRuleDetailDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CoreEdcAuthorityExpressions.WRITE_EDC_DATA)
    public ResponseEntity<Void> deleteRule(@PathVariable int id) {
        try {
            ruleService.deleteRule(id);
            return ResponseEntity.noContent().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private RuleSetDTO toRuleSetDto(RuleSetEntity entity) {
        RuleSetDTO dto = new RuleSetDTO();
        dto.setRuleSetId(entity.getRuleSetId());
        dto.setStudyId(entity.getStudyId());
        dto.setOwnerId(entity.getOwnerId());
        dto.setDateCreated(entity.getDateCreated());
        dto.setRuleIds(ruleService.listRuleSetRules(entity.getRuleSetId())
                .stream().map(RuleSetRuleEntity::getRuleId).toList());
        return dto;
    }

    private RuleDetailDTO toRuleDetailDto(RuleEntity rule) {
        RuleDetailDTO dto = new RuleDetailDTO();
        dto.setRuleId(rule.getRuleId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setEnabled(rule.getEnabled());
        if (rule.getRuleExpressionId() != null) {
            try {
                RuleExpressionEntity expr = ruleService.getRuleExpression(rule.getRuleExpressionId());
                dto.setExpressionValue(expr.getValue());
                dto.setExpressionContext(expr.getContext());
            } catch (java.util.NoSuchElementException e) {
                dto.setExpressionValue(null);
            }
        }
        return dto;
    }
}
