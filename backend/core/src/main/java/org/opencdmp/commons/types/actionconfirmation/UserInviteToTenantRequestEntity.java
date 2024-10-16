package org.opencdmp.commons.types.actionconfirmation;

import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name = "user-invite-to-tenant-confirmation")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserInviteToTenantRequestEntity {

    @XmlAttribute(name = "email")
    private String email;

    @XmlAttribute(name = "tenantCode")
    private String tenantCode;

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private List<String> roles;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }


}

