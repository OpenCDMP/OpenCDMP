package org.opencdmp.authorization;

import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.event.DescriptionStatusTouchedEvent;
import org.opencdmp.event.PlanStatusTouchedEvent;
import org.opencdmp.service.custompolicy.CustomPolicyCacheService;
import org.opencdmp.service.custompolicy.CustomPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(CustomPermissionAttributesProperties.class)
public class CustomPermissionAttributesConfiguration {

    private final CustomPermissionAttributesProperties properties;
    private HashMap<String, CustomPermissionAttributesProperties.MyPermission> myPolicies;
    private final CustomPolicyService customPolicyService;
    private final CustomPolicyCacheService customPolicyCacheService;

    @Autowired
    public CustomPermissionAttributesConfiguration(CustomPermissionAttributesProperties properties, CustomPolicyService customPolicyService, CustomPolicyCacheService customPolicyCacheService) {
        this.properties = properties;
        this.customPolicyService = customPolicyService;
        this.customPolicyCacheService = customPolicyCacheService;
    }

    @EventListener
    public void handlePlanTouchedEvent(PlanStatusTouchedEvent event) {
        this.customPolicyCacheService.clearCache(event);
        this.refresh(true);
    }

    @EventListener
    public void handleDescriptionStatusTouchedEvent(DescriptionStatusTouchedEvent event) {
        this.customPolicyCacheService.clearCache(event);
        this.refresh(true);
    }

    public HashMap<String, CustomPermissionAttributesProperties.MyPermission> getMyPolicies() {
        if (this.myPolicies == null) this.refresh(false);

        return properties.getPolicies();
    }

    public void refresh(boolean force) {
        if (!force && this.myPolicies != null) return;
        this.myPolicies = this.properties.getPolicies();
        this.myPolicies.putAll(this.buildAffiliatedCustomPermissions());
    }

    private HashMap<String, CustomPermissionAttributesProperties.MyPermission> buildAffiliatedCustomPermissions() {
        HashMap<String, CustomPermissionAttributesProperties.MyPermission> affiliatedCustomPermissions = new HashMap<>();
        this.buildAffiliatedPlanCustomPermissions(affiliatedCustomPermissions);
        this.buildAffiliatedDescriptionCustomPermissions(affiliatedCustomPermissions);

        return affiliatedCustomPermissions;
    }

    private void buildAffiliatedPlanCustomPermissions(HashMap<String, CustomPermissionAttributesProperties.MyPermission> affiliatedCustomPermissions) {
        Map<UUID, PlanStatusDefinitionEntity> map = this.customPolicyService.buildPlanStatusDefinitionMap();
        if (map == null) return;

        for (UUID statusId: map.keySet()) {
            PlanStatusDefinitionEntity definition = map.get(statusId);
            if (definition != null && definition.getAuthorization() != null && definition.getAuthorization().getEdit() != null) {
                CustomPermissionAttributesProperties.MyPermission myPermission = new CustomPermissionAttributesProperties.MyPermission(new PlanRole(new HashSet<>(definition.getAuthorization().getEdit().getPlanRoles())), null);
                affiliatedCustomPermissions.put(this.customPolicyService.getPlanStatusCanEditStatusPermission(statusId), myPermission);
            }
        }

    }

    private void buildAffiliatedDescriptionCustomPermissions(HashMap<String, CustomPermissionAttributesProperties.MyPermission> affiliatedCustomPermissions) {
        Map<UUID, DescriptionStatusDefinitionEntity> map = this.customPolicyService.buildDescriptionStatusDefinitionMap();
        if (map == null) return;

        for (UUID statusId: map.keySet()) {
            DescriptionStatusDefinitionEntity definition = map.get(statusId);
            if (definition != null && definition.getAuthorization() != null && definition.getAuthorization().getEdit() != null) {
                CustomPermissionAttributesProperties.MyPermission myPermission = new CustomPermissionAttributesProperties.MyPermission(new PlanRole(new HashSet<>(definition.getAuthorization().getEdit().getPlanRoles())), null);
                affiliatedCustomPermissions.put(this.customPolicyService.getDescriptionStatusCanEditStatusPermission(statusId), myPermission);
            }
        }
    }

}
