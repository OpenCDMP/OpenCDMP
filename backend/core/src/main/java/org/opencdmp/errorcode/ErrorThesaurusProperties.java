package org.opencdmp.errorcode;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "error-thesaurus")
public class ErrorThesaurusProperties {

    private ErrorDescription systemError;

    public ErrorDescription getSystemError() {
        return this.systemError;
    }

    public void setSystemError(ErrorDescription systemError) {
        this.systemError = systemError;
    }

    private ErrorDescription forbidden;

    public ErrorDescription getForbidden() {
        return this.forbidden;
    }

    public void setForbidden(ErrorDescription forbidden) {
        this.forbidden = forbidden;
    }

    private ErrorDescription hashConflict;

    public ErrorDescription getHashConflict() {
        return this.hashConflict;
    }

    public void setHashConflict(ErrorDescription hashConflict) {
        this.hashConflict = hashConflict;
    }

    private ErrorDescription modelValidation;

    public ErrorDescription getModelValidation() {
        return this.modelValidation;
    }

    public void setModelValidation(ErrorDescription modelValidation) {
        this.modelValidation = modelValidation;
    }

    private ErrorDescription descriptionTemplateNewVersionConflict;

    public ErrorDescription getDescriptionTemplateNewVersionConflict() {
        return this.descriptionTemplateNewVersionConflict;
    }

    public void setDescriptionTemplateNewVersionConflict(ErrorDescription descriptionTemplateNewVersionConflict) {
        this.descriptionTemplateNewVersionConflict = descriptionTemplateNewVersionConflict;
    }

    private ErrorDescription descriptionTemplateIsNotFinalized;

    public ErrorDescription getDescriptionTemplateIsNotFinalized() {
        return this.descriptionTemplateIsNotFinalized;
    }

    private ErrorDescription descriptionTemplateNewVersionAlreadyCreatedDraft;

    public ErrorDescription getDescriptionTemplateNewVersionAlreadyCreatedDraft() {
        return this.descriptionTemplateNewVersionAlreadyCreatedDraft;
    }

    public void setDescriptionTemplateNewVersionAlreadyCreatedDraft(ErrorDescription descriptionTemplateNewVersionAlreadyCreatedDraft) {
        this.descriptionTemplateNewVersionAlreadyCreatedDraft = descriptionTemplateNewVersionAlreadyCreatedDraft;
    }

    public void setDescriptionTemplateIsNotFinalized(ErrorDescription descriptionTemplateIsNotFinalized) {
        this.descriptionTemplateIsNotFinalized = descriptionTemplateIsNotFinalized;
    }

    private ErrorDescription multipleDescriptionTemplateVersionsNotSupported;

    public ErrorDescription getMultipleDescriptionTemplateVersionsNotSupported() {
        return  this.multipleDescriptionTemplateVersionsNotSupported;
    }

    public void setMultipleDescriptionTemplateVersionsNotSupported(ErrorDescription multipleDescriptionTemplateVersionsNotSupported) {
        this.multipleDescriptionTemplateVersionsNotSupported = multipleDescriptionTemplateVersionsNotSupported;
    }

    private ErrorDescription planNewVersionConflict;

    public ErrorDescription getPlanNewVersionConflict() {
        return this.planNewVersionConflict;
    }

    public void setPlanNewVersionConflict(ErrorDescription planNewVersionConflict) {
        this.planNewVersionConflict = planNewVersionConflict;
    }

    private ErrorDescription planIsNotFinalized;

    public ErrorDescription getPlanIsNotFinalized() {
        return this.planIsNotFinalized;
    }

    public void setPlanIsNotFinalized(ErrorDescription planIsNotFinalized) {
        this.planIsNotFinalized = planIsNotFinalized;
    }

    private ErrorDescription multiplePlanVersionsNotSupported;

    public ErrorDescription getMultiplePlanVersionsNotSupported() {
        return this.multiplePlanVersionsNotSupported;
    }

    public void setMultiplePlanVersionsNotSupported(ErrorDescription multiplePlanVersionsNotSupported) {
        this.multiplePlanVersionsNotSupported = multiplePlanVersionsNotSupported;
    }

    public ErrorDescription planBlueprintNewVersionConflict;

