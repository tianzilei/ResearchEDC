package org.researchedc.module.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "ModuleUserAccount")
@Table(name = "user_account")
public class UserAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_account_user_id_seq", allocationSize = 1)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name", length = 40)
    private String userName;

    @Column(name = "first_name", length = 60)
    private String firstName;

    @Column(name = "last_name", length = 60)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "institutional_affiliation", length = 255)
    private String institutionalAffiliation;

    @Column(name = "user_type_id")
    private Integer userTypeId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "active_study")
    private Integer activeStudyId;

    @Column
    private Boolean enabled;

    @Column(name = "account_non_locked")
    private Boolean accountNonLocked;

    @Column(name = "passwd", length = 255)
    private String passwordHash;

    @Column(name = "date_created")
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "owner_id")
    private Integer ownerId;

    @Column(name = "update_id")
    private Integer updateId;

    public Integer getUserId() { return userId; }
    public void setUserId(Integer v) { this.userId = v; }
    public String getUserName() { return userName; }
    public void setUserName(String v) { this.userName = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getInstitutionalAffiliation() { return institutionalAffiliation; }
    public void setInstitutionalAffiliation(String v) { this.institutionalAffiliation = v; }
    public Integer getUserTypeId() { return userTypeId; }
    public void setUserTypeId(Integer v) { this.userTypeId = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
    public Integer getActiveStudyId() { return activeStudyId; }
    public void setActiveStudyId(Integer v) { this.activeStudyId = v; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean v) { this.enabled = v; }
    public Boolean getAccountNonLocked() { return accountNonLocked; }
    public void setAccountNonLocked(Boolean v) { this.accountNonLocked = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime v) { this.dateCreated = v; }
    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime v) { this.dateUpdated = v; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer v) { this.ownerId = v; }
    public Integer getUpdateId() { return updateId; }
    public void setUpdateId(Integer v) { this.updateId = v; }
}
