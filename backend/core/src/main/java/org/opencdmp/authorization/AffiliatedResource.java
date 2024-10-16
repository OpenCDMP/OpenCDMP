package org.opencdmp.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationResource;
import org.opencdmp.commons.enums.PlanUserRole;
import org.opencdmp.commons.enums.UserDescriptionTemplateRole;

import java.util.HashSet;

public class AffiliatedResource extends AuthorizationResource {
	private HashSet<PlanUserRole> planUserRoles;
	private HashSet<UserDescriptionTemplateRole> userDescriptionTemplateRoles;

	public AffiliatedResource() {
		this.planUserRoles = new HashSet<>();
		this.userDescriptionTemplateRoles = new HashSet<>();
	}

	public HashSet<PlanUserRole> getPlanUserRoles() {
		return this.planUserRoles;
	}

	public void setPlanUserRoles(HashSet<PlanUserRole> planUserRoles) {
		this.planUserRoles = planUserRoles;
	}

	public HashSet<UserDescriptionTemplateRole> getUserDescriptionTemplateRoles() {
		return this.userDescriptionTemplateRoles;
	}

	public void setUserDescriptionTemplateRoles(HashSet<UserDescriptionTemplateRole> userDescriptionTemplateRoles) {
		this.userDescriptionTemplateRoles = userDescriptionTemplateRoles;
	}
}
