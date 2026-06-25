package org.researchedc.module.rule.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RuleSetDTO {
    private Integer ruleSetId;
    private Integer studyId;
    private Integer ownerId;
    private LocalDateTime dateCreated;
    private List<Integer> ruleIds;

    public Integer getRuleSetId() { return ruleSetId; }
    public void setRuleSetId(Integer v) { this.ruleSetId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public List<Integer> getRuleIds() { return ruleIds; }
    public void setRuleIds(List<Integer> v) { this.ruleIds = v; }
}
