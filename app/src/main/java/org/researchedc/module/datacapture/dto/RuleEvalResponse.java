package org.researchedc.module.datacapture.dto;

import java.util.List;

/**
 * Response DTO for rule evaluation on an EventCRF.
 * Returns applicable rules with their expressions, without
 * performing full expression evaluation.
 */
public class RuleEvalResponse {
    private int eventCrfId;
    private int ruleSetCount;
    private List<RuleInfo> rules;

    public int getEventCrfId() { return eventCrfId; }
    public void setEventCrfId(int v) { this.eventCrfId = v; }
    public int getRuleSetCount() { return ruleSetCount; }
    public void setRuleSetCount(int v) { this.ruleSetCount = v; }
    public List<RuleInfo> getRules() { return rules; }
    public void setRules(List<RuleInfo> v) { this.rules = v; }

    public static class RuleInfo {
        private String ruleName;
        private String ruleDescription;
        private String expressionValue;
        private boolean enabled;

        public String getRuleName() { return ruleName; }
        public void setRuleName(String v) { this.ruleName = v; }
        public String getRuleDescription() { return ruleDescription; }
        public void setRuleDescription(String v) { this.ruleDescription = v; }
        public String getExpressionValue() { return expressionValue; }
        public void setExpressionValue(String v) { this.expressionValue = v; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean v) { this.enabled = v; }
    }
}
