package org.researchedc.module.legacy.dto;

public class CreateRuleSetRequest {
    private Integer studyId;
    private Integer crfId;
    private Integer crfVersionId;
    private Integer studyEventDefinitionId;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
}