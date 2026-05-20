package org.researchedc.module.subject.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleStudySubject")
@Table(name = "study_subject")
public class StudySubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ss_seq")
    @SequenceGenerator(name = "ss_seq", sequenceName = "study_subject_study_subject_id_seq", allocationSize = 1)
    @Column(name = "study_subject_id")
    private Integer studySubjectId;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(length = 30)
    private String label;

    @Column(name = "secondary_label", length = 30)
    private String secondaryLabel;

    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;

    @Column(name = "oc_oid", length = 40)
    private String ocOid;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "status_id")
    private Integer statusId;

    public Integer getStudySubjectId() { return studySubjectId; }
    public void setStudySubjectId(Integer v) { this.studySubjectId = v; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer v) { this.subjectId = v; }

    public String getLabel() { return label; }
    public void setLabel(String v) { this.label = v; }

    public String getSecondaryLabel() { return secondaryLabel; }
    public void setSecondaryLabel(String v) { this.secondaryLabel = v; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime v) { this.enrollmentDate = v; }

    public String getOcOid() { return ocOid; }
    public void setOcOid(String v) { this.ocOid = v; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
}
