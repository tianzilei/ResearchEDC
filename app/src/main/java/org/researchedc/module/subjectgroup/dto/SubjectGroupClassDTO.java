package org.researchedc.module.subjectgroup.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SubjectGroupClassDTO {
    private Integer groupClassId;
    private String name;
    private Integer studyId;
    private String subjectAssignment;
    private Integer ownerId;
    private LocalDateTime dateCreated;
    private List<SubjectGroupDTO> groups;

    public Integer getGroupClassId() { return groupClassId; }
    public void setGroupClassId(Integer v) { this.groupClassId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public String getSubjectAssignment() { return subjectAssignment; }
    public void setSubjectAssignment(String v) { this.subjectAssignment = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public List<SubjectGroupDTO> getGroups() { return groups; }
    public void setGroups(List<SubjectGroupDTO> v) { this.groups = v; }
}
