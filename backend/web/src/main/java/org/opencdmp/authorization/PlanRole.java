package org.opencdmp.authorization;


import org.opencdmp.commons.enums.PlanUserRole;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Set;

public class PlanRole {
	private final Set<PlanUserRole> roles;

	@ConstructorBinding
	public PlanRole(Set<PlanUserRole> roles) {
		this.roles = roles;
	}

	public Set<PlanUserRole> getRoles() {
		return this.roles;
	}

}
