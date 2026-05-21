package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;
import org.researchedc.module.legacy.dto.RuleSetDTO;
import org.researchedc.module.rule.entity.RuleSetEntity;
import org.researchedc.module.rule.entity.RuleSetRuleEntity;
import org.researchedc.module.rule.service.RuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/rule-sets")
public class LegacyRuleSetController {

    private final RuleService ruleService;

    public LegacyRuleSetController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ResponseEntity<List<RuleSetDTO>> listRuleSets(
            @RequestParam(required = false) Integer studyId) {
        List<RuleSetDTO> result = new ArrayList<>();
        List<RuleSetEntity> entities;
        if (studyId != null) {
            entities = ruleService.listRuleSetsByStudy(studyId);
        } else {
            entities = ruleService.listAllRuleSets();
        }
        for (RuleSetEntity entity : entities) {
            result.add(toDto(entity));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleSetDTO> getRuleSet(@PathVariable int id) {
        try {
            RuleSetEntity entity = ruleService.getRuleSet(id);
            return ResponseEntity.ok(toDto(entity));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private RuleSetDTO toDto(RuleSetEntity entity) {
        RuleSetDTO dto = new RuleSetDTO();
        dto.setRuleSetId(entity.getRuleSetId());
        dto.setName(null);
        dto.setDescription(null);
        dto.setStudyName(entity.getStudyId() != null ? String.valueOf(entity.getStudyId()) : null);
        dto.setStudyId(entity.getStudyId() != null ? entity.getStudyId() : 0);
        dto.setCrfName(entity.getCrfId() != null ? String.valueOf(entity.getCrfId()) : null);
        dto.setCrfVersionName(entity.getCrfVersionId() != null ? String.valueOf(entity.getCrfVersionId()) : null);
        dto.setEventDefinitionName(entity.getStudyEventDefinitionId() != null ? String.valueOf(entity.getStudyEventDefinitionId()) : null);
        dto.setTarget(null);
        dto.setOwnerId(entity.getOwnerId() != null ? entity.getOwnerId() : 0);
        dto.setDateCreated(entity.getDateCreated() != null ?
                java.util.Date.from(entity.getDateCreated().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null);

        List<String> ruleNames = new ArrayList<>();
        for (RuleSetRuleEntity rsr : ruleService.listRuleSetRules(entity.getRuleSetId())) {
            ruleNames.add("rule-" + rsr.getRuleId());
        }
        dto.setRuleNames(ruleNames);
        return dto;
    }
}
