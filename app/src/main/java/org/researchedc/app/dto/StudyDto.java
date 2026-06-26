package org.researchedc.app.dto;

import org.researchedc.app.dto.AuditableEntity;

import java.util.Date;

public class StudyDto extends AuditableEntity {
    public static final int TYPE_GENETIC = 1;
    public static final int TYPE_NON_GENETIC = 2;

    private int parentStudyId;
    private String officialTitle;
    private String identifier;
    private String secondaryIdentifier;
    private String summary;
    private Date datePlannedStart;
    private Date datePlannedEnd;
    private int typeId;
    private String principalInvestigator;
    private String facilityName;
    private String facilityCity;
    private String facilityState;
    private String facilityZip;
    private String facilityCountry;
    private String facilityRecruitmentStatus;
    private String facilityContactName;
    private String facilityContactDegree;
    private String facilityContactPhone;
    private String protocolType;
    private String protocolDescription;
    private Date protocolDateVerification;
    private String phase;
    private int expectedTotalEnrollment;
    private String sponsor;
    private String collaborators;
    private String medlineIdentifier;
    private boolean resultsReference;
    private String oid;
    private String url;
    private String urlDescription;
    private String conditions;
    private String keywords;
    private String eligibility;
    private String gender;
    private String ageMax;
    private String ageMin;
    private boolean healthyVolunteerAccepted;
    private String purpose;
    private String allocation;
    private String masking;
    private String control;
    private String assignment;
    private String endpoint;
    private String interventions;
    private String duration;
    private String selection;
    private String timing;

    public StudyDto() {
        parentStudyId = 0;
        officialTitle = "";
        identifier = "";
        secondaryIdentifier = "";
        summary = "";
        typeId = TYPE_NON_GENETIC;
        principalInvestigator = "";
        facilityName = "";
        facilityCity = "";
        facilityState = "";
        facilityZip = "";
        facilityCountry = "";
        facilityRecruitmentStatus = "";
        facilityContactName = "";
        facilityContactDegree = "";
        facilityContactPhone = "";
        protocolType = "";
        protocolDescription = "";
        phase = "";
        expectedTotalEnrollment = 0;
        sponsor = "n_a";
        collaborators = "";
        medlineIdentifier = "";
        resultsReference = false;
        url = "";
        urlDescription = "";
        conditions = "";
        keywords = "";
        eligibility = "";
        gender = "both";
        ageMax = "";
        ageMin = "";
        healthyVolunteerAccepted = false;
        purpose = "";
        allocation = "";
        masking = "";
        control = "";
        assignment = "";
        endpoint = "";
        interventions = "";
        duration = "";
        selection = "";
        timing = "";
    }

