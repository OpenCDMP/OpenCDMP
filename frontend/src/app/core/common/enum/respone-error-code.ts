import { TranslateService } from "@ngx-translate/core";

export enum ResponseErrorCode {
  // Common Errors
  HashConflict = 100,
  Forbidden = 101,
  SystemError = 102,
  MissingTenant = 103,
  ModelValidation = 106,
  TenantNotAllowed = 112,
  DescriptionTemplateNewVersionConflict = 113,
  DescriptionTemplateIsNotFinalized = 114,
  MultipleDescriptionTemplateVersionsNotSupported = 115,
  PlanNewVersionConflict = 116,
  PlanIsNotFinalized = 117,
  MultiplePlanVersionsNotSupported = 118,
  PlanIsFinalized = 119,
  PlanCanNotChange = 120,
  PlanDescriptionTemplateCanNotChange = 121,
  InvalidDescriptionTemplate = 122,
  DescriptionIsFinalized = 123,
  PlanBlueprintHasNoDescriptionTemplates = 124,
  PlanBlueprintNewVersionConflict = 125,
  PlanDescriptionTemplateCanNotRemove = 126,
  TenantTampering = 127,
  TenantConfigurationTypeCanNotChange = 128,
  MultipleTenantConfigurationTypeNotAllowed = 129,
  TenantCodeExists = 130,
  PlanNewVersionAlreadyCreatedDraft = 131,
  DescriptionTemplateInactiveUser = 132,
  DescriptionTemplateMissingUserContactInfo = 133,
  PlanInactiveUser = 134,
  PlanMissingUserContactInfo = 135,
  ImportDescriptionWithoutPlanDescriptionTemplate = 136,
  DuplicatePlanUser = 137,
  DescriptionTemplateNewVersionAlreadyCreatedDraft = 138,
  PlanBlueprintNewVersionAlreadyCreatedDraft = 139,
  ReferenceTypeCodeExists = 140,
  PrefillingSourceCodeExists = 141,
  InviteUserAlreadyConfirmed = 142,
  RequestHasExpired = 143,
  MaxDescriptionsExceeded = 144,
  UsageLimitException = 145,
  UsageLimitMetricAlreadyExists = 146,
  DescriptionTemplateTypeCodeExists = 147,
  descriptionTemplateTypeImportNotFound = 148,
  referenceTypeImportNotFound = 149,
  descriptionTemplateCodeExists = 150,
  descriptionTemplateImportNotFound = 151,
  planBlueprintCodeExists = 152,
  planBlueprintImportNotFound = 153,
  blueprintDescriptionTemplateImportDraft = 154,
  planDescriptionTemplateImportDraft = 155,
  descriptionTemplateTypeImportDraft = 156,
  planBlueprintImportDraft = 157,
  missingTenantRole = 158,
  missingGlobalRole = 159,
  userProfileInactive = 160,
  planInvitationAlreadyConfirmed = 161,
  anotherUserToken = 162,
  tokenNotExist = 163,

  // Notification & Annotation Errors
  InvalidApiKey = 200,
  StaleApiKey = 201,
  SensitiveInfo = 202,
  NonPersonPrincipal = 203,
  BlockingConsent = 204,
  SingleTenantConfigurationPerTypeSupported = 205,
  IncompatibleTenantConfigurationTypes = 206,
  MissingTotpToken = 207,
  OverlappingTenantConfigurationNotifierList = 208,
}

export class ResponseErrorCodeHelper {
  public static isBackendError(statusCode: number): boolean {
  	return Object.values(ResponseErrorCode).includes(statusCode);
  }

