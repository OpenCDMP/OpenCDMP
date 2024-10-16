package org.opencdmp.model;

import org.opencdmp.commons.enums.UserSettingsType;

import java.time.Instant;
import java.util.UUID;

public class UserSettings {

	private UUID id;
	public static final String _id = "id";

	private String key;
	public static final String _key = "key";

	private String value;
	public static final String _value = "value";

	private UUID entityId;
	public static final String _entityId = "entityId";

	private Instant createdAt;
	public static final String _createdAt = "createdAt";

	private Instant updatedAt;
	public static final String _updatedAt = "updatedAt";

	private UserSettingsType type;
	public static final String _type = "type";

	private String hash;
	public static final String _hash = "hash";

	private Boolean belongsToCurrentTenant;
	public static final String _belongsToCurrentTenant = "belongsToCurrentTenant";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
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

	public UserSettingsType getType() {
		return type;
	}

	public void setType(UserSettingsType type) {
		this.type = type;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Boolean getBelongsToCurrentTenant() {
		return belongsToCurrentTenant;
	}

	public void setBelongsToCurrentTenant(Boolean belongsToCurrentTenant) {
		this.belongsToCurrentTenant = belongsToCurrentTenant;
	}
}
