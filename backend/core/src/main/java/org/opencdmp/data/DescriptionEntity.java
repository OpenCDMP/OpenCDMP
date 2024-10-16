package org.opencdmp.data;


import jakarta.persistence.*;
import org.opencdmp.commons.enums.DescriptionStatus;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.data.converters.enums.DescriptionStatusConverter;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "\"Description\"")
public class DescriptionEntity extends TenantScopedBaseEntity  {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;

	public static final String _id = "id";

	@Column(name = "label", length = DescriptionEntity._labelLength, nullable = false)
	private String label;
	public static final int _labelLength = 250;

	public static final String _label = "label";


	@Column(name = "properties")
	private String properties;

	public static final String _properties = "properties";

	@Column(name = "status", nullable = false)
	@Convert(converter = DescriptionStatusConverter.class)
	private DescriptionStatus status;
	public static final String _status = "status";

	@Column(name = "description")
	private String description;

	public static final String _description = "description";

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

	@Column(name = "\"finalized_at\"")
	private Instant finalizedAt;

	public static final String _finalizedAt = "finalizedAt";

	@Column(name = "created_by", columnDefinition = "uuid", nullable = false)
	private UUID createdById;

	public static final String _createdById = "createdById";

	@Column(name = "plan_description_template", columnDefinition = "uuid", nullable = false)
	private UUID planDescriptionTemplateId;
	public static final String _planDescriptionTemplateId = "planDescriptionTemplateId";
	
	@Column(name = "plan", columnDefinition = "uuid", nullable = false)
	private UUID planId;
	public static final String _planId = "planId";

	@Column(name = "description_template", columnDefinition = "uuid", nullable = false)
	private UUID descriptionTemplateId;
	public static final String _descriptionTemplateId = "descriptionTemplateId";



	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getProperties() {
		return this.properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}
	
	public DescriptionStatus getStatus() {
		return this.status;
	}

	public void setStatus(DescriptionStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public IsActive getIsActive() {
		return this.isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public Instant getFinalizedAt() {
		return this.finalizedAt;
	}

	public void setFinalizedAt(Instant finalizedAt) {
		this.finalizedAt = finalizedAt;
	}

	public UUID getCreatedById() {
		return this.createdById;
	}

	public void setCreatedById(UUID createdById) {
		this.createdById = createdById;
	}

	public UUID getPlanDescriptionTemplateId() {
		return this.planDescriptionTemplateId;
	}

	public void setPlanDescriptionTemplateId(UUID planDescriptionTemplateId) {
		this.planDescriptionTemplateId = planDescriptionTemplateId;
	}

	public UUID getPlanId() {
		return this.planId;
	}

	public void setPlanId(UUID planId) {
		this.planId = planId;
	}

	public UUID getDescriptionTemplateId() {
		return this.descriptionTemplateId;
	}

	public void setDescriptionTemplateId(UUID descriptionTemplateId) {
		this.descriptionTemplateId = descriptionTemplateId;
	}
}

