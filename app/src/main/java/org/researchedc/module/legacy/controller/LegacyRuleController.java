package org.researchedc.module.legacy.controller;

import org.researchedc.module.legacy.dto.CreateRuleRequest;
import org.researchedc.module.legacy.dto.RuleDetailDTO;
import org.researchedc.module.rule.entity.RuleEntity;
import org.researchedc.module.rule.entity.RuleExpressionEntity;
import org.researchedc.config.CurrentUserUtils;
import org.researchedc.module.rule.service.RuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/rules")
public class LegacyRuleController {

    private final RuleService ruleService;
    private final CurrentUserUtils currentUserUtils;

    public LegacyRuleController(RuleService ruleService, CurrentUserUtils currentUserUtils) {
        this.ruleService = ruleService;
        this.currentUserUtils = currentUserUtils;
    }

    private RuleDetailDTO toDetailDto(RuleEntity rule) {
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

    @GetMapping("/{id}")
    public ResponseEntity<RuleDetailDTO> getRule(@PathVariable int id) {
        try {
            return ResponseEntity.ok(toDetailDto(ruleService.getRule(id)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<RuleDetailDTO> createRule(@RequestBody CreateRuleRequest req) {
        Integer ownerId = currentUserUtils.getCurrentUserId();
        RuleEntity entity = ruleService.createRule(
                req.getName(), req.getDescription(), req.getEnabled(),
                req.getExpressionValue(), req.getExpressionContext(), ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDetailDto(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleDetailDTO> updateRule(
            @PathVariable int id, @RequestBody CreateRuleRequest req) {
        try {
            Integer updaterId = 1;
            RuleEntity entity = ruleService.updateRule(
                    id, req.getName(), req.getDescription(), req.getEnabled(),
                    req.getExpressionValue(), req.getExpressionContext(), updaterId);
            return ResponseEntity.ok(toDetailDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable int id) {
        try {
            ruleService.deleteRule(id);
            return ResponseEntity.noContent().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}