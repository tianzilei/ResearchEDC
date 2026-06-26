package org.researchedc.module.rule.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleRule")
@Table(name = "module_rule")
public class RuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_rule_seq")
    @SequenceGenerator(name = "module_rule_seq", sequenceName = "module_rule_id_seq", allocationSize = 1)
    @Column(name = "rule_id")
    private Integer ruleId;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "rule_expression_id")
    private Integer ruleExpressionId;

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

    public Integer getRuleId() { return ruleId; }
    public void setRuleId(Integer v) { this.ruleId = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean v) { this.enabled = v; }
    public Integer getRuleExpressionId() { return ruleExpressionId; }
    public void setRuleExpressionId(Integer v) { this.ruleExpressionId = v; }
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
