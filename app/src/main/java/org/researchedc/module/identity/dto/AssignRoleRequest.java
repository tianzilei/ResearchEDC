package org.researchedc.module.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssignRoleRequest {

    @NotBlank
    private String userName;

    @NotNull
    private Integer studyId;

    @NotBlank
    private String roleName;

    private Integer statusId;

    public String getUserName() { return userName; }
    public void setUserName(String v) { this.userName = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
}
