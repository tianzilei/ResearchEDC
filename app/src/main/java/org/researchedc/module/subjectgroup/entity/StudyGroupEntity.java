package org.researchedc.module.subjectgroup.entity;

import jakarta.persistence.*;

@Entity(name = "ModuleStudyGroup")
@Table(name = "module_study_group")
public class StudyGroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_study_group_seq")
    @SequenceGenerator(name = "module_study_group_seq", sequenceName = "module_study_group_id_seq", allocationSize = 1)
    @Column(name = "study_group_id")
    private Integer studyGroupId;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "study_group_class_id")
    private Integer studyGroupClassId;

    public Integer getStudyGroupId() { return studyGroupId; }
    public void setStudyGroupId(Integer v) { this.studyGroupId = v; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }

    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }

    public Integer getStudyGroupClassId() { return studyGroupClassId; }
    public void setStudyGroupClassId(Integer v) { this.studyGroupClassId = v; }
}
