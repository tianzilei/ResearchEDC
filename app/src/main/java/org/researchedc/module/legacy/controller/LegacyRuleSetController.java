package org.researchedc.module.legacy.controller;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.researchedc.bean.rule.RuleBean;
import org.researchedc.bean.rule.RuleSetBean;
import org.researchedc.bean.managestudy.StudyBean;
import org.researchedc.dao.managestudy.StudyDAO;
import org.researchedc.dao.rule.RuleDAO;
import org.researchedc.dao.rule.RuleSetDAO;
import org.researchedc.module.legacy.dto.RuleSetDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/legacy/rule-sets")
public class LegacyRuleSetController {

    private final RuleSetDAO ruleSetDao;
    private final RuleDAO ruleDao;
    private final StudyDAO studyDao;

    public LegacyRuleSetController(DataSource dataSource) {
        this.ruleSetDao = new RuleSetDAO(dataSource);
        this.ruleDao = new RuleDAO(dataSource);
        this.studyDao = new StudyDAO(dataSource);
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<RuleSetDTO>> listRuleSets(
            @RequestParam(required = false) Integer studyId) {
        List<RuleSetDTO> result = new ArrayList<>();
        if (studyId != null) {
            StudyBean study = (StudyBean) studyDao.findByPK(studyId);
            if (study == null || study.getId() == 0) {
                return ResponseEntity.ok(List.of());
            }
            for (RuleSetBean bean : ruleSetDao.findAllByStudy(study)) {
                result.add(toDto(bean));
            }
        } else {
            for (Object obj : ruleSetDao.findAll()) {
                result.add(toDto((RuleSetBean) obj));
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<RuleSetDTO> getRuleSet(@PathVariable int id) {
        RuleSetBean bean = (RuleSetBean) ruleSetDao.findByPK(id);
        if (bean == null || bean.getId() == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toDto(bean));
    }

    private RuleSetDTO toDto(RuleSetBean bean) {
        RuleSetDTO dto = new RuleSetDTO();
        dto.setRuleSetId(bean.getId());
        dto.setName(bean.getName());
        dto.setDescription(bean.getName());
        dto.setStudyName(bean.getStudy() != null ? bean.getStudy().getName() : null);
        dto.setStudyId(bean.getStudy() != null ? bean.getStudy().getId() : 0);
        dto.setCrfName(bean.getCrf() != null ? bean.getCrf().getName() : null);
        dto.setCrfVersionName(bean.getCrfVersion() != null ? bean.getCrfVersion().getName() : null);
        dto.setEventDefinitionName(bean.getStudyEventDefinition() != null ? bean.getStudyEventDefinition().getName() : null);
        dto.setTarget(bean.getTarget() != null ? bean.getTarget().getValue() : null);
        dto.setOwnerId(bean.getOwnerId());
        dto.setDateCreated(bean.getCreatedDate());

        List<String> ruleNames = new ArrayList<>();
        if (bean.getRuleSetRules() != null) {
            for (Object rsr : bean.getRuleSetRules()) {
                org.researchedc.bean.rule.RuleSetRuleBean rsrb = (org.researchedc.bean.rule.RuleSetRuleBean) rsr;
                if (rsrb.getRuleBean() != null) {
                    ruleNames.add(rsrb.getRuleBean().getName());
                }
            }
        }
        dto.setRuleNames(ruleNames);
        return dto;
    }
}
