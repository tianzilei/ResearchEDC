package org.researchedc.domain.rule.action;
public class NotificationActionBean extends RuleActionBean {
    private String message;
    private String subject;
    private String to;
    private Boolean expressionEvaluatesTo;
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { this.subject = v; }
    public String getTo() { return to; }
    public void setTo(String v) { this.to = v; }
    public Boolean getExpressionEvaluatesTo() { return expressionEvaluatesTo; }
    public void setExpressionEvaluatesTo(Boolean v) { this.expressionEvaluatesTo = v; }
}
