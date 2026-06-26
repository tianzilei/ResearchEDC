package org.researchedc.module.openrosa.dto;

public class SubmissionResponse {

    private String message;
    private int statusCode;

    public SubmissionResponse() {}

    public SubmissionResponse(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getMessage() { return message; }
    public void setMessage(String v) { this.message = v; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int v) { this.statusCode = v; }

    public String toXml() {
        return "<OpenRosaResponse xmlns=\"http://openrosa.org/http/response\">"
                + "<message>" + escapeXml(message) + "</message>"
                + "</OpenRosaResponse>";
    }

    public static SubmissionResponse success() {
        return new SubmissionResponse("success", 201);
    }

    public static SubmissionResponse error(String message) {
        return new SubmissionResponse(message, 406);
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