  public static getErrorMessageByBackendStatusCode(statusCode: number, language: TranslateService): string {
  	switch (statusCode) {
  	  case ResponseErrorCode.HashConflict:
        return language.instant("GENERAL.BACKEND-ERRORS.HASH-CONFLICT");
  	  case ResponseErrorCode.Forbidden:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.FORBIDDEN");
  	  case ResponseErrorCode.SystemError:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.SYSTEM-ERROR");
  	  case ResponseErrorCode.MissingTenant:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.MISSING-TENANT");
  	  case ResponseErrorCode.ModelValidation:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.MODEL-VALIDATION");
  	  case ResponseErrorCode.TenantNotAllowed:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.TENANT-NOT-ALLOWED");
  	  case ResponseErrorCode.DescriptionTemplateNewVersionConflict:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-NEW-VERSION-CONFLICT");
  	  case ResponseErrorCode.DescriptionTemplateIsNotFinalized:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-IS-NOT-FINALIZED");
  	  case ResponseErrorCode.MultipleDescriptionTemplateVersionsNotSupported:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.MULTIPLE-DESCRIPTION-TEMPLATE-VERSIONS-NOT-SUPPORTED");
  	  case ResponseErrorCode.PlanNewVersionConflict:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-NEW-VERSION-CONFLICT");
  	  case ResponseErrorCode.PlanIsNotFinalized:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-IS-NOT-FINALIZED");
  	  case ResponseErrorCode.MultiplePlanVersionsNotSupported:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.MULTIPLE-PLAN-VERSIONS-NOT-SUPPORTED");
  	  case ResponseErrorCode.PlanIsFinalized:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-IS-FINALIZED");
  	  case ResponseErrorCode.PlanCanNotChange:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-CAN-NOT-CHANGE");
  	  case ResponseErrorCode.PlanDescriptionTemplateCanNotChange:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-DESCRIPTION-TEMPLATE-CAN-NOT-CHANGE");
  	  case ResponseErrorCode.InvalidDescriptionTemplate:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.INVALID-DESCRIPTION-TEMPLATE");
  	  case ResponseErrorCode.DescriptionIsFinalized:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-IS-FINALIZED");
  	  case ResponseErrorCode.PlanBlueprintHasNoDescriptionTemplates:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-HAS-NO-DESCRIPTION-TEMPLATES");
  	  case ResponseErrorCode.PlanBlueprintNewVersionConflict:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-NEW-VERSION-CONFLICT");
  	  case ResponseErrorCode.PlanDescriptionTemplateCanNotRemove:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-DESCRIPTION-TEMPLATE-CAN-NOT-REMOVE");
  	  case ResponseErrorCode.TenantTampering:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.TENANT-TAMPERING");
  	  case ResponseErrorCode.TenantConfigurationTypeCanNotChange:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.TENANT-CONFIGURATION-TYPE-CAN-NOT-CHANGE");
  	  case ResponseErrorCode.MultipleTenantConfigurationTypeNotAllowed:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.MULTIPLE-TENANT-CONFIGURATION-TYPE-NOT-ALLOWED");
  	  case ResponseErrorCode.TenantCodeExists:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.TENANT-CODE-EXISTS");
  	  case ResponseErrorCode.PlanNewVersionAlreadyCreatedDraft:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-NEW-VERSION-ALREADY-CREATED-DRAFT");
	  case ResponseErrorCode.DescriptionTemplateInactiveUser:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-INACTIVE-USER");
	  case ResponseErrorCode.DescriptionTemplateMissingUserContactInfo:
		return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-MISSING-USER-CONTACT-INFO");
	  case ResponseErrorCode.PlanInactiveUser:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-INACTIVE-USER");
	  case ResponseErrorCode.PlanMissingUserContactInfo:
		return language.instant("GENERAL.BACKEND-ERRORS.PLAN-MISSING-USER-CONTACT-INFO");
  	  case ResponseErrorCode.ImportDescriptionWithoutPlanDescriptionTemplate:
		return language.instant("GENERAL.BACKEND-ERRORS.IMPORT-DESCRIPTION-WITHOUT-PLAN-DESCRIPTION-TEMPLATE");
  	  case ResponseErrorCode.InvalidApiKey:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.INVALID-API-KEY");
  	  case ResponseErrorCode.DuplicatePlanUser:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.DUPLICATE-PLAN-USER");
  	  case ResponseErrorCode.StaleApiKey:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.STALE-API-KEY");
  	  case ResponseErrorCode.SensitiveInfo:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.SENSITIVE-INFO");
  	  case ResponseErrorCode.NonPersonPrincipal:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.NON-PERSON-PRINCIPAL");
  	  case ResponseErrorCode.BlockingConsent:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.BLOCKING-CONSENT");
  	  case ResponseErrorCode.SingleTenantConfigurationPerTypeSupported:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.SINGLE-TENANT-CONFIGURATION-PER-TYPE-SUPPORTED");
  	  case ResponseErrorCode.IncompatibleTenantConfigurationTypes:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.INCOMPATIBLE-TENANT-CONFIGURATION-TYPES");
  	  case ResponseErrorCode.MissingTotpToken:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.MISSING-TOTP-TOKEN");
  	  case ResponseErrorCode.OverlappingTenantConfigurationNotifierList:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.OVERLAPPING-TENANT-CONFIGURATION-NOTIFIER-LIST");
  	  case ResponseErrorCode.DescriptionTemplateNewVersionAlreadyCreatedDraft:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-NEW-VERSION-ALREADY-CREATED-DRAFT");
  	  case ResponseErrorCode.PlanBlueprintNewVersionAlreadyCreatedDraft:
  	  	return language.instant("GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-NEW-VERSION-ALREADY-CREATED-DRAFT");
  	  case ResponseErrorCode.ReferenceTypeCodeExists:
		return language.instant("GENERAL.BACKEND-ERRORS.REFERENCE-TYPE-CODE-EXISTS");
  	  case ResponseErrorCode.PrefillingSourceCodeExists:
		return language.instant("GENERAL.BACKEND-ERRORS.PREFILLING-SOURCE-CODE-EXISTS");
	  case ResponseErrorCode.InviteUserAlreadyConfirmed:
		return language.instant("GENERAL.BACKEND-ERRORS.INVITE-USER-ALREADY-CONFIRMED");
	  case ResponseErrorCode.RequestHasExpired:
		return language.instant("GENERAL.BACKEND-ERRORS.REQUEST-HAS-EXPIRED");
	  case ResponseErrorCode.MaxDescriptionsExceeded:
		return language.instant("GENERAL.BACKEND-ERRORS.MAX-DESCRIPTION-EXCEEDED");
	  case ResponseErrorCode.UsageLimitException:
		return language.instant("GENERAL.BACKEND-ERRORS.USAGE-LIMIT-EXCEPTION");
	  case ResponseErrorCode.UsageLimitMetricAlreadyExists:
		return language.instant("GENERAL.BACKEND-ERRORS.USAGE-LIMIT-METRIC-ALLREADY-EXISTS");
	  case ResponseErrorCode.DescriptionTemplateTypeCodeExists:
		return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-TYPE-CODE-EXISTS");
	  case ResponseErrorCode.descriptionTemplateTypeImportNotFound:
		return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-TYPE-IMPORT-NOT-FOUND");
	  case ResponseErrorCode.referenceTypeImportNotFound:
		return language.instant("GENERAL.BACKEND-ERRORS.REFERENCE-TYPE-IMPORT-NOT-FOUND");
	  case ResponseErrorCode.descriptionTemplateCodeExists:
		return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-CODE-EXISTS");
	  case ResponseErrorCode.descriptionTemplateImportNotFound:
		return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-IMPORT-NOT-FOUND");
	  case ResponseErrorCode.planBlueprintCodeExists:
		return language.instant("GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-CODE-EXISTS");
	  case ResponseErrorCode.blueprintDescriptionTemplateImportDraft:
		return language.instant("GENERAL.BACKEND-ERRORS.BLUEPRINT-DESCRIPTION-TEMPLATE-IMPORT-DRAFT");
	  case ResponseErrorCode.planDescriptionTemplateImportDraft:
		return language.instant("GENERAL.BACKEND-ERRORS.PLAN-DESCRIPTION-TEMPLATE-IMPORT-DRAFT");
	  case ResponseErrorCode.descriptionTemplateTypeImportDraft:
		return language.instant("GENERAL.BACKEND-ERRORS.DESCRIPTION-TEMPLATE-TYPE-IMPORT-DRAFT");
	  case ResponseErrorCode.planBlueprintImportDraft:
		return language.instant("GENERAL.BACKEND-ERRORS.PLAN-BLUEPRINT-IMPORT-DRAFT");
	  case ResponseErrorCode.missingTenantRole:
		return language.instant("GENERAL.BACKEND-ERRORS.MISSING-TENANT-ROLE");
	  case ResponseErrorCode.missingGlobalRole:
		return language.instant("GENERAL.BACKEND-ERRORS.MISSING-GLOBAL-ROLE");
      case ResponseErrorCode.userProfileInactive: 
        return language.instant("GENERAL.BACKEND-ERRORS.CANNOT-PERFORM-ACTION-ON-USER");
	  case ResponseErrorCode.planInvitationAlreadyConfirmed: 
        return language.instant("GENERAL.BACKEND-ERRORS.PLAN-INVITATION-ALREADY-CONFIRMED");
	  case ResponseErrorCode.anotherUserToken: 
        return language.instant("GENERAL.BACKEND-ERRORS.ANOTHER-USER-TOKEN");
	  case ResponseErrorCode.tokenNotExist: 
        return language.instant("GENERAL.BACKEND-ERRORS.TOKEN-NOT-EXIST");
  	  default:
  	  	return language.instant("GENERAL.SNACK-BAR.NOT-FOUND");
  	}
  }
}