    public int getParentStudyId() { return parentStudyId; }
    public void setParentStudyId(int v) { this.parentStudyId = v; }
    public String getOfficialTitle() { return officialTitle; }
    public void setOfficialTitle(String v) { this.officialTitle = v; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String v) { this.identifier = v; }
    public String getSecondaryIdentifier() { return secondaryIdentifier; }
    public void setSecondaryIdentifier(String v) { this.secondaryIdentifier = v; }
    public String getSummary() { return summary; }
    public void setSummary(String v) { this.summary = v; }
    public Date getDatePlannedStart() { return datePlannedStart; }
    public void setDatePlannedStart(Date v) { this.datePlannedStart = v; }
    public Date getDatePlannedEnd() { return datePlannedEnd; }
    public void setDatePlannedEnd(Date v) { this.datePlannedEnd = v; }
    public int getTypeId() { return typeId; }
    public void setTypeId(int v) { this.typeId = v; }
    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String v) { this.principalInvestigator = v; }
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String v) { this.facilityName = v; }
    public String getFacilityCity() { return facilityCity; }
    public void setFacilityCity(String v) { this.facilityCity = v; }
    public String getFacilityState() { return facilityState; }
    public void setFacilityState(String v) { this.facilityState = v; }
    public String getFacilityZip() { return facilityZip; }
    public void setFacilityZip(String v) { this.facilityZip = v; }
    public String getFacilityCountry() { return facilityCountry; }
    public void setFacilityCountry(String v) { this.facilityCountry = v; }
    public String getFacilityRecruitmentStatus() { return facilityRecruitmentStatus; }
    public void setFacilityRecruitmentStatus(String v) { this.facilityRecruitmentStatus = v; }
    public String getFacilityContactName() { return facilityContactName; }
    public void setFacilityContactName(String v) { this.facilityContactName = v; }
    public String getFacilityContactDegree() { return facilityContactDegree; }
    public void setFacilityContactDegree(String v) { this.facilityContactDegree = v; }
    public String getFacilityContactPhone() { return facilityContactPhone; }
    public void setFacilityContactPhone(String v) { this.facilityContactPhone = v; }
    public String getProtocolType() { return protocolType; }
    public void setProtocolType(String v) { this.protocolType = v; }
    public String getProtocolDescription() { return protocolDescription; }
    public void setProtocolDescription(String v) { this.protocolDescription = v; }
    public Date getProtocolDateVerification() { return protocolDateVerification; }
    public void setProtocolDateVerification(Date v) { this.protocolDateVerification = v; }
    public String getPhase() { return phase; }
    public void setPhase(String v) { this.phase = v; }
    public int getExpectedTotalEnrollment() { return expectedTotalEnrollment; }
    public void setExpectedTotalEnrollment(int v) { this.expectedTotalEnrollment = v; }
    public String getSponsor() { return sponsor; }
    public void setSponsor(String v) { this.sponsor = v; }
    public String getCollaborators() { return collaborators; }
    public void setCollaborators(String v) { this.collaborators = v; }
    public String getMedlineIdentifier() { return medlineIdentifier; }
    public void setMedlineIdentifier(String v) { this.medlineIdentifier = v; }
    public boolean isResultsReference() { return resultsReference; }
    public void setResultsReference(boolean v) { this.resultsReference = v; }
    public String getOid() { return oid; }
    public void setOid(String v) { this.oid = v; }
    public String getUrl() { return url; }
    public void setUrl(String v) { this.url = v; }
    public String getUrlDescription() { return urlDescription; }
    public void setUrlDescription(String v) { this.urlDescription = v; }
    public String getConditions() { return conditions; }
    public void setConditions(String v) { this.conditions = v; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String v) { this.keywords = v; }
    public String getEligibility() { return eligibility; }
    public void setEligibility(String v) { this.eligibility = v; }
    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }
    public String getAgeMax() { return ageMax; }
    public void setAgeMax(String v) { this.ageMax = v; }
    public String getAgeMin() { return ageMin; }
    public void setAgeMin(String v) { this.ageMin = v; }
    public boolean getHealthyVolunteerAccepted() { return healthyVolunteerAccepted; }
    public void setHealthyVolunteerAccepted(boolean v) { this.healthyVolunteerAccepted = v; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String v) { this.purpose = v; }
    public String getAllocation() { return allocation; }
    public void setAllocation(String v) { this.allocation = v; }
    public String getMasking() { return masking; }
    public void setMasking(String v) { this.masking = v; }
    public String getControl() { return control; }
    public void setControl(String v) { this.control = v; }
    public String getAssignment() { return assignment; }
    public void setAssignment(String v) { this.assignment = v; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String v) { this.endpoint = v; }
    public String getInterventions() { return interventions; }
    public void setInterventions(String v) { this.interventions = v; }
    public String getDuration() { return duration; }
    public void setDuration(String v) { this.duration = v; }
    public String getSelection() { return selection; }
    public void setSelection(String v) { this.selection = v; }
    public String getTiming() { return timing; }
    public void setTiming(String v) { this.timing = v; }
}
