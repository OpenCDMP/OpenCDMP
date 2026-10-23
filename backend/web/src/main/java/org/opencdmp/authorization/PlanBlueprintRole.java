package org.opencdmp.authorization;

import org.opencdmp.commons.enums.UserPlanBlueprintRole;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Set;

public class PlanBlueprintRole {
	private final Set<UserPlanBlueprintRole> roles;

	@ConstructorBinding
	public PlanBlueprintRole(Set<UserPlanBlueprintRole> roles) {
		this.roles = roles;
	}

	public Set<UserPlanBlueprintRole> getRoles() {
		return this.roles;
	}

}
