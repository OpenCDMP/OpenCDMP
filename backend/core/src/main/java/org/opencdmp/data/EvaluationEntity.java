package org.opencdmp.data;


import jakarta.persistence.*;
import org.opencdmp.commons.enums.EntityType;
import org.opencdmp.commons.enums.EvaluationStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.EntityTypeConverter;
import org.opencdmp.data.converters.enums.EvaluationStatusConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "\"Evaluation\"")
public class EvaluationEntity extends TenantScopedBaseEntity  {

	@Id
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;
	public static final String _id = "id";

	@Column(name = "entity_type", nullable = false)
	@Convert(converter = EntityTypeConverter.class)
	private EntityType entityType;
	public static final String _entityType = "entityType";

	@Column(name = "entity", updatable = false, nullable = false)
	private UUID entityId;
	public static final String _entityId = "entityId";

	@Column(name = "evaluated_at", nullable = false)
	private Instant evaluatedAt;
	public static final String _evaluatedAt = "evaluatedAt";

	@Column(name = "data", nullable = false)
	private String data;
	public static final String _data = "data";

	@Column(name = "status", nullable = false)
	@Convert(converter = EvaluationStatusConverter.class)
	private EvaluationStatus status;
	public static final String _status = "status";

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

	@Column(name = "created_by", columnDefinition = "uuid", nullable = false)
	private UUID createdById;
	public static final String _createdById = "createdById";

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public UUID getEntityId() {
		return entityId;
	}

	public void setEntityId(UUID entityId) {
		this.entityId = entityId;
	}

	public Instant getEvaluatedAt() {
		return evaluatedAt;
	}

	public void setEvaluatedAt(Instant evaluatedAt) {
		this.evaluatedAt = evaluatedAt;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public EvaluationStatus getStatus() {
		return status;
	}

	public void setStatus(EvaluationStatus status) {
		this.status = status;
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

	public UUID getCreatedById() {
		return createdById;
	}

	public void setCreatedById(UUID createdById) {
		this.createdById = createdById;
	}
}

