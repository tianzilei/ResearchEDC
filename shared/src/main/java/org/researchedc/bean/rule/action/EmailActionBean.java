package org.researchedc.bean.rule.action;
public class EmailActionBean extends RuleActionBean {
    private String to, subject, message;
    public String getTo() { return to; } public void setTo(String v) { to = v; }
    public String getSubject() { return subject; } public void setSubject(String v) { subject = v; }
    public String getMessage() { return message; } public void setMessage(String v) { message = v; }
}
