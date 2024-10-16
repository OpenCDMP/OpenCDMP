package org.opencdmp.service.keycloak;

import org.jetbrains.annotations.NotNull;
import org.keycloak.representations.idm.GroupRepresentation;
import org.opencdmp.convention.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    private final MyKeycloakAdminRestApi api;
    private final KeycloakResourcesConfiguration configuration;
    private final ConventionService conventionService;

    @Autowired
    public KeycloakServiceImpl(MyKeycloakAdminRestApi api, KeycloakResourcesConfiguration configuration, ConventionService conventionService) {
        this.api = api;
        this.configuration = configuration;
	    this.conventionService = conventionService;
    }

    @Override
    public void addUserToGroup(@NotNull String subjectId, String groupId) {
	    this.api.users().addUserToGroup(subjectId, groupId);
    }

    @Override
    public void removeUserFromGroup(@NotNull String subjectId, String groupId) {
	    this.api.users().removeUserFromGroup(subjectId, groupId);
    }
    
    @Override
    public List<String> getUserGroups(String subjectId) {
        if (this.configuration.getProperties().getAuthorities() == null) return new ArrayList<>();
        List<GroupRepresentation> group = this.api.users().getGroups(subjectId);
        if (group != null) return group.stream().map(GroupRepresentation::getId).toList();
        return new ArrayList<>();
    }

    @Override
    public void removeFromAllGroups(String subjectId){
        List<String> existingGroups = this.getUserGroups(subjectId);
        for (String existingGroup : existingGroups){
            this.removeUserFromGroup(subjectId, existingGroup);
        }
    }
    
    @Override
    public void addUserToGlobalRoleGroup(String subjectId, String role) {
        if (this.configuration.getProperties().getAuthorities() == null) return;
        KeycloakAuthorityProperties properties = this.configuration.getProperties().getAuthorities().getOrDefault(role, null);
        if (properties != null) this.addUserToGroup(subjectId, properties.getGroupId());
    }

    @Override
    public void removeUserGlobalRoleGroup(@NotNull String subjectId, String role) {
        if (this.configuration.getProperties().getAuthorities() == null) return;
        KeycloakAuthorityProperties properties = this.configuration.getProperties().getAuthorities().getOrDefault(role, null);
        if (properties != null) this.removeUserFromGroup(subjectId, properties.getGroupId());
    }

    @Override
    public void addUserToTenantRoleGroup(String subjectId, String tenantCode, String tenantRole) {
        if (this.configuration.getProperties().getAuthorities() == null) return;
        KeycloakTenantAuthorityProperties properties = this.configuration.getProperties().getTenantAuthorities().getOrDefault(tenantRole, null);
        if (properties == null) return;
        GroupRepresentation group = this.api.groups().findGroupByPath(this.getTenantAuthorityParentPath(properties) + "/" + this.configuration.getTenantGroupName(tenantCode));
        if (group != null) this.addUserToGroup(subjectId, group.getId());
    }

    @Override
    public void removeUserTenantRoleGroup(String subjectId, String tenantCode, String tenantRole) {
        KeycloakTenantAuthorityProperties properties = this.configuration.getProperties().getTenantAuthorities().getOrDefault(tenantRole, null);
        if (properties == null) return;
        GroupRepresentation group = this.api.groups().findGroupByPath(this.getTenantAuthorityParentPath(properties) + "/" + this.configuration.getTenantGroupName(tenantCode));
        if (group != null) this.removeUserFromGroup(subjectId, group.getId());
    }
    
    private String getTenantAuthorityParentPath(KeycloakTenantAuthorityProperties keycloakTenantAuthorityProperties) {
        GroupRepresentation parent = this.api.groups().findGroupById(keycloakTenantAuthorityProperties.getParent());
        return parent.getPath();
    }

    @Override
    public void createTenantGroups(String tenantCode) {
        if (this.configuration.getProperties().getTenantAuthorities() == null) return;
        for (Map.Entry<String,KeycloakTenantAuthorityProperties> entry : this.configuration.getProperties().getTenantAuthorities().entrySet()){
            GroupRepresentation group = new GroupRepresentation();
            group.setName(this.configuration.getTenantGroupName(tenantCode));
            HashMap<String, List<String>> user_attributes = new HashMap<>();
            if (!this.conventionService.isNullOrEmpty(this.configuration.getProperties().getTenantRoleAttributeName())) user_attributes.put(this.configuration.getProperties().getTenantRoleAttributeName(), List.of(this.configuration.getTenantRoleAttributeValue(tenantCode, entry.getValue())));
            group.setAttributes(user_attributes);
	        this.api.groups().addGroupWithParent(group, entry.getValue().getParent());
        }
    }
}
