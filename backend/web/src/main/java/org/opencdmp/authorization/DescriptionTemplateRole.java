package org.opencdmp.authorization;

import org.opencdmp.commons.enums.UserDescriptionTemplateRole;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Set;

public class DescriptionTemplateRole {
	private final Set<UserDescriptionTemplateRole> roles;

	@ConstructorBinding
	public DescriptionTemplateRole(Set<UserDescriptionTemplateRole> roles) {
		this.roles = roles;
	}

	public Set<UserDescriptionTemplateRole> getRoles() {
		return this.roles;
	}

}
