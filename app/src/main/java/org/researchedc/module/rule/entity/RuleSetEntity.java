package org.researchedc.module.rule.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleRuleSet")
@Table(name = "rule_set")
public class RuleSetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rule_set_seq")
    @SequenceGenerator(name = "rule_set_seq", sequenceName = "rule_set_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Integer ruleSetId;

    @Column(name = "rule_expression_id")
    private Integer ruleExpressionId;

    @Column(name = "study_event_definition_id")
    private Integer studyEventDefinitionId;

    @Column(name = "crf_id")
    private Integer crfId;

    @Column(name = "crf_version_id")
    private Integer crfVersionId;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "run_schedule")
    private Boolean runSchedule;

    @Column(name = "run_time", length = 255)
    private String runTime;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "version")
    private Integer version;

    public Integer getRuleSetId() { return ruleSetId; }
    public void setRuleSetId(Integer v) { this.ruleSetId = v; }
    public Integer getRuleExpressionId() { return ruleExpressionId; }
    public void setRuleExpressionId(Integer v) { this.ruleExpressionId = v; }
    public Integer getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(Integer v) { this.studyEventDefinitionId = v; }
    public Integer getCrfId() { return crfId; }
    public void setCrfId(Integer v) { this.crfId = v; }
    public Integer getCrfVersionId() { return crfVersionId; }
    public void setCrfVersionId(Integer v) { this.crfVersionId = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Boolean getRunSchedule() { return runSchedule; }
    public void setRunSchedule(Boolean v) { this.runSchedule = v; }
    public String getRunTime() { return runTime; }
    public void setRunTime(String v) { this.runTime = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { this.version = v; }
}
