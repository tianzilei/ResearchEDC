package org.researchedc.module.openrosa.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a parsed OpenRosa submission request.
 * Contains the XML body, subject context, study OID, and any file attachments.
 */
public class SubmissionRequest {

    private String studyOid;
    private String xmlSubmission;
    private String ecid;
    private Map<String, byte[]> attachments = new HashMap<>();

    // Parsed context from ecid
    private String studySubjectOid;
    private String studyEventDefinitionId;
    private String studyEventOrdinal;
    private String crfVersionOid;

    public String getStudyOid() { return studyOid; }
    public void setStudyOid(String v) { this.studyOid = v; }

    public String getXmlSubmission() { return xmlSubmission; }
    public void setXmlSubmission(String v) { this.xmlSubmission = v; }

    public String getEcid() { return ecid; }
    public void setEcid(String v) { this.ecid = v; }

    public Map<String, byte[]> getAttachments() { return attachments; }
    public void setAttachments(Map<String, byte[]> v) { this.attachments = v; }

    public String getStudySubjectOid() { return studySubjectOid; }
    public void setStudySubjectOid(String v) { this.studySubjectOid = v; }

    public String getStudyEventDefinitionId() { return studyEventDefinitionId; }
    public void setStudyEventDefinitionId(String v) { this.studyEventDefinitionId = v; }

    public String getStudyEventOrdinal() { return studyEventOrdinal; }
    public void setStudyEventOrdinal(String v) { this.studyEventOrdinal = v; }

    public String getCrfVersionOid() { return crfVersionOid; }
    public void setCrfVersionOid(String v) { this.crfVersionOid = v; }

    public boolean isComplete() {
        return studyOid != null && xmlSubmission != null
                && crfVersionOid != null;
    }
}
