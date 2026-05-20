package org.researchedc.module.study.application.command;

import java.time.LocalDateTime;

public class UpdateStudyCommand {

    private Integer studyId;
    private String name;
    private String uniqueIdentifier;
    private String secondaryIdentifier;
    private String summary;
    private LocalDateTime datePlannedStart;
    private LocalDateTime datePlannedEnd;
    private Integer typeId;
    private Integer statusId;
    private String principalInvestigator;
    private String facilityName;
    private String facilityCity;
    private String facilityState;
    private String facilityCountry;
    private String facilityRecruitmentStatus;
    private String facilityContactName;
    private String facilityContactDegree;
    private String facilityContactPhone;
    private String facilityContactEmail;
    private String protocolType;
    private String protocolDescription;
    private String phase;
    private Integer expectedTotalEnrollment;
    private String sponsor;
    private String collaborators;
    private String officialTitle;
    private String conditions;
    private String keywords;
    private String eligibility;
    private String gender;
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
    private Integer updatedBy;

    public UpdateStudyCommand() {
    }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String uniqueIdentifier) { this.uniqueIdentifier = uniqueIdentifier; }

    public String getSecondaryIdentifier() { return secondaryIdentifier; }
    public void setSecondaryIdentifier(String secondaryIdentifier) { this.secondaryIdentifier = secondaryIdentifier; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public LocalDateTime getDatePlannedStart() { return datePlannedStart; }
    public void setDatePlannedStart(LocalDateTime datePlannedStart) { this.datePlannedStart = datePlannedStart; }

    public LocalDateTime getDatePlannedEnd() { return datePlannedEnd; }
    public void setDatePlannedEnd(LocalDateTime datePlannedEnd) { this.datePlannedEnd = datePlannedEnd; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String principalInvestigator) { this.principalInvestigator = principalInvestigator; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getFacilityCity() { return facilityCity; }
    public void setFacilityCity(String facilityCity) { this.facilityCity = facilityCity; }

    public String getFacilityState() { return facilityState; }
    public void setFacilityState(String facilityState) { this.facilityState = facilityState; }

    public String getFacilityCountry() { return facilityCountry; }
    public void setFacilityCountry(String facilityCountry) { this.facilityCountry = facilityCountry; }

    public String getFacilityRecruitmentStatus() { return facilityRecruitmentStatus; }
    public void setFacilityRecruitmentStatus(String facilityRecruitmentStatus) { this.facilityRecruitmentStatus = facilityRecruitmentStatus; }

    public String getFacilityContactName() { return facilityContactName; }
    public void setFacilityContactName(String facilityContactName) { this.facilityContactName = facilityContactName; }

    public String getFacilityContactDegree() { return facilityContactDegree; }
    public void setFacilityContactDegree(String facilityContactDegree) { this.facilityContactDegree = facilityContactDegree; }

    public String getFacilityContactPhone() { return facilityContactPhone; }
    public void setFacilityContactPhone(String facilityContactPhone) { this.facilityContactPhone = facilityContactPhone; }

    public String getFacilityContactEmail() { return facilityContactEmail; }
    public void setFacilityContactEmail(String facilityContactEmail) { this.facilityContactEmail = facilityContactEmail; }

    public String getProtocolType() { return protocolType; }
    public void setProtocolType(String protocolType) { this.protocolType = protocolType; }

    public String getProtocolDescription() { return protocolDescription; }
    public void setProtocolDescription(String protocolDescription) { this.protocolDescription = protocolDescription; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public Integer getExpectedTotalEnrollment() { return expectedTotalEnrollment; }
    public void setExpectedTotalEnrollment(Integer expectedTotalEnrollment) { this.expectedTotalEnrollment = expectedTotalEnrollment; }

    public String getSponsor() { return sponsor; }
    public void setSponsor(String sponsor) { this.sponsor = sponsor; }

    public String getCollaborators() { return collaborators; }
    public void setCollaborators(String collaborators) { this.collaborators = collaborators; }

    public String getOfficialTitle() { return officialTitle; }
    public void setOfficialTitle(String officialTitle) { this.officialTitle = officialTitle; }

    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getEligibility() { return eligibility; }
    public void setEligibility(String eligibility) { this.eligibility = eligibility; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getAllocation() { return allocation; }
    public void setAllocation(String allocation) { this.allocation = allocation; }

    public String getMasking() { return masking; }
    public void setMasking(String masking) { this.masking = masking; }

    public String getControl() { return control; }
    public void setControl(String control) { this.control = control; }

    public String getAssignment() { return assignment; }
    public void setAssignment(String assignment) { this.assignment = assignment; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getInterventions() { return interventions; }
    public void setInterventions(String interventions) { this.interventions = interventions; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getSelection() { return selection; }
    public void setSelection(String selection) { this.selection = selection; }

    public String getTiming() { return timing; }
    public void setTiming(String timing) { this.timing = timing; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }
}
