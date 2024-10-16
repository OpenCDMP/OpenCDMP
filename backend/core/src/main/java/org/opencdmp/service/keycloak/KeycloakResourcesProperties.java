package org.opencdmp.service.keycloak;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.HashMap;

@ConfigurationProperties(prefix = "keycloak-resources")
@ConditionalOnProperty(prefix = "keycloak-resources", name = "enabled", havingValue = "true")
public class KeycloakResourcesProperties {

    private HashMap<String, KeycloakAuthorityProperties> authorities;
    private HashMap<String, KeycloakTenantAuthorityProperties> tenantAuthorities;
    
    private String tenantGroupsNamingStrategy;
    private String tenantRoleAttributeName;

    public HashMap<String, KeycloakAuthorityProperties> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(HashMap<String, KeycloakAuthorityProperties> authorities) {
        this.authorities = authorities;
    }

    public HashMap<String, KeycloakTenantAuthorityProperties> getTenantAuthorities() {
        return tenantAuthorities;
    }

    public void setTenantAuthorities(HashMap<String, KeycloakTenantAuthorityProperties> tenantAuthorities) {
        this.tenantAuthorities = tenantAuthorities;
    }

    public String getTenantGroupsNamingStrategy() {
        return tenantGroupsNamingStrategy;
    }

    public void setTenantGroupsNamingStrategy(String tenantGroupsNamingStrategy) {
        this.tenantGroupsNamingStrategy = tenantGroupsNamingStrategy;
    }

    public String getTenantRoleAttributeName() {
        return tenantRoleAttributeName;
    }

    public void setTenantRoleAttributeName(String tenantRoleAttributeName) {
        this.tenantRoleAttributeName = tenantRoleAttributeName;
    }
}
