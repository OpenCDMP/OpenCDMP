package org.opencdmp.data;

import jakarta.persistence.*;
import org.opencdmp.commons.enums.*;
import org.opencdmp.data.converters.enums.IsActiveConverter;
import org.opencdmp.data.converters.enums.PlanUpdateRequestActionTypeConverter;
import org.opencdmp.data.converters.enums.PlanUpdateRequestStatusConverter;
import org.opencdmp.data.tenant.TenantScopedBaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "\"PlanUpdateRequest\"")
public class PlanUpdateRequestEntity extends TenantScopedBaseEntity {

	@Id
	@Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
	private UUID id;
	public static final String _id = "id";

	@Column(name = "plan_id", columnDefinition = "uuid", nullable = true)
	private UUID planId;
	public static final String _planId = "planId";

	@Column(name = "submitter_id", columnDefinition = "uuid", nullable = true)
	private UUID submitterId;
	public static final String _submitterId = "submitterId";

	@Column(name = "action_type", nullable = false)
	@Convert(converter = PlanUpdateRequestActionTypeConverter.class)
	private PlanUpdateRequestActionType actionType;
	public static final String _actionType = "actionType";

	@Column(name = "status", nullable = false)
	@Convert(converter = PlanUpdateRequestStatusConverter.class)
	private PlanUpdateRequestStatus status;
	public static final String _status = "status";

	@Column(name = "\"data\"", nullable = false)
	private String data;
	public static final String _data = "data";

	@Column(name = "source_code", nullable = false)
	private String sourceCode;
	public static final String _sourceCode = "sourceCode";

	@Column(name = "request_at", nullable = false)
	private Instant requestAt;
	public static final String _requestAt = "requestAt";

	@Column(name = "approved_by", columnDefinition = "uuid", nullable = true)
	private UUID approvedBy;
	public static final String _approvedBy = "approvedBy";

	@Column(name = "approved_at", nullable = true)
	private Instant approvedAt;
	public static final String _approvedAt = "approvedAt";

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

	public UUID getPlanId() {
		return planId;
	}

	public void setPlanId(UUID planId) {
		this.planId = planId;
	}

	public UUID getSubmitterId() {
		return submitterId;
	}

	public void setSubmitterId(UUID submitterId) {
		this.submitterId = submitterId;
	}

	public PlanUpdateRequestActionType getActionType() {
		return actionType;
	}

	public void setActionType(PlanUpdateRequestActionType actionType) {
		this.actionType = actionType;
	}

	public PlanUpdateRequestStatus getStatus() {
		return status;
	}

	public void setStatus(PlanUpdateRequestStatus status) {
		this.status = status;
	}

	public Instant getRequestAt() {
		return requestAt;
	}

	public void setRequestAt(Instant requestAt) {
		this.requestAt = requestAt;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public UUID getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(UUID approvedBy) {
		this.approvedBy = approvedBy;
	}

	public Instant getApprovedAt() {
		return approvedAt;
	}

	public void setApprovedAt(Instant approvedAt) {
		this.approvedAt = approvedAt;
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
