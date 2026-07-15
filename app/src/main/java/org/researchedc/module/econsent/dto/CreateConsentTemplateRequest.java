package org.researchedc.module.econsent.dto;

public class CreateConsentTemplateRequest {
    private Integer studyId;
    private String code;
    private String name;
    private String description;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
