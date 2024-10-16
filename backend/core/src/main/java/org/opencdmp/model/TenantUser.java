package org.opencdmp.model;

import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class TenantUser {

	private UUID id;
	public final static String _id = "id";

	private User user;
	public final static String _user = "user";

	private Tenant tenant;
	public final static String _tenant = "tenant";

	private IsActive isActive;
	public final static String _isActive = "isActive";

	private Instant createdAt;
	public final static String _createdAt = "createdAt";

	private Instant updatedAt;
	public final static String _updatedAt = "updatedAt";

	private String hash;
	public final static String _hash = "hash";

	private Boolean belongsToCurrentTenant;
	public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public IsActive getIsActive() {
		return isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Boolean getBelongsToCurrentTenant() {
		return belongsToCurrentTenant;
	}

	public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
		this.belongsToCurrentTenant = belongsToCurrentTenant;
	}
}
