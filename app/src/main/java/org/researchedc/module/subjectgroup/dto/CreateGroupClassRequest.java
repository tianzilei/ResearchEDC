package org.researchedc.module.subjectgroup.dto;

public class CreateGroupClassRequest {
    private String name;
    private Integer studyId;
    private String subjectAssignment;

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public String getSubjectAssignment() { return subjectAssignment; }
    public void setSubjectAssignment(String v) { this.subjectAssignment = v; }
}
