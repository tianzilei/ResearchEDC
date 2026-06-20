package org.researchedc.module.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "audit_user_login")
public class AuditUserLoginEntry {

    private Integer id;
    private String userName;
    private Date loginAttemptDate;
    private Integer loginStatusCode;
    private String details;
    private Integer userAccountId;

    @Id
    @Column(name = "audit_user_login_id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "user_name")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "login_attempt_date")
    public Date getLoginAttemptDate() {
        return loginAttemptDate;
    }

    public void setLoginAttemptDate(Date loginAttemptDate) {
        this.loginAttemptDate = loginAttemptDate;
    }

    @Column(name = "login_status_code")
    public Integer getLoginStatusCode() {
        return loginStatusCode;
    }

    public void setLoginStatusCode(Integer loginStatusCode) {
        this.loginStatusCode = loginStatusCode;
    }

    @Column(name = "details")
    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Column(name = "user_id")
    public Integer getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Integer userAccountId) {
        this.userAccountId = userAccountId;
    }

    public AuditLoginStatus loginStatus() {
        return AuditLoginStatus.fromCode(loginStatusCode);
    }
}
