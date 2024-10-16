package org.opencdmp.model.actionconfirmation;

import org.opencdmp.commons.enums.ActionConfirmationStatus;
import org.opencdmp.commons.enums.ActionConfirmationType;
import org.opencdmp.commons.enums.IsActive;
import org.opencdmp.model.user.User;

import java.time.Instant;
import java.util.UUID;

public class ActionConfirmation {

	private UUID id;
	public static final String _id = "id";

	private ActionConfirmationType type;
	public static final String _type = "type";

	private ActionConfirmationStatus status;
	public static final String _status = "status";
	private MergeAccountConfirmation mergeAccountConfirmation;

	public static final String _mergeAccountConfirmation = "mergeAccountConfirmation";

	private RemoveCredentialRequest removeCredentialRequest;

	public static final String _removeCredentialRequest = "removeCredentialRequest";

	private UserInviteToTenantRequest userInviteToTenantRequest;

	public static final String _userInviteToTenantRequest = "userInviteToTenantRequest";

	private PlanInvitation planInvitation;
	public static final String _planInvitation = "planInvitation";

	private Instant expiresAt;
	public static final String _expiresAt = "expiresAt";

	private User createdBy;
	public static final String _createdBy = "createdBy";

	private Instant createdAt;
	public static final String _createdAt = "createdAt";

	private Instant updatedAt;
	public static final String _updatedAt = "updatedAt";


	private IsActive isActive;
	public static final String _isActive = "isActive";

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

	public PlanInvitation getPlanInvitation() {
		return planInvitation;
	}

	public void setPlanInvitation(PlanInvitation planInvitation) {
		this.planInvitation = planInvitation;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
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

	public MergeAccountConfirmation getMergeAccountConfirmation() {
		return mergeAccountConfirmation;
	}

	public void setMergeAccountConfirmation(MergeAccountConfirmation mergeAccountConfirmation) {
		this.mergeAccountConfirmation = mergeAccountConfirmation;
	}

	public RemoveCredentialRequest getRemoveCredentialRequest() {
		return removeCredentialRequest;
	}

	public void setRemoveCredentialRequest(RemoveCredentialRequest removeCredentialRequest) {
		this.removeCredentialRequest = removeCredentialRequest;
	}

	public UserInviteToTenantRequest getUserInviteToTenantRequest() {
		return userInviteToTenantRequest;
	}

	public void setUserInviteToTenantRequest(UserInviteToTenantRequest userInviteToTenantRequest) {
		this.userInviteToTenantRequest = userInviteToTenantRequest;
	}
}
