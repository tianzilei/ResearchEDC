package org.researchedc.module.legacy.dto;

import java.util.Date;
import java.util.List;

public class RuleSetDTO {
    private int ruleSetId;
    private String name;
    private String description;
    private String studyName;
    private int studyId;
    private String crfName;
    private String crfVersionName;
    private String eventDefinitionName;
    private String target;
    private int ownerId;
    private Date dateCreated;
    private List<String> ruleNames;

    public int getRuleSetId() { return ruleSetId; }
    public void setRuleSetId(int v) { this.ruleSetId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public String getStudyName() { return studyName; }
    public void setStudyName(String v) { this.studyName = v; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int v) { this.studyId = v; }
    public String getCrfName() { return crfName; }
    public void setCrfName(String v) { this.crfName = v; }
    public String getCrfVersionName() { return crfVersionName; }
    public void setCrfVersionName(String v) { this.crfVersionName = v; }
    public String getEventDefinitionName() { return eventDefinitionName; }
    public void setEventDefinitionName(String v) { this.eventDefinitionName = v; }
    public String getTarget() { return target; }
    public void setTarget(String v) { this.target = v; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int v) { this.ownerId = v; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date v) { this.dateCreated = v; }
    public List<String> getRuleNames() { return ruleNames; }
    public void setRuleNames(List<String> v) { this.ruleNames = v; }
}
