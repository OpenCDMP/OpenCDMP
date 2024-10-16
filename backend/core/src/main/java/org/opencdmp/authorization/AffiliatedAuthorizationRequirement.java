package org.opencdmp.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationRequirement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AffiliatedAuthorizationRequirement implements AuthorizationRequirement {
	private final Set<String> requiredPermissions;
	private final boolean matchAll;

	public AffiliatedAuthorizationRequirement(Set<String> requiredPermissions) {
		this(false, requiredPermissions);
	}

	public AffiliatedAuthorizationRequirement(String... requiredPermissions) {
		this(false, requiredPermissions);

	}

	public AffiliatedAuthorizationRequirement(boolean matchAll, Set<String> requiredPermissions) {
		this.matchAll = matchAll;
		this.requiredPermissions = requiredPermissions;
	}

	public AffiliatedAuthorizationRequirement(boolean matchAll, String... requiredPermissions) {
		this.requiredPermissions = new HashSet<>();
		this.matchAll = matchAll;
		this.requiredPermissions.addAll(Arrays.stream(requiredPermissions).distinct().toList());
	}

	public Set<String> getRequiredPermissions() {
		return this.requiredPermissions;
	}

	public boolean getMatchAll() {
		return this.matchAll;
	}
}
