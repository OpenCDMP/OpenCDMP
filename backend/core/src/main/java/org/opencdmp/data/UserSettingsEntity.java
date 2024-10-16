package org.opencdmp.data;

import org.opencdmp.commons.enums.UserSettingsType;
import org.opencdmp.data.converters.enums.UserSettingsTypeConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"UserSettings\"")
public class UserSettingsEntity extends TenantScopedBaseEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public static final String _id = "id";

	@Column(name = "key", length = 500, nullable = false)
	private String key;
	public static final String _key = "key";

	@Column(name = "value", nullable = false)
	private String value;
	public static final String _value = "value";

	@Column(name = "name", nullable = false)
	private String name;
	public static final String _name = "name";

	@Column(name = "entity_id", columnDefinition = "uuid", nullable = true)
	private UUID entityId;
	public static final String _entityId = "entityId";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	public static final String _createdAt = "createdAt";

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
	public static final String _updatedAt = "updatedAt";

	@Column(name = "type", nullable = false)
	@Convert(converter = UserSettingsTypeConverter.class)
	private UserSettingsType type;
	public static final String _type = "type";

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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
