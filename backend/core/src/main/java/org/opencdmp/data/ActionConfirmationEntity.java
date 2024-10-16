package org.opencdmp.data;

import org.opencdmp.commons.enums.ActionConfirmationStatus;
import org.opencdmp.commons.enums.ActionConfirmationType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.ActionConfirmationStatusConverter;
import org.opencdmp.data.converters.enums.ActionConfirmationTypeConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"ActionConfirmation\"")
public class ActionConfirmationEntity extends TenantScopedBaseEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public static final String _id = "id";

	@Column(name = "type", nullable = false)
	@Convert(converter = ActionConfirmationTypeConverter.class)
	private ActionConfirmationType type;
	public static final String _type = "type";

	@Column(name = "status", nullable = false)
	@Convert(converter = ActionConfirmationStatusConverter.class)
	private ActionConfirmationStatus status;
	public static final String _status = "status";

	@Column(name = "\"token\"", updatable = false, nullable = false)
	private String token;
	public static final String _token = "token";

	@Column(name = "\"data\"", nullable = false)
	private String data;
	public static final String _data = "data";

	@Column(name = "\"expires_at\"", nullable = false)
	private Instant expiresAt;
	public static final String _expiresAt = "expiresAt";

	@Column(name = "created_by", columnDefinition = "uuid", nullable = false)
	private UUID createdById;
	public static final String _createdById = "createdById";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	public static final String _createdAt = "createdAt";

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
	public static final String _updatedAt = "updatedAt";

	@Column(name = "is_active", nullable = false)
	@Convert(converter = IsActiveConverter.class)
	private IsActive isActive;

	public static final String _isActive = "isActive";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public ActionConfirmationType getType() {
		return type;
	}

	public void setType(ActionConfirmationType type) {
		this.type = type;
	}

	public ActionConfirmationStatus getStatus() {
		return status;
	}

	public void setStatus(ActionConfirmationStatus status) {
		this.status = status;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public UUID getCreatedById() {
		return createdById;
	}

	public void setCreatedById(UUID createdById) {
		this.createdById = createdById;
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

	public IsActive getIsActive() {
		return isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}
}
