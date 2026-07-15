package org.researchedc.module.studybuild.dto;

public class ApplyStudyTemplateRequest {
    private String name;
    private String uniqueIdentifier;
    private String principalInvestigator;
    private String facilityName;
    private String sponsor;
    private Integer expectedTotalEnrollment;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String uniqueIdentifier) { this.uniqueIdentifier = uniqueIdentifier; }
    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String principalInvestigator) { this.principalInvestigator = principalInvestigator; }
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    public String getSponsor() { return sponsor; }
    public void setSponsor(String sponsor) { this.sponsor = sponsor; }
    public Integer getExpectedTotalEnrollment() { return expectedTotalEnrollment; }
    public void setExpectedTotalEnrollment(Integer expectedTotalEnrollment) { this.expectedTotalEnrollment = expectedTotalEnrollment; }
}
