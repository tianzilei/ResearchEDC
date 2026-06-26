package org.researchedc.module.subject.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleSubject")
@Table(name = "module_subject")
public class SubjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_subject_seq")
    @SequenceGenerator(name = "module_subject_seq", sequenceName = "module_subject_id_seq", allocationSize = 1)
    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "unique_identifier", length = 30)
    private String uniqueIdentifier;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(length = 1)
    private String gender;

    @Column(name = "dob_collected")
    private Boolean dobCollected;

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

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer v) { this.subjectId = v; }

    public String getUniqueIdentifier() { return uniqueIdentifier; }
    public void setUniqueIdentifier(String v) { this.uniqueIdentifier = v; }

    public LocalDateTime getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDateTime v) { this.dateOfBirth = v; }

    public String getGender() { return gender; }
    public void setGender(String v) { this.gender = v; }

    public Boolean getDobCollected() { return dobCollected; }
    public void setDobCollected(Boolean v) { this.dobCollected = v; }

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
