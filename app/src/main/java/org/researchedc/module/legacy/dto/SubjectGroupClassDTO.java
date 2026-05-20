package org.researchedc.module.legacy.dto;

import java.util.Date;
import java.util.List;

import org.researchedc.module.legacy.dto.SubjectGroupDTO;

public class SubjectGroupClassDTO {
    private int groupClassId;
    private String name;
    private int studyId;
    private String groupClassType;
    private String subjectAssignment;
    private List<SubjectGroupDTO> groups;
    private int ownerId;
    private Date dateCreated;

    public int getGroupClassId() { return groupClassId; }
    public void setGroupClassId(int v) { this.groupClassId = v; }
    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public int getStudyId() { return studyId; }
    public void setStudyId(int v) { this.studyId = v; }
    public String getGroupClassType() { return groupClassType; }
    public void setGroupClassType(String v) { this.groupClassType = v; }
    public String getSubjectAssignment() { return subjectAssignment; }
    public void setSubjectAssignment(String v) { this.subjectAssignment = v; }
    public List<SubjectGroupDTO> getGroups() { return groups; }
    public void setGroups(List<SubjectGroupDTO> v) { this.groups = v; }
    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int v) { this.ownerId = v; }
    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date v) { this.dateCreated = v; }
}
