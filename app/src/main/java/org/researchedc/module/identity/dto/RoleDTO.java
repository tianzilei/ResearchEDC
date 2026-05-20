package org.researchedc.module.identity.dto;

public class RoleDTO {
    private Long studyUserRoleId;
    private String roleName;
    private String userName;
    private Integer studyId;
    private Integer statusId;

    public Long getStudyUserRoleId() { return studyUserRoleId; }
    public void setStudyUserRoleId(Long v) { this.studyUserRoleId = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
    public String getUserName() { return userName; }
    public void setUserName(String v) { this.userName = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
}
