package org.researchedc.module.dataset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleDataset")
@Table(name = "module_dataset")
public class DatasetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_dataset_seq")
    @SequenceGenerator(name = "module_dataset_seq", sequenceName = "module_dataset_id_seq", allocationSize = 1)
    @Column(name = "dataset_id")
    private Integer datasetId;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "sql_statement", length = 4000)
    private String sqlStatement;

    @Column(name = "num_runs")
    private Integer numRuns;

    @Column(name = "date_start")
    private LocalDateTime dateStart;

    @Column(name = "date_end")
    private LocalDateTime dateEnd;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "date_last_run")
    private LocalDateTime dateLastRun;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "approver_id")
    private Integer approverId;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "show_event_location")
    private Boolean showEventLocation;

    @Column(name = "show_event_start")
    private Boolean showEventStart;

    @Column(name = "show_event_end")
    private Boolean showEventEnd;

    @Column(name = "show_subject_dob")
    private Boolean showSubjectDob;

    @Column(name = "show_subject_gender")
    private Boolean showSubjectGender;

    @Column(name = "show_event_status")
    private Boolean showEventStatus;

    @Column(name = "show_subject_status")
    private Boolean showSubjectStatus;

    @Column(name = "show_subject_unique_id")
    private Boolean showSubjectUniqueId;

    @Column(name = "show_subject_age_at_event")
    private Boolean showSubjectAgeAtEvent;

    @Column(name = "show_crf_status")
    private Boolean showCrfStatus;

    @Column(name = "show_crf_version")
    private Boolean showCrfVersion;

    @Column(name = "show_crf_int_name")
    private Boolean showCrfIntName;

    @Column(name = "show_crf_int_date")
    private Boolean showCrfIntDate;

    @Column(name = "show_group_info")
    private Boolean showGroupInfo;

    @Column(name = "show_disc_info")
    private Boolean showDiscInfo;

    @Column(name = "odm_meta_data_version_name", length = 255)
    private String odmMetaDataVersionName;

    @Column(name = "odm_meta_data_version_oid", length = 255)
    private String odmMetaDataVersionOid;

    @Column(name = "odm_prior_study_oid", length = 255)
    private String odmPriorStudyOid;

    @Column(name = "odm_prior_meta_data_version_oid", length = 255)
    private String odmPriorMetaDataVersionOid;

    @Column(name = "show_secondary_id")
    private Boolean showSecondaryId;

    @Column(name = "dataset_item_status_id")
    private Integer datasetItemStatusId;

    public Integer getDatasetId() { return datasetId; }
    public void setDatasetId(Integer v) { this.datasetId = v; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }

    public String getSqlStatement() { return sqlStatement; }
    public void setSqlStatement(String v) { this.sqlStatement = v; }

    public Integer getNumRuns() { return numRuns; }
    public void setNumRuns(Integer v) { this.numRuns = v; }

    public LocalDateTime getDateStart() { return dateStart; }
    public void setDateStart(LocalDateTime v) { this.dateStart = v; }

    public LocalDateTime getDateEnd() { return dateEnd; }
    public void setDateEnd(LocalDateTime v) { this.dateEnd = v; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }

    public LocalDateTime getDateLastRun() { return dateLastRun; }
    public void setDateLastRun(LocalDateTime v) { this.dateLastRun = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public Integer getApproverId() { return approverId; }
    public void setApproverId(Integer v) { this.approverId = v; }

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }

    public Boolean getShowEventLocation() { return showEventLocation; }
    public void setShowEventLocation(Boolean v) { this.showEventLocation = v; }

    public Boolean getShowEventStart() { return showEventStart; }
    public void setShowEventStart(Boolean v) { this.showEventStart = v; }

    public Boolean getShowEventEnd() { return showEventEnd; }
    public void setShowEventEnd(Boolean v) { this.showEventEnd = v; }

    public Boolean getShowSubjectDob() { return showSubjectDob; }
    public void setShowSubjectDob(Boolean v) { this.showSubjectDob = v; }

    public Boolean getShowSubjectGender() { return showSubjectGender; }
    public void setShowSubjectGender(Boolean v) { this.showSubjectGender = v; }

    public Boolean getShowEventStatus() { return showEventStatus; }
    public void setShowEventStatus(Boolean v) { this.showEventStatus = v; }

    public Boolean getShowSubjectStatus() { return showSubjectStatus; }
    public void setShowSubjectStatus(Boolean v) { this.showSubjectStatus = v; }

    public Boolean getShowSubjectUniqueId() { return showSubjectUniqueId; }
    public void setShowSubjectUniqueId(Boolean v) { this.showSubjectUniqueId = v; }

    public Boolean getShowSubjectAgeAtEvent() { return showSubjectAgeAtEvent; }
    public void setShowSubjectAgeAtEvent(Boolean v) { this.showSubjectAgeAtEvent = v; }

    public Boolean getShowCrfStatus() { return showCrfStatus; }
    public void setShowCrfStatus(Boolean v) { this.showCrfStatus = v; }

    public Boolean getShowCrfVersion() { return showCrfVersion; }
    public void setShowCrfVersion(Boolean v) { this.showCrfVersion = v; }

    public Boolean getShowCrfIntName() { return showCrfIntName; }
    public void setShowCrfIntName(Boolean v) { this.showCrfIntName = v; }

    public Boolean getShowCrfIntDate() { return showCrfIntDate; }
    public void setShowCrfIntDate(Boolean v) { this.showCrfIntDate = v; }

    public Boolean getShowGroupInfo() { return showGroupInfo; }
    public void setShowGroupInfo(Boolean v) { this.showGroupInfo = v; }

    public Boolean getShowDiscInfo() { return showDiscInfo; }
    public void setShowDiscInfo(Boolean v) { this.showDiscInfo = v; }

    public String getOdmMetaDataVersionName() { return odmMetaDataVersionName; }
    public void setOdmMetaDataVersionName(String v) { this.odmMetaDataVersionName = v; }

    public String getOdmMetaDataVersionOid() { return odmMetaDataVersionOid; }
    public void setOdmMetaDataVersionOid(String v) { this.odmMetaDataVersionOid = v; }

    public String getOdmPriorStudyOid() { return odmPriorStudyOid; }
    public void setOdmPriorStudyOid(String v) { this.odmPriorStudyOid = v; }

    public String getOdmPriorMetaDataVersionOid() { return odmPriorMetaDataVersionOid; }
    public void setOdmPriorMetaDataVersionOid(String v) { this.odmPriorMetaDataVersionOid = v; }

    public Boolean getShowSecondaryId() { return showSecondaryId; }
    public void setShowSecondaryId(Boolean v) { this.showSecondaryId = v; }

    public Integer getDatasetItemStatusId() { return datasetItemStatusId; }
    public void setDatasetItemStatusId(Integer v) { this.datasetItemStatusId = v; }
}
