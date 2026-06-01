package org.researchedc.domain.rule.action;
public class EmailActionBean extends RuleActionBean {
    private String to;
    private String subject;
    private String message;
    public String getTo() { return to; }
    public void setTo(String v) { this.to = v; }
    public String getSubject() { return subject; }
    public void setSubject(String v) { this.subject = v; }
    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
}
