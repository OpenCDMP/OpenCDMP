package org.opencdmp.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
@EnableConfigurationProperties(CustomPermissionAttributesProperties.class)
public class CustomPermissionAttributesConfiguration {

    private final CustomPermissionAttributesProperties properties;

    @Autowired
    public CustomPermissionAttributesConfiguration(CustomPermissionAttributesProperties properties) {
        this.properties = properties;
    }

    public HashMap<String, CustomPermissionAttributesProperties.MyPermission> getMyPolicies() {
        return properties.getPolicies();
    }

}
