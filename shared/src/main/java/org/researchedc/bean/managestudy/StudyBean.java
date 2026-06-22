/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.researchedc.bean.managestudy;

import org.researchedc.bean.core.AuditableEntityBean;
import org.researchedc.bean.core.Status;

import java.util.ArrayList;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * @author thickerson
 * 
 * 
 */
public class StudyBean extends AuditableEntityBean {
    private int parentStudyId = 0;
    // YW << The original reason to add this is being able to list on
    // userbox.jsp the study name to which a site belong.
    // This property doesn't exist in the database table <Study>, so it might
    // not has value if it hasn't been assigned
    private String parentStudyName = "";
    // >> YW 06-12-2007
    private String officialTitle = "";
    private String identifier = "";
    private String secondaryIdentifier = "";
    private String summary = "";// need to be removed

    private Date datePlannedStart;
    private Date datePlannedEnd;

    public static final int TYPE_GENETIC = 1;
    public static final int TYPE_NON_GENETIC = 2;
    private static final String TYPE_GENETIC_NAME = "genetic";
    private static final String TYPE_NON_GENETIC_NAME = "non_genetic";

    // to designate genetic/non-genetic:
    private int typeId = TYPE_NON_GENETIC;

    /**
     * <code>true</code> if the study manages pedigrees, <code>false</code>
     * otherwise Always equal to typeId == TYPE_GENETIC. Not in the
     * database.
     */
    private boolean genetic = false;

    // private int statusId = 0;//?

    private String principalInvestigator = "";
    private String facilityName = "";
    private String facilityCity = "";
    private String facilityState = "";
    private String facilityZip = "";
    private String facilityCountry = "";
    private String facilityRecruitmentStatus = "";
    private String facilityContactName = "";
    private String facilityContactDegree = "";
    private String facilityContactPhone = "";
    private String protocolType = "";
    private String protocolDescription = "";
    private Date protocolDateVerification;
    private String phase = "";
    private int expectedTotalEnrollment = 0;
    private String sponsor = "n_a";
    private String collaborators = "";
    private String medlineIdentifier = "";
    private boolean resultsReference = false;
    
    // private boolean usingDOB = false;
    // private boolean discrepancyManagement = false;
    private String oid;

    private ArrayList studyParameters = new ArrayList();

    /**
     * @return Returns the studyParameters.
     */
    public ArrayList getStudyParameters() {
        return studyParameters;
    }

    /**
     * @param studyParameters
     *            The studyParameters to set.
     */
    public void setStudyParameters(ArrayList studyParameters) {
        this.studyParameters = studyParameters;
    }

    /**
     * @return Returns the officialTitle.
     */
    public String getOfficialTitle() {
        return officialTitle;
    }

    /**
     * @param officialTitle
     *            The officialTitle to set.
     */
    public void setOfficialTitle(String officialTitle) {
        this.officialTitle = officialTitle;
    }

    /**
     * @return Returns the resultsReference.
     */
    public boolean isResultsReference() {
        return resultsReference;
    }

    /**
     * @param resultsReference
     *            The resultsReference to set.
     */
    public void setResultsReference(boolean resultsReference) {
        this.resultsReference = resultsReference;
    }

    private String url = "";
    private String urlDescription = "";
    private String conditions = "";
    private String keywords = "";
    private String eligibility = "";
    private String gender = "both";
    private String ageMax = "";
    private String ageMin = "";
    private boolean healthyVolunteerAccepted = false;
    private String purpose = "";
    private String allocation = "";
    private String masking = "";
    private String control = "";
    private String assignment = "";
    private String endpoint = "";
    private String interventions = "";
    private String duration = "";
    private String selection = "";
    private String timing = "";

    /**
     * @return Returns the ageMax.
     */
    public String getAgeMax() {
        return ageMax;
    }

    /**
     * @param ageMax
     *            The ageMax to set.
     */
    public void setAgeMax(String ageMax) {
        this.ageMax = ageMax;
    }

    /**
     * @return Returns the ageMin.
     */
    public String getAgeMin() {
        return ageMin;
    }

