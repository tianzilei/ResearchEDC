package org.researchedc.module.rule.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleRuleExpression")
@Table(name = "module_rule_expression")
public class RuleExpressionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_rule_expression_seq")
    @SequenceGenerator(name = "module_rule_expression_seq", sequenceName = "module_rule_expression_id_seq", allocationSize = 1)
    @Column(name = "rule_expression_id")
    private Integer ruleExpressionId;

    @Column(name = "value", length = 2040)
    private String value;

    @Column(name = "context")
    private Integer context;

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

    public Integer getRuleExpressionId() { return ruleExpressionId; }
    public void setRuleExpressionId(Integer v) { this.ruleExpressionId = v; }
    public String getValue() { return value; }
    public void setValue(String v) { this.value = v; }
    public Integer getContext() { return context; }
    public void setContext(Integer v) { this.context = v; }
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
