package org.opencdmp.service.keycloak;

public class KeycloakTenantAuthorityProperties {

    private String parent;
    private String roleAttributeValueStrategy;

    public KeycloakTenantAuthorityProperties() {
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getRoleAttributeValueStrategy() {
        return roleAttributeValueStrategy;
    }

    public void setRoleAttributeValueStrategy(String roleAttributeValueStrategy) {
        this.roleAttributeValueStrategy = roleAttributeValueStrategy;
    }
}
