package org.opencdmp.model;

import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class UserRole {
	private UUID id;
	public static final String _id = "id";

	private String role;
	public static final String _role = "role";

	private User user;
	public static final String _user = "user";

	private Instant createdAt;

	public static final String _createdAt = "createdAt";

	private Boolean belongsToCurrentTenant;
	public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getBelongsToCurrentTenant() {
		return belongsToCurrentTenant;
	}

	public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
		this.belongsToCurrentTenant = belongsToCurrentTenant;
	}
}

