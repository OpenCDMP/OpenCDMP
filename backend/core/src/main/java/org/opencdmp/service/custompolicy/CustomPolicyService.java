package org.opencdmp.service.custompolicy;


import gr.cite.commons.web.authz.configuration.Permission;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface CustomPolicyService {

    HashMap<String, Permission> buildPlanStatusPolicies();

    Map<UUID, PlanStatusDefinitionEntity> buildPlanStatusDefinitionMap();

    Map<UUID, DescriptionStatusDefinitionEntity> buildDescriptionStatusDefinitionMap();

    String getPlanStatusCanEditStatusPermission(UUID id);

    HashMap<String, Permission> buildDescriptionStatusPolicies();

    String getDescriptionStatusCanEditStatusPermission(UUID id);

}
