package org.researchedc.module.subjectgroup.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleStudyGroupClass")
@Table(name = "module_study_group_class")
public class StudyGroupClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_study_group_class_seq")
    @SequenceGenerator(name = "module_study_group_class_seq", sequenceName = "module_study_group_class_id_seq", allocationSize = 1)
    @Column(name = "study_group_class_id")
    private Integer studyGroupClassId;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "update_id")
    private Integer updateId;

    @Column(name = "group_class_type_id")
    private Integer groupClassTypeId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "subject_assignment", length = 30)
    private String subjectAssignment;

    public Integer getStudyGroupClassId() { return studyGroupClassId; }
    public void setStudyGroupClassId(Integer v) { this.studyGroupClassId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }

    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }

    public Integer getGroupClassTypeId() { return groupClassTypeId; }
    public void setGroupClassTypeId(Integer v) { this.groupClassTypeId = v; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }

    public String getSubjectAssignment() { return subjectAssignment; }
    public void setSubjectAssignment(String v) { this.subjectAssignment = v; }
}
