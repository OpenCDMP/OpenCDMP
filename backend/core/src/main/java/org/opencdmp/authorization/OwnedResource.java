package org.opencdmp.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationResource;

import java.util.List;
import java.util.UUID;

public class OwnedResource extends AuthorizationResource {
	private List<UUID> userIds;

	public OwnedResource(UUID userId) {
		this(List.of(userId));
	}

	public OwnedResource(List<UUID> userIds) {
		this.userIds = userIds;
	}

	public List<UUID> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<UUID> userIds) {
		this.userIds = userIds;
	}
}
