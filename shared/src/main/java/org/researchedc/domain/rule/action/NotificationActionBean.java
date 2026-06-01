package org.researchedc.domain.rule.action;
public class NotificationActionBean extends RuleActionBean {
    private String message, subject, to;
    private Boolean expressionEvaluatesTo;
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
    public String getSubject() { return subject; } public void setSubject(String v) { subject = v; }
    public String getTo() { return to; } public void setTo(String v) { to = v; }
    public Boolean getExpressionEvaluatesTo() { return expressionEvaluatesTo; } public void setExpressionEvaluatesTo(Boolean v) { expressionEvaluatesTo = v; }
}