    /**
     * @param ageMin
     *            The ageMin to set.
     */
    public void setAgeMin(String ageMin) {
        this.ageMin = ageMin;
    }

    /**
     * @return Returns the allocation.
     */
    public String getAllocation() {
        return getResAdmin(allocation);
    }

    /**
     * @param allocation
     *            The allocation to set.
     */
    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }

    /**
     * @return Returns the assignment.
     */
    public String getAssignment() {
        return getResAdmin(assignment);
    }

    /**
     * @param assignment
     *            The assignment to set.
     */
    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }

    /**
     * @return Returns the collaborators.
     */
    public String getCollaborators() {
        return collaborators;
    }

    /**
     * @param collaborators
     *            The collaborators to set.
     */
    public void setCollaborators(String collaborators) {
        this.collaborators = collaborators;
    }

    /**
     * @return Returns the conditions.
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * @param conditions
     *            The conditions to set.
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * @return Returns the control.
     */
    public String getControl() {
        return getResAdmin(control);
    }

    /**
     * @param control
     *            The control to set.
     */
    public void setControl(String control) {
        this.control = control;
    }

    /**
     * @return Returns the datePlannedEnd.
     */
    public Date getDatePlannedEnd() {
        return datePlannedEnd;
    }

    /**
     * @param datePlannedEnd
     *            The datePlannedEnd to set.
     */
    public void setDatePlannedEnd(Date datePlannedEnd) {
        this.datePlannedEnd = datePlannedEnd;
    }

    /**
     * @return Returns the datePlannedStart.
     */
    public Date getDatePlannedStart() {
        return datePlannedStart;
    }

    /**
     * @param datePlannedStart
     *            The datePlannedStart to set.
     */
    public void setDatePlannedStart(Date datePlannedStart) {
        this.datePlannedStart = datePlannedStart;
    }

    /**
     * @return Returns the duration.
     */
    public String getDuration() {
        return getResAdmin(duration);
    }

    /**
     * @param duration
     *            The duration to set.
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * @return Returns the eligibility.
     */
    public String getEligibility() {
        return eligibility;
    }

    /**
     * @param eligibility
     *            The eligibility to set.
     */
    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    /**
     * @return Returns the endpoint.
     */
    public String getEndpoint() {
        return getResAdmin(endpoint);
    }

    /**
     * @param endpoint
     *            The endpoint to set.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return Returns the expectedTotalEnrollment.
     */
    public int getExpectedTotalEnrollment() {
        return expectedTotalEnrollment;
    }

    /**
     * @param expectedTotalEnrollment
     *            The expectedTotalEnrollment to set.
     */
    public void setExpectedTotalEnrollment(int expectedTotalEnrollment) {
        this.expectedTotalEnrollment = expectedTotalEnrollment;
    }

    /**
     * @return Returns the facilityCity.
     */
    public String getFacilityCity() {
        return facilityCity;
    }

    /**
     * @param facilityCity
     *            The facilityCity to set.
     */
    public void setFacilityCity(String facilityCity) {
        this.facilityCity = facilityCity;
    }

    /**
     * @return Returns the facilityContactDegree.
     */
    public String getFacilityContactDegree() {
        return facilityContactDegree;
    }

    /**
     * @param facilityContactDegree
     *            The facilityContactDegree to set.
     */
    public void setFacilityContactDegree(String facilityContactDegree) {
        this.facilityContactDegree = facilityContactDegree;
    }

    /**
     * @return Returns the facilityContactName.
     */
    public String getFacilityContactName() {
        return facilityContactName;
    }

    /**
     * @param facilityContactName
     *            The facilityContactName to set.
     */
    public void setFacilityContactName(String facilityContactName) {
        this.facilityContactName = facilityContactName;
    }

    /**
     * @return Returns the facilityContactPhone.
     */
    public String getFacilityContactPhone() {
        return facilityContactPhone;
    }

    /**
     * @param facilityContactPhone
     *            The facilityContactPhone to set.
     */
    public void setFacilityContactPhone(String facilityContactPhone) {
        this.facilityContactPhone = facilityContactPhone;
    }

    /**
     * @return Returns the facilityCountry.
     */
    public String getFacilityCountry() {
        return facilityCountry;
    }

    /**
     * @param facilityCountry
     *            The facilityCountry to set.
     */
    public void setFacilityCountry(String facilityCountry) {
        this.facilityCountry = facilityCountry;
    }

    /**
     * @return Returns the facilityName.
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * @param facilityName
     *            The facilityName to set.
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * @return Returns the facilityRecruitmentStatus.
     */
    public String getFacilityRecruitmentStatus() {
        return getResAdmin(facilityRecruitmentStatus);
    }

    /**
     * @param facilityRecruitmentStatus
     *            The facilityRecruitmentStatus to set.
     */
    public void setFacilityRecruitmentStatus(String facilityRecruitmentStatus) {
        this.facilityRecruitmentStatus = facilityRecruitmentStatus;
    }

    /**
     * @return Returns the facilityState.
     */
    public String getFacilityState() {
        return facilityState;
    }

    /**
     * @param facilityState
     *            The facilityState to set.
     */
    public void setFacilityState(String facilityState) {
        this.facilityState = facilityState;
    }

    /**
     * @return Returns the facilityZip.
     */
    public String getFacilityZip() {
        return facilityZip;
    }

    /**
     * @param facilityZip
     *            The facilityZip to set.
     */
    public void setFacilityZip(String facilityZip) {
        this.facilityZip = facilityZip;
    }

    /**
     * @return Returns the gender.
     */
    public String getGender() {
        return getResAdmin(gender);
    }

    /**
     * @param gender
     *            The gender to set.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return Returns the healthyVolunteerAccepted.
     */
    public boolean getHealthyVolunteerAccepted() {
        return healthyVolunteerAccepted;
    }

    /**
     * @param healthyVolunteerAccepted
     *            The healthyVolunteerAccepted to set.
     */
    public void setHealthyVolunteerAccepted(boolean healthyVolunteerAccepted) {
        this.healthyVolunteerAccepted = healthyVolunteerAccepted;
    }

    /**
     * @return Returns the interventions, using the internationalized version of
     *         the intervention type.
     */
    public String getInterventions() {
        StringTokenizer st = new StringTokenizer(interventions, ",");
        StringBuffer sb = new StringBuffer();
        String intervention, name;
        while (st.hasMoreElements()) {
            StringTokenizer inter = new StringTokenizer(st.nextToken().toString(), "/");
            intervention = inter.nextToken();
            sb.append(getResAdmin(intervention));
            sb.append("/");
            name = inter.nextToken();
            sb.append(name);
            sb.append(",");
        }
        if (sb.length() != 0)
            sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    /**
     * @param interventions
     *            The interventions to set.
     */
    public void setInterventions(String interventions) {
        this.interventions = interventions;
    }

    /**
     * @return Returns the keywords.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            The keywords to set.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * @return Returns the masking.
     */
    public String getMasking() {
        return getResAdmin(masking);
    }

    /**
     * @param masking
     *            The masking to set.
     */
    public void setMasking(String masking) {
        this.masking = masking;
    }

    /**
     * @return Returns the medlineIdentifier.
     */
    public String getMedlineIdentifier() {
        return medlineIdentifier;
    }

    /**
     * @param medlineIdentifier
     *            The medlineIdentifier to set.
     */
    public void setMedlineIdentifier(String medlineIdentifier) {
        this.medlineIdentifier = medlineIdentifier;
    }

    /**
     * @return Returns the objective.
     */

    /**
     * @return Returns the parentStudyId.
     */
    public int getParentStudyId() {
        return parentStudyId;
    }

    /**
     * @param parentStudyId
     *            The parentStudyId to set.
     */
    public void setParentStudyId(int parentStudyId) {
        this.parentStudyId = parentStudyId;
    }

    /**
     * @return Returns the parentStudyName
     */
    public String getParentStudyName() {
        return parentStudyName;
    }

    /**
     * @param parentStudyName
     *            String
     */
    public void setParentStudyName(String parentStudyName) {
        this.parentStudyName = parentStudyName;
    }

    /**
     * @return Returns the phase.
     */
    public String getPhase() {
        return getResAdmin(phase);
    }

    /**
     * @param phase
     *            The phase to set.
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * @return Returns the principalInvestigator.
     */
    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    /**
     * @param principalInvestigator
     *            The principalInvestigator to set.
     */
    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    /**
     * @return Returns the protocolDateVerification.
     */
    public Date getProtocolDateVerification() {
        return protocolDateVerification;
    }

    /**
     * @param protocolDateVerification
     *            The protocolDateVerification to set.
     */
    public void setProtocolDateVerification(Date protocolDateVerification) {
        this.protocolDateVerification = protocolDateVerification;
    }

    /**
     * @return Returns the protocolDescription.
     */
    public String getProtocolDescription() {
        return protocolDescription;
    }

    /**
     * @param protocolDescription
     *            The protocolDescription to set.
     */
    public void setProtocolDescription(String protocolDescription) {
        this.protocolDescription = protocolDescription;
    }

    /**
     * @return Returns the purpose.
     */
    public String getPurpose() {
        return getResAdmin(purpose);
    }

    /**
     * @param purpose
     *            The purpose to set.
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * @return Returns the selection.
     */
    public String getSelection() {
        return getResAdmin(selection);
    }

    /**
     * @param selection
     *            The selection to set.
     */
    public void setSelection(String selection) {
        this.selection = selection;
    }

    /**
     * @return Returns the sponsor.
     */
    public String getSponsor() {
        return sponsor;
    }

    /**
     * @param sponsor
     *            The sponsor to set.
     */
    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    /**
     * @deprecated
     * @return Returns the statusId.
     */
    @Deprecated
    public int getStatusId() {
        return status.getId();
    }

    /**
     * @deprecated
     * @param statusId
     *            The statusId to set.
     */
    @Deprecated
    public void setStatusId(int statusId) {
        Status s = Status.get(statusId);
        setStatus(s);
    }

    /**
     * @return Returns the timing.
     */
    public String getTiming() {
        return getResAdmin(timing);
    }

    /**
     * @param timing
     *            The timing to set.
     */
    public void setTiming(String timing) {
        this.timing = timing;
    }

    /**
     * @return Returns the type.
     */
    public String getProtocolType() {
        return getResAdmin(protocolType);
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setProtocolType(String type) {
        this.protocolType = type;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return typeId == TYPE_GENETIC ? TYPE_GENETIC_NAME : TYPE_NON_GENETIC_NAME;
    }

    /**
     * @param type
     *            The type name to set.
     */
    public void setType(String type) {
        setTypeId(TYPE_GENETIC_NAME.equals(type) ? TYPE_GENETIC : TYPE_NON_GENETIC);
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId == TYPE_GENETIC ? TYPE_GENETIC : TYPE_NON_GENETIC;
        this.genetic = this.typeId == TYPE_GENETIC;
    }

    public void setGenetic(boolean genetic) {
        this.genetic = genetic;
        this.typeId = genetic ? TYPE_GENETIC : TYPE_NON_GENETIC;
    }

    /**
     * @return Returns the uRL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            The uRL to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return Returns the uRLDescription.
     */
    public String getUrlDescription() {
        return urlDescription;
    }

    /**
     * @param description
     *            The uRLDescription to set.
     */
    public void setUrlDescription(String description) {
        urlDescription = description;
    }

    /**
     * @return Returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            The identifier to set.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return Returns the secondaryIdentifier.
     */
    public String getSecondaryIdentifier() {
        return secondaryIdentifier;
    }

    /**
     * @param secondaryIdentifier
     *            The secondaryIdentifier to set.
     */
    public void setSecondaryIdentifier(String secondaryIdentifier) {
        this.secondaryIdentifier = secondaryIdentifier;
    }

    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary
     *            The summary to set.
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    private String getResAdmin(String key) {
        try {
            return ResourceBundle.getBundle("org.researchedc.i18n.admin").getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

}
