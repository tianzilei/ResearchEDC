package org.researchedc.module.fhir.dto;

public class CreateFhirConnectorRequest {
    private Integer studyId;
    private String name;
    private String baseUrl;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
}
