package org.researchedc.module.identity.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Size(max = 60)
    private String firstName;

    @Size(max = 60)
    private String lastName;

    @Size(max = 255)
    private String phone;

    @Size(max = 255)
    private String institution;

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getInstitution() { return institution; }
    public void setInstitution(String v) { this.institution = v; }
}