    public ErrorDescription getPlanBlueprintNewVersionConflict() {
        return this.planBlueprintNewVersionConflict;
    }

    public void setPlanBlueprintNewVersionConflict(ErrorDescription planBlueprintNewVersionConflict) {
        this.planBlueprintNewVersionConflict = planBlueprintNewVersionConflict;
    }

    private ErrorDescription planBlueprintNewVersionAlreadyCreatedDraft;

    public ErrorDescription getPlanBlueprintNewVersionAlreadyCreatedDraft() {
        return this.planBlueprintNewVersionAlreadyCreatedDraft;
    }

    public void setPlanBlueprintNewVersionAlreadyCreatedDraft(ErrorDescription planBlueprintNewVersionAlreadyCreatedDraft) {
        this.planBlueprintNewVersionAlreadyCreatedDraft = planBlueprintNewVersionAlreadyCreatedDraft;
    }

    private ErrorDescription planIsFinalized;

    public ErrorDescription getPlanIsFinalized() {
        return this.planIsFinalized;
    }

    public void setPlanIsFinalized(ErrorDescription planIsFinalized) {
        this.planIsFinalized = planIsFinalized;
    }

    private ErrorDescription planCanNotChange;

    public ErrorDescription getPlanCanNotChange() {
        return this.planCanNotChange;
    }

    public void setPlanCanNotChange(ErrorDescription planCanNotChange) {
        this.planCanNotChange = planCanNotChange;
    }
    
    private ErrorDescription planDescriptionTemplateCanNotChange;

    public ErrorDescription getPlanDescriptionTemplateCanNotChange() {
        return this.planDescriptionTemplateCanNotChange;
    }

    public void setPlanDescriptionTemplateCanNotChange(ErrorDescription planDescriptionTemplateCanNotChange) {
        this.planDescriptionTemplateCanNotChange = planDescriptionTemplateCanNotChange;
    }

    private ErrorDescription  invalidDescriptionTemplate;

    public ErrorDescription getInvalidDescriptionTemplate() {
        return this.invalidDescriptionTemplate;
    }

    public void setInvalidDescriptionTemplate(ErrorDescription invalidDescriptionTemplate) {
        this.invalidDescriptionTemplate = invalidDescriptionTemplate;
    }

    private ErrorDescription descriptionIsFinalized;

    public ErrorDescription getDescriptionIsFinalized() {
        return this.descriptionIsFinalized;
    }

    public void setDescriptionIsFinalized(ErrorDescription descriptionIsFinalized) {
        this.descriptionIsFinalized = descriptionIsFinalized;
    }

    private ErrorDescription planBlueprintHasNoDescriptionTemplates;

    public ErrorDescription getPlanBlueprintHasNoDescriptionTemplates() {
        return this.planBlueprintHasNoDescriptionTemplates;
    }

    public void setPlanBlueprintHasNoDescriptionTemplates(ErrorDescription planBlueprintHasNoDescriptionTemplates) {
        this.planBlueprintHasNoDescriptionTemplates = planBlueprintHasNoDescriptionTemplates;
    }

    private ErrorDescription planDescriptionTemplateCanNotRemove;

    public ErrorDescription getPlanDescriptionTemplateCanNotRemove() {
        return this.planDescriptionTemplateCanNotRemove;
    }

    public void setPlanDescriptionTemplateCanNotRemove(ErrorDescription planDescriptionTemplateCanNotRemove) {
        this.planDescriptionTemplateCanNotRemove = planDescriptionTemplateCanNotRemove;
    }

    private ErrorDescription missingTenant;

    public ErrorDescription getMissingTenant() {
        return this.missingTenant;
    }

    public void setMissingTenant(ErrorDescription missingTenant) {
        this.missingTenant = missingTenant;
    }

    private ErrorDescription tenantNotAllowed;

    public ErrorDescription getTenantNotAllowed() {
        return this.tenantNotAllowed;
    }

    public void setTenantNotAllowed(ErrorDescription tenantNotAllowed) {
        this.tenantNotAllowed = tenantNotAllowed;
    }
    
    private ErrorDescription tenantTampering;

    public ErrorDescription getTenantTampering() {
        return this.tenantTampering;
    }

