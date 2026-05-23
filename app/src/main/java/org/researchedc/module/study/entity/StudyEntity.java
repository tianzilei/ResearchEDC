package org.researchedc.module.study.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleStudy")
@Table(name = "module_study")
public class StudyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_study_seq")
    @SequenceGenerator(name = "module_study_seq", sequenceName = "module_study_id_seq", allocationSize = 1)
    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "parent_study_id")
    private Integer parentStudyId;

    @Column(name = "unique_identifier", length = 30)
    private String uniqueIdentifier;

    @Column(name = "secondary_identifier", length = 255)
    private String secondaryIdentifier;

    @Column(length = 60)
    private String name;

    @Column(length = 255)
    private String summary;

    @Column(name = "date_planned_start")
    private LocalDateTime datePlannedStart;

    @Column(name = "date_planned_end")
    private LocalDateTime datePlannedEnd;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "type_id")
    private Integer typeId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "principal_investigator", length = 255)
    private String principalInvestigator;

    @Column(name = "facility_name", length = 255)
    private String facilityName;

    @Column(name = "facility_city", length = 255)
    private String facilityCity;

    @Column(name = "facility_state", length = 20)
    private String facilityState;

    @Column(name = "facility_zip", length = 64)
    private String facilityZip;

    @Column(name = "facility_country", length = 64)
    private String facilityCountry;

    @Column(name = "facility_recruitment_status", length = 60)
    private String facilityRecruitmentStatus;

    @Column(name = "facility_contact_name", length = 255)
    private String facilityContactName;

    @Column(name = "facility_contact_degree", length = 255)
    private String facilityContactDegree;

    @Column(name = "facility_contact_phone", length = 255)
    private String facilityContactPhone;

    @Column(name = "facility_contact_email", length = 255)
    private String facilityContactEmail;

    @Column(name = "protocol_type", length = 30)
    private String protocolType;

    @Column(name = "protocol_description", length = 1000)
    private String protocolDescription;

    @Column(name = "protocol_date_verification")
    private LocalDateTime protocolDateVerification;

    @Column(length = 30)
    private String phase;

    @Column(name = "expected_total_enrollment")
    private Integer expectedTotalEnrollment;

    @Column(length = 255)
    private String sponsor;

    @Column(length = 1000)
    private String collaborators;

    @Column(name = "medline_identifier", length = 255)
    private String medlineIdentifier;

    @Column(length = 255)
    private String url;

    @Column(name = "url_description", length = 255)
    private String urlDescription;

    @Column(length = 1000)
    private String conditions;

    @Column(length = 1000)
    private String keywords;

    @Column(length = 1000)
    private String eligibility;

    @Column(length = 1)
    private String gender;

    @Column(name = "age_max", length = 3)
    private String ageMax;

    @Column(name = "age_min", length = 3)
    private String ageMin;

    @Column(name = "healthy_volunteer_accepted")
    private Boolean healthyVolunteerAccepted;

    @Column(length = 100)
    private String purpose;

    @Column(length = 100)
    private String allocation;

    @Column(length = 100)
    private String masking;

    @Column(length = 100)
    private String control;

    @Column(length = 100)
    private String assignment;

    @Column(length = 100)
    private String endpoint;

    @Column(length = 100)
    private String interventions;

    @Column(length = 100)
    private String duration;

    @Column(length = 100)
    private String selection;

    @Column(length = 100)
    private String timing;

    @Column(name = "official_title", length = 1000)
    private String officialTitle;

    @Column(name = "results_reference")
    private Boolean resultsReference;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "old_status_id")
    private Integer oldStatusId;

    @Column
    private Integer version;

    @Column(name = "feature_flags", columnDefinition = "JSONB")
    private String featureFlags;

    public String getFeatureFlags() { return featureFlags; }
    public void setFeatureFlags(String v) { this.featureFlags = v; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer studyId) { this.studyId = studyId; }

    public Integer getParentStudyId() { return parentStudyId; }
    public void setParentStudyId(Integer parentStudyId) { this.parentStudyId = parentStudyId; }

    public boolean isSite() { return parentStudyId != null && parentStudyId > 0; }

    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String v) { this.uniqueIdentifier = v; }

    public String getSecondaryIdentifier() { return secondaryIdentifier; }
    public void setSecondaryIdentifier(String v) { this.secondaryIdentifier = v; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

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

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer v) { this.typeId = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }

    public String getPrincipalInvestigator() { return principalInvestigator; }
    public void setPrincipalInvestigator(String v) { this.principalInvestigator = v; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String v) { this.facilityName = v; }

    public String getFacilityCity() { return facilityCity; }
    public void setFacilityCity(String v) { this.facilityCity = v; }

    public String getFacilityState() { return facilityState; }
    public void setFacilityState(String v) { this.facilityState = v; }

    public String getFacilityCountry() { return facilityCountry; }
    public void setFacilityCountry(String v) { this.facilityCountry = v; }

    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }

    public String getPhase() { return phase; }
    public void setPhase(String v) { this.phase = v; }

    public Integer getExpectedTotalEnrollment() { return expectedTotalEnrollment; }
    public void setExpectedTotalEnrollment(Integer v) { this.expectedTotalEnrollment = v; }

    public String getSponsor() { return sponsor; }
    public void setSponsor(String v) { this.sponsor = v; }

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

    public String getOfficialTitle() { return officialTitle; }
    public void setOfficialTitle(String v) { this.officialTitle = v; }

    public String getCollaborators() { return collaborators; }
    public void setCollaborators(String v) { this.collaborators = v; }

    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String v) { this.purpose = v; }

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

    public Boolean getResultsReference() { return resultsReference; }
    public void setResultsReference(Boolean v) { this.resultsReference = v; }
}
