package org.researchedc.module.identity.entity;

import jakarta.persistence.*;

@Entity(name = "ModuleStudyUserRole")
@Table(name = "module_study_user_role")
public class RoleEntity {

    @Id
    @Column(name = "study_user_role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyUserRoleId;

    @Column(name = "role_name", length = 40)
    private String roleName;

    @Column(name = "study_id")
    private Integer studyId;

    @Column(name = "user_name", length = 40)
    private String userName;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "owner_id")
    private Integer ownerId;

    public Long getStudyUserRoleId() { return studyUserRoleId; }
    public void setStudyUserRoleId(Long v) { this.studyUserRoleId = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { this.roleName = v; }
    public Integer getStudyId() { return studyId; }
    public void setStudyId(Integer v) { this.studyId = v; }
    public String getUserName() { return userName; }
    public void setUserName(String v) { this.userName = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
}
