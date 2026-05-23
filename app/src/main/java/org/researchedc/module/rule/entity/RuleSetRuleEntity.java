package org.researchedc.module.rule.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleRuleSetRule")
@Table(name = "module_rule_set_rule")
public class RuleSetRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_rule_set_rule_seq")
    @SequenceGenerator(name = "module_rule_set_rule_seq", sequenceName = "module_rule_set_rule_id_seq", allocationSize = 1)
    @Column(name = "rule_set_rule_id")
    private Integer ruleSetRuleId;

    @Column(name = "rule_set_id")
    private Integer ruleSetId;

    @Column(name = "rule_id")
    private Integer ruleId;

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

    public Integer getRuleSetRuleId() { return ruleSetRuleId; }
    public void setRuleSetRuleId(Integer v) { this.ruleSetRuleId = v; }
    public Integer getRuleSetId() { return ruleSetId; }
    public void setRuleSetId(Integer v) { this.ruleSetId = v; }
    public Integer getRuleId() { return ruleId; }
    public void setRuleId(Integer v) { this.ruleId = v; }
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
