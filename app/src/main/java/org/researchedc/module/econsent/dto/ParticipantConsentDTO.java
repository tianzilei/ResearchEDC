package org.researchedc.module.econsent.dto;

public class ParticipantConsentDTO {
    private ConsentAssignmentDTO assignment;
    private ConsentVersionDTO version;
    private ConsentTemplateDTO template;

    public ConsentAssignmentDTO getAssignment() { return assignment; }
    public void setAssignment(ConsentAssignmentDTO assignment) { this.assignment = assignment; }

    public ConsentVersionDTO getVersion() { return version; }
    public void setVersion(ConsentVersionDTO version) { this.version = version; }

    public ConsentTemplateDTO getTemplate() { return template; }
    public void setTemplate(ConsentTemplateDTO template) { this.template = template; }
}