    public void setTenantTampering(ErrorDescription tenantTampering) {
        this.tenantTampering = tenantTampering;
    }

    private ErrorDescription tenantConfigurationTypeCanNotChange;

    public ErrorDescription getTenantConfigurationTypeCanNotChange() {
        return this.tenantConfigurationTypeCanNotChange;
    }

    public void setTenantConfigurationTypeCanNotChange(ErrorDescription tenantConfigurationTypeCanNotChange) {
        this.tenantConfigurationTypeCanNotChange = tenantConfigurationTypeCanNotChange;
    }

    private ErrorDescription multipleTenantConfigurationTypeNotAllowed;

    public ErrorDescription getMultipleTenantConfigurationTypeNotAllowed() {
        return this.multipleTenantConfigurationTypeNotAllowed;
    }

    public void setMultipleTenantConfigurationTypeNotAllowed(ErrorDescription multipleTenantConfigurationTypeNotAllowed) {
        this.multipleTenantConfigurationTypeNotAllowed = multipleTenantConfigurationTypeNotAllowed;
    }

    private ErrorDescription tenantCodeExists;

    public ErrorDescription getTenantCodeExists() {
        return this.tenantCodeExists;
    }

    public void setTenantCodeExists(ErrorDescription tenantCodeExists) {
        this.tenantCodeExists = tenantCodeExists;
    }


    private ErrorDescription planNewVersionAlreadyCreatedDraft;

    public ErrorDescription getPlanNewVersionAlreadyCreatedDraft() {
        return this.planNewVersionAlreadyCreatedDraft;
    }

    public void setPlanNewVersionAlreadyCreatedDraft(ErrorDescription planNewVersionAlreadyCreatedDraft) {
        this.planNewVersionAlreadyCreatedDraft = planNewVersionAlreadyCreatedDraft;
    }

    private ErrorDescription descriptionTemplateInactiveUser;

    public ErrorDescription getDescriptionTemplateInactiveUser() {
        return this.descriptionTemplateInactiveUser;
    }

    public void setDescriptionTemplateInactiveUser(ErrorDescription descriptionTemplateInactiveUser) {
        this.descriptionTemplateInactiveUser = descriptionTemplateInactiveUser;
    }

    private ErrorDescription descriptionTemplateMissingUserContactInfo;

    public ErrorDescription getDescriptionTemplateMissingUserContactInfo() {
        return this.descriptionTemplateMissingUserContactInfo;
    }

    public void setDescriptionTemplateMissingUserContactInfo(ErrorDescription descriptionTemplateMissingUserContactInfo) {
        this.descriptionTemplateMissingUserContactInfo = descriptionTemplateMissingUserContactInfo;
    }


    private ErrorDescription planInactiveUser;

    public ErrorDescription getPlanInactiveUser() {
        return this.planInactiveUser;
    }

    public void setPlanInactiveUser(ErrorDescription planInactiveUser) {
        this.planInactiveUser = planInactiveUser;
    }


    private ErrorDescription planMissingUserContactInfo;

    public ErrorDescription getPlanMissingUserContactInfo() {
        return this.planMissingUserContactInfo;
    }

    public void setPlanMissingUserContactInfo(ErrorDescription planMissingUserContactInfo) {
        this.planMissingUserContactInfo = planMissingUserContactInfo;
    }


    private ErrorDescription importDescriptionWithoutPlanDescriptionTemplate;

    public ErrorDescription getImportDescriptionWithoutPlanDescriptionTemplate() {
        return this.importDescriptionWithoutPlanDescriptionTemplate;
    }

    public void setImportDescriptionWithoutPlanDescriptionTemplate(ErrorDescription importDescriptionWithoutPlanDescriptionTemplate) {
        this.importDescriptionWithoutPlanDescriptionTemplate = importDescriptionWithoutPlanDescriptionTemplate;
    }

    private ErrorDescription duplicatePlanUser;

    public ErrorDescription getDuplicatePlanUser() {
        return this.duplicatePlanUser;
    }

    public void setDuplicatePlanUser(ErrorDescription duplicatePlanUser) {
        this.duplicatePlanUser = duplicatePlanUser;
    }

    private ErrorDescription referenceTypeCodeExists;

