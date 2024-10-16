package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"TenantUser\"")
public class TenantUserEntity extends TenantScopedBaseEntity {
	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public final static String _id = "id";

	@Column(name = "user", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID userId;
	public final static String _userId = "userId";

	@Column(name = "tenant", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID tenantId;
	public final static String _tenantId = "tenantId";

	@Column(name = "is_active", length = 20, nullable = false)
	@Convert(converter = IsActiveConverter.class)
	private IsActive isActive;
	public final static String _isActive = "isActive";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;
	public final static String _createdAt = "createdAt";

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
	public final static String _updatedAt = "updatedAt";

	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getUserId() {
		return this.userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getTenantId() {
		return this.tenantId;
	}

	public void setTenantId(UUID tenantId) {
		this.tenantId = tenantId;
	}

	public IsActive getIsActive() {
		return this.isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return this.updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
