package org.opencdmp.model.actionconfirmation;

import java.util.List;

public class UserInviteToTenantRequest {

    private String email;
    public static final String _email = "email";

    private String tenantCode;
    public static final String _tenantCode = "tenantCode";

    private List<String> roles;
    public static final String _roles = "roles";

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

