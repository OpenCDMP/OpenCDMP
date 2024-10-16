package org.opencdmp.authorization;

import gr.cite.commons.web.authz.handler.AuthorizationHandler;
import gr.cite.commons.web.authz.handler.AuthorizationHandlerContext;
import gr.cite.commons.web.authz.policy.AuthorizationRequirement;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component("affiliatedAuthorizationHandler")
public class AffiliatedAuthorizationHandler extends AuthorizationHandler<AffiliatedAuthorizationRequirement> {

	private final CustomPermissionAttributesConfiguration myConfiguration;

	@Autowired
	public AffiliatedAuthorizationHandler(CustomPermissionAttributesConfiguration myConfiguration) {
		this.myConfiguration = myConfiguration;
	}

	@Override
	public int handleRequirement(AuthorizationHandlerContext context, Object resource, AuthorizationRequirement requirement) {
		AffiliatedAuthorizationRequirement req = (AffiliatedAuthorizationRequirement) requirement;
		if (req.getRequiredPermissions() == null)
			return ACCESS_NOT_DETERMINED;

		AffiliatedResource rs = (AffiliatedResource) resource;

		boolean isAuthenticated = ((MyPrincipal) context.getPrincipal()).isAuthenticated();
		if (!isAuthenticated)
			return ACCESS_NOT_DETERMINED;

		if (this.myConfiguration.getMyPolicies() == null)
			return ACCESS_NOT_DETERMINED;

		int hits = 0;
		HashSet<PlanUserRole> planUserRoles = rs != null && rs.getPlanUserRoles() != null ? rs.getPlanUserRoles() : null;
		HashSet<UserDescriptionTemplateRole> userDescriptionTemplateRoles = rs != null && rs.getUserDescriptionTemplateRoles() != null ? rs.getUserDescriptionTemplateRoles() : null;

		for (String permission : req.getRequiredPermissions()) {
			CustomPermissionAttributesProperties.MyPermission policy = this.myConfiguration.getMyPolicies().get(permission);
			boolean hasPlanPermission = policy != null && this.hasPermission(policy.getPlan(), planUserRoles);
			boolean hasDescriptionTemplatePermission = policy != null && this.hasPermission(policy.getDescriptionTemplate(), userDescriptionTemplateRoles);
			if (hasPlanPermission || hasDescriptionTemplatePermission) hits += 1;
		}
		if ((req.getMatchAll() && req.getRequiredPermissions().size() == hits) || (!req.getMatchAll() && hits > 0))
			return ACCESS_GRANTED;

		return ACCESS_NOT_DETERMINED;
	}

	private Boolean hasPermission(DescriptionTemplateRole descriptionTemplateRole, HashSet<UserDescriptionTemplateRole> roles) {
		if (roles == null)
			return Boolean.FALSE;
		if (descriptionTemplateRole == null || descriptionTemplateRole.getRoles() == null)
			return Boolean.FALSE;
		for (UserDescriptionTemplateRole role : descriptionTemplateRole.getRoles()) {
			if (roles.contains(role))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	private Boolean hasPermission(PlanRole planRole, HashSet<PlanUserRole> roles) {
		if (roles == null)
			return Boolean.FALSE;
		if (planRole == null || planRole.getRoles() == null)
			return Boolean.FALSE;
		for (PlanUserRole role : planRole.getRoles()) {
			if (roles.contains(role))
				return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public Class<? extends AuthorizationRequirement> supporting() {
		return AffiliatedAuthorizationRequirement.class;
	}

}