    public ErrorDescription getReferenceTypeCodeExists() {
        return this.referenceTypeCodeExists;
    }

    public void setReferenceTypeCodeExists(ErrorDescription referenceTypeCodeExists) {
        this.referenceTypeCodeExists = referenceTypeCodeExists;
    }
    
    private ErrorDescription prefillingSourceCodeExists;

    public ErrorDescription getPrefillingSourceCodeExists() {
        return this.prefillingSourceCodeExists;
    }

    public void setPrefillingSourceCodeExists(ErrorDescription prefillingSourceCodeExists) {
        this.prefillingSourceCodeExists = prefillingSourceCodeExists;
    }

    private ErrorDescription inviteUserAlreadyConfirmed;

    public ErrorDescription getInviteUserAlreadyConfirmed() {
        return inviteUserAlreadyConfirmed;
    }

    public void setInviteUserAlreadyConfirmed(ErrorDescription inviteUserAlreadyConfirmed) {
        this.inviteUserAlreadyConfirmed = inviteUserAlreadyConfirmed;
    }

    private ErrorDescription requestHasExpired;

    public ErrorDescription getRequestHasExpired() {
        return requestHasExpired;
    }

    public void setRequestHasExpired(ErrorDescription requestHasExpired) {
        this.requestHasExpired = requestHasExpired;
    }

    private ErrorDescription maxDescriptionsExceeded;

    public ErrorDescription getMaxDescriptionsExceeded() {
        return maxDescriptionsExceeded;
    }

    public void setMaxDescriptionsExceeded(ErrorDescription maxDescriptionsExceeded) {
        this.maxDescriptionsExceeded = maxDescriptionsExceeded;
    }

    private ErrorDescription usageLimitException;

    public ErrorDescription getUsageLimitException() {
        return usageLimitException;
    }

    public void setUsageLimitException(ErrorDescription usageLimitException) {
        this.usageLimitException = usageLimitException;
    }

    private ErrorDescription usageLimitMetricAlreadyExists;

    public ErrorDescription getUsageLimitMetricAlreadyExists() {
        return usageLimitMetricAlreadyExists;
    }

    public void setUsageLimitMetricAlreadyExists(ErrorDescription usageLimitMetricAlreadyExists) {
        this.usageLimitMetricAlreadyExists = usageLimitMetricAlreadyExists;
    }

    private ErrorDescription descriptionTemplateTypeCodeExists;

    public ErrorDescription getDescriptionTemplateTypeCodeExists() {
        return descriptionTemplateTypeCodeExists;
    }

    public void setDescriptionTemplateTypeCodeExists(ErrorDescription descriptionTemplateTypeCodeExists) {
        this.descriptionTemplateTypeCodeExists = descriptionTemplateTypeCodeExists;
    }

    private ErrorDescription descriptionTemplateTypeImportNotFound;

    public ErrorDescription getDescriptionTemplateTypeImportNotFound() {
        return descriptionTemplateTypeImportNotFound;
    }

    public void setDescriptionTemplateTypeImportNotFound(ErrorDescription descriptionTemplateTypeImportNotFound) {
        this.descriptionTemplateTypeImportNotFound = descriptionTemplateTypeImportNotFound;
    }

    private ErrorDescription referenceTypeImportNotFound;

    public ErrorDescription getReferenceTypeImportNotFound() {
        return referenceTypeImportNotFound;
    }

    public void setReferenceTypeImportNotFound(ErrorDescription referenceTypeImportNotFound) {
        this.referenceTypeImportNotFound = referenceTypeImportNotFound;
    }

    private ErrorDescription descriptionTemplateCodeExists;

    public ErrorDescription getDescriptionTemplateCodeExists() {
        return descriptionTemplateCodeExists;
    }

    public void setDescriptionTemplateCodeExists(ErrorDescription descriptionTemplateCodeExists) {
        this.descriptionTemplateCodeExists = descriptionTemplateCodeExists;
    }

    private ErrorDescription descriptionTemplateImportNotFound;

    public ErrorDescription getDescriptionTemplateImportNotFound() {
        return descriptionTemplateImportNotFound;
    }

