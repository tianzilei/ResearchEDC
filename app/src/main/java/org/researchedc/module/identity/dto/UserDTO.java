package org.researchedc.module.identity.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Integer userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String phone;
    private String institutionalAffiliation;
    private String userType;
    private Boolean enabled;
    private Integer activeStudyId;
    private LocalDateTime dateCreated;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer v) { this.userId = v; }
    public String getUserName() { return userName; }
    public void setUserName(String v) { this.userName = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getInstitutionalAffiliation() { return institutionalAffiliation; }
    public void setInstitutionalAffiliation(String v) { this.institutionalAffiliation = v; }
    public String getUserType() { return userType; }
    public void setUserType(String v) { this.userType = v; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean v) { this.enabled = v; }
    public Integer getActiveStudyId() { return activeStudyId; }
    public void setActiveStudyId(Integer v) { this.activeStudyId = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
}
