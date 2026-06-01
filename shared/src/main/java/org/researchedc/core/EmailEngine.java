package org.researchedc.core;

public class EmailEngine {
    public EmailEngine() {}
    public EmailEngine(String smtpHost) {}
    public static String getAdminEmail() { return ""; }
    public static String getSMTPHost() { return ""; }
    public static void sendMail(String to, String subject, String body) {}
}
