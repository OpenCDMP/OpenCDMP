package org.opencdmp.commons.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

	private UUID planInvitationExternalUserType;
	private UUID planInvitationExistingUserType;
	private UUID planModifiedType;
	private UUID planFinalisedType;
	private UUID planStatusChangedType;
	private UUID planAnnotationCreatedType;
	private UUID planAnnotationStatusChangedType;
	private UUID descriptionCreatedType;
	private UUID descriptionModifiedType;
	private UUID descriptionFinalisedType;
	private UUID descriptionStatusChangedType;
	private UUID descriptionAnnotationCreatedType;
	private UUID descriptionAnnotationStatusChangedType;
	private UUID mergeAccountConfirmationType;
	private UUID removeCredentialConfirmationType;
	private UUID planDepositType;
	private UUID descriptionTemplateInvitationType;
	private UUID contactSupportType;
	private UUID publicContactSupportType;
	private UUID tenantSpecificInvitationExternalUserType;
	private UUID tenantSpecificInvitationExistingUserType;
	private String contactSupportEmail;

	public UUID getPlanInvitationExternalUserType() {
		return this.planInvitationExternalUserType;
	}

	public void setPlanInvitationExternalUserType(UUID planInvitationExternalUserType) {
		this.planInvitationExternalUserType = planInvitationExternalUserType;
	}

	public UUID getPlanInvitationExistingUserType() {
		return this.planInvitationExistingUserType;
	}

	public void setPlanInvitationExistingUserType(UUID planInvitationExistingUserType) {
		this.planInvitationExistingUserType = planInvitationExistingUserType;
	}

	public UUID getPlanModifiedType() {
		return this.planModifiedType;
	}

	public void setPlanModifiedType(UUID planModifiedType) {
		this.planModifiedType = planModifiedType;
	}

	public UUID getPlanFinalisedType() {
		return this.planFinalisedType;
	}

	public void setPlanFinalisedType(UUID planFinalisedType) {
		this.planFinalisedType = planFinalisedType;
	}

	public UUID getPlanStatusChangedType() {
		return planStatusChangedType;
	}

	public void setPlanStatusChangedType(UUID planStatusChangedType) {
		this.planStatusChangedType = planStatusChangedType;
	}

	public UUID getPlanAnnotationCreatedType() {
		return planAnnotationCreatedType;
	}

	public void setPlanAnnotationCreatedType(UUID planAnnotationCreatedType) {
		this.planAnnotationCreatedType = planAnnotationCreatedType;
	}

	public UUID getPlanAnnotationStatusChangedType() {
		return planAnnotationStatusChangedType;
	}

	public void setPlanAnnotationStatusChangedType(UUID planAnnotationStatusChangedType) {
		this.planAnnotationStatusChangedType = planAnnotationStatusChangedType;
	}

	public UUID getDescriptionCreatedType() {
		return descriptionCreatedType;
	}

	public void setDescriptionCreatedType(UUID descriptionCreatedType) {
		this.descriptionCreatedType = descriptionCreatedType;
	}

	public UUID getDescriptionModifiedType() {
		return this.descriptionModifiedType;
	}

	public void setDescriptionModifiedType(UUID descriptionModifiedType) {
		this.descriptionModifiedType = descriptionModifiedType;
	}

	public UUID getDescriptionFinalisedType() {
		return this.descriptionFinalisedType;
	}

	public void setDescriptionFinalisedType(UUID descriptionFinalisedType) {
		this.descriptionFinalisedType = descriptionFinalisedType;
	}

	public UUID getDescriptionStatusChangedType() {
		return descriptionStatusChangedType;
	}

	public void setDescriptionStatusChangedType(UUID descriptionStatusChangedType) {
		this.descriptionStatusChangedType = descriptionStatusChangedType;
	}

	public UUID getMergeAccountConfirmationType() {
		return this.mergeAccountConfirmationType;
	}

	public void setMergeAccountConfirmationType(UUID mergeAccountConfirmationType) {
		this.mergeAccountConfirmationType = mergeAccountConfirmationType;
	}

	public UUID getRemoveCredentialConfirmationType() {
		return this.removeCredentialConfirmationType;
	}

	public void setRemoveCredentialConfirmationType(UUID removeCredentialConfirmationType) {
		this.removeCredentialConfirmationType = removeCredentialConfirmationType;
	}

	public UUID getPlanDepositType() {
		return this.planDepositType;
	}

	public void setPlanDepositType(UUID planDepositType) {
		this.planDepositType = planDepositType;
	}

	public UUID getDescriptionTemplateInvitationType() {
		return this.descriptionTemplateInvitationType;
	}

	public void setDescriptionTemplateInvitationType(UUID descriptionTemplateInvitationType) {
		this.descriptionTemplateInvitationType = descriptionTemplateInvitationType;
	}

	public UUID getContactSupportType() {
		return this.contactSupportType;
	}

	public void setContactSupportType(UUID contactSupportType) {
		this.contactSupportType = contactSupportType;
	}

	public UUID getPublicContactSupportType() {
		return this.publicContactSupportType;
	}

	public void setPublicContactSupportType(UUID publicContactSupportType) {
		this.publicContactSupportType = publicContactSupportType;
	}

	public String getContactSupportEmail() {
		return this.contactSupportEmail;
	}

	public void setContactSupportEmail(String contactSupportEmail) {
		this.contactSupportEmail = contactSupportEmail;
	}

	public UUID getDescriptionAnnotationCreatedType() {
		return descriptionAnnotationCreatedType;
	}

	public void setDescriptionAnnotationCreatedType(UUID descriptionAnnotationCreatedType) {
		this.descriptionAnnotationCreatedType = descriptionAnnotationCreatedType;
	}

	public UUID getDescriptionAnnotationStatusChangedType() {
		return descriptionAnnotationStatusChangedType;
	}

	public void setDescriptionAnnotationStatusChangedType(UUID descriptionAnnotationStatusChangedType) {
		this.descriptionAnnotationStatusChangedType = descriptionAnnotationStatusChangedType;
	}

	public UUID getTenantSpecificInvitationExternalUserType() {
		return tenantSpecificInvitationExternalUserType;
	}

	public void setTenantSpecificInvitationExternalUserType(UUID tenantSpecificInvitationExternalUserType) {
		this.tenantSpecificInvitationExternalUserType = tenantSpecificInvitationExternalUserType;
	}

	public UUID getTenantSpecificInvitationExistingUserType() {
		return tenantSpecificInvitationExistingUserType;
	}

	public void setTenantSpecificInvitationExistingUserType(UUID tenantSpecificInvitationExistingUserType) {
		this.tenantSpecificInvitationExistingUserType = tenantSpecificInvitationExistingUserType;
	}
}
