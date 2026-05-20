package org.researchedc.module.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank
    @Size(max = 40)
    private String userName;

    @Size(max = 60)
    private String firstName;

    @Size(max = 60)
    private String lastName;

    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String phone;

    @Size(max = 255)
    private String institutionalAffiliation;

    @Size(max = 20)
    private String userType;

    private Integer statusId;

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
    public String getUserType() { return userType; }
    public void setUserType(String v) { this.userType = v; }
    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer v) { this.statusId = v; }
}
