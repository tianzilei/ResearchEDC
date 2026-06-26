package org.researchedc.module.study.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StudyDetailDTO {
    private Integer studyId;
    private Integer parentStudyId;
    private boolean site;
    private String name;
    private String uniqueIdentifier;
    private String secondaryIdentifier;
    private String ocOid;
    private String officialTitle;
    private String summary;
    private String phase;
    private String principalInvestigator;
    private String sponsor;
    private String collaborators;
    private String status;
    private Integer typeId;
    private String facilityName;
    private String facilityCity;
    private String facilityState;
    private String facilityCountry;
    private LocalDateTime datePlannedStart;
    private LocalDateTime datePlannedEnd;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Integer ownerId;
    private Integer expectedTotalEnrollment;
    private String protocolType;
    private String protocolDescription;
    private String conditions;
    private String keywords;
    private String eligibility;
    private String gender;
    private String purpose;
    private String allocation;
    private String masking;
    private List<StudySummaryDTO> sites;

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getParentStudyId() { return parentStudyId; }
    public void setParentStudyId(Integer v) { this.parentStudyId = v; }
    public boolean isSite() { return site; }
    public void setSite(boolean v) { this.site = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String v) { this.uniqueIdentifier = v; }
    public String getSecondaryIdentifier() { return secondaryIdentifier; }
    public void setSecondaryIdentifier(String v) { this.secondaryIdentifier = v; }
    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }
    public String getOfficialTitle() { return officialTitle; }
    public void setOfficialTitle(String v) { this.officialTitle = v; }
    public String getSummary() { return summary; }
    public void setSummary(String v) { this.summary = v; }
    public String getPhase() { return phase; }
    public void setPhase(String v) { this.phase = v; }
    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String v) { this.principalInvestigator = v; }
    public String getSponsor() { return sponsor; }
    public void setSponsor(String v) { this.sponsor = v; }
    public String getCollaborators() { return collaborators; }
    public void setCollaborators(String v) { this.collaborators = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer v) { this.typeId = v; }
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String v) { this.facilityName = v; }
    public String getFacilityCity() { return facilityCity; }
    public void setFacilityCity(String v) { this.facilityCity = v; }
    public String getFacilityState() { return facilityState; }
    public void setFacilityState(String v) { this.facilityState = v; }
    public String getFacilityCountry() { return facilityCountry; }
    public void setFacilityCountry(String v) { this.facilityCountry = v; }
    public LocalDateTime getDatePlannedStart() { return datePlannedStart; }
    public void setDatePlannedStart(LocalDateTime v) { this.datePlannedStart = v; }
    public LocalDateTime getDatePlannedEnd() { return datePlannedEnd; }
    public void setDatePlannedEnd(LocalDateTime v) { this.datePlannedEnd = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getExpectedTotalEnrollment() { return expectedTotalEnrollment; }
    public void setExpectedTotalEnrollment(Integer v) { this.expectedTotalEnrollment = v; }
    public String getProtocolType() { return protocolType; }
    public void setProtocolType(String v) { this.protocolType = v; }
    public String getProtocolDescription() { return protocolDescription; }
    public void setProtocolDescription(String v) { this.protocolDescription = v; }
    public String getConditions() { return conditions; }
    public void setConditions(String v) { this.conditions = v; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String v) { this.keywords = v; }
    public String getEligibility() { return eligibility; }
    public void setEligibility(String v) { this.eligibility = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String v) { this.purpose = v; }
    public String getAllocation() { return allocation; }
    public void setAllocation(String v) { this.allocation = v; }
    public String getMasking() { return masking; }
    public void setMasking(String v) { this.masking = v; }
    public List<StudySummaryDTO> getSites() { return sites; }
    public void setSites(List<StudySummaryDTO> v) { this.sites = v; }
}
