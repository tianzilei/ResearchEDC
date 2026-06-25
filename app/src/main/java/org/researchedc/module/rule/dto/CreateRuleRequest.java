package org.researchedc.module.rule.dto;

public class CreateRuleRequest {
    private String name;
    private String description;
    private Boolean enabled;
    private String expressionValue;
    private Integer expressionContext;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean v) { this.enabled = v; }
    public String getExpressionValue() { return expressionValue; }
    public void setExpressionValue(String v) { this.expressionValue = v; }
    public Integer getExpressionContext() { return expressionContext; }
    public void setExpressionContext(Integer v) { this.expressionContext = v; }
}