    public void setDescriptionTemplateImportNotFound(ErrorDescription descriptionTemplateImportNotFound) {
        this.descriptionTemplateImportNotFound = descriptionTemplateImportNotFound;
    }

    private ErrorDescription planBlueprintCodeExists;

    public ErrorDescription getPlanBlueprintCodeExists() {
        return planBlueprintCodeExists;
    }

    public void setPlanBlueprintCodeExists(ErrorDescription planBlueprintCodeExists) {
        this.planBlueprintCodeExists = planBlueprintCodeExists;
    }

    private ErrorDescription planBlueprintImportNotFound;

    public ErrorDescription getPlanBlueprintImportNotFound() {
        return planBlueprintImportNotFound;
    }

    public void setPlanBlueprintImportNotFound(ErrorDescription planBlueprintImportNotFound) {
        this.planBlueprintImportNotFound = planBlueprintImportNotFound;
    }

    private ErrorDescription blueprintDescriptionTemplateImportDraft;

    public ErrorDescription getBlueprintDescriptionTemplateImportDraft() {
        return blueprintDescriptionTemplateImportDraft;
    }

    public void setBlueprintDescriptionTemplateImportDraft(ErrorDescription blueprintDescriptionTemplateImportDraft) {
        this.blueprintDescriptionTemplateImportDraft = blueprintDescriptionTemplateImportDraft;
    }

    private ErrorDescription planDescriptionTemplateImportDraft;

    public ErrorDescription getPlanDescriptionTemplateImportDraft() {
        return planDescriptionTemplateImportDraft;
    }

    public void setPlanDescriptionTemplateImportDraft(ErrorDescription planDescriptionTemplateImportDraft) {
        this.planDescriptionTemplateImportDraft = planDescriptionTemplateImportDraft;
    }

    private ErrorDescription descriptionTemplateTypeImportDraft;

    public ErrorDescription getDescriptionTemplateTypeImportDraft() {
        return descriptionTemplateTypeImportDraft;
    }

    public void setDescriptionTemplateTypeImportDraft(ErrorDescription descriptionTemplateTypeImportDraft) {
        this.descriptionTemplateTypeImportDraft = descriptionTemplateTypeImportDraft;
    }

    private ErrorDescription planBlueprintImportDraft;

    public ErrorDescription getPlanBlueprintImportDraft() {
        return planBlueprintImportDraft;
    }

    public void setPlanBlueprintImportDraft(ErrorDescription planBlueprintImportDraft) {
        this.planBlueprintImportDraft = planBlueprintImportDraft;
    }

    private ErrorDescription missingTenantRole;

    public ErrorDescription getMissingTenantRole() {
        return missingTenantRole;
    }

    public void setMissingTenantRole(ErrorDescription missingTenantRole) {
        this.missingTenantRole = missingTenantRole;
    }

    private ErrorDescription missingGlobalRole;

    public ErrorDescription getMissingGlobalRole() {
        return missingGlobalRole;
    }

    public void setMissingGlobalRole(ErrorDescription missingGlobalRole) {
        this.missingGlobalRole = missingGlobalRole;
    }

    private ErrorDescription userProfileInactive;

    public ErrorDescription getUserProfileInactive() { return  userProfileInactive; }

    public void setUserProfileInactive(ErrorDescription userProfileInactive) {this.userProfileInactive = userProfileInactive; }

    private ErrorDescription planInvitationAlreadyConfirmed;

    public ErrorDescription getPlanInvitationAlreadyConfirmed() {
        return planInvitationAlreadyConfirmed;
    }

    public void setPlanInvitationAlreadyConfirmed(ErrorDescription planInvitationAlreadyConfirmed) {
        this.planInvitationAlreadyConfirmed = planInvitationAlreadyConfirmed;
    }

    private ErrorDescription anotherUserToken;

    public ErrorDescription getAnotherUserToken() {
        return anotherUserToken;
    }

    public void setAnotherUserToken(ErrorDescription anotherUserToken) {
        this.anotherUserToken = anotherUserToken;
    }

    private ErrorDescription tokenNotExist;

    public ErrorDescription getTokenNotExist() {
        return tokenNotExist;
    }

    public void setTokenNotExist(ErrorDescription tokenNotExist) {
        this.tokenNotExist = tokenNotExist;
    }
}
