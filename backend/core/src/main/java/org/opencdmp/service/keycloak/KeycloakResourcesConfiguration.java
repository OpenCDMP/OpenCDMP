package org.opencdmp.service.keycloak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakResourcesProperties.class)
public class KeycloakResourcesConfiguration {

    private final KeycloakResourcesProperties properties;

    @Autowired
    public KeycloakResourcesConfiguration(KeycloakResourcesProperties properties) {
        this.properties = properties;
    }

    public KeycloakResourcesProperties getProperties() {
        return this.properties;
    }

    public String getTenantGroupName(String tenantCode) {
        return this.properties.getTenantGroupsNamingStrategy()
                .replace("{tenantCode}", tenantCode);
    }

    public String getTenantRoleAttributeValue(String tenantCode, KeycloakTenantAuthorityProperties properties) {
        return properties.getRoleAttributeValueStrategy()
                .replace("{tenantCode}", tenantCode);
    }

}
