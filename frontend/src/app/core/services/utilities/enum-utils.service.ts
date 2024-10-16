import { Injectable } from '@angular/core';
import { AnnotationProtectionType } from '@app/core/common/enum/annotation-protection-type.enum';
import { DescriptionStatusEnum } from '@app/core/common/enum/description-status';
import { DescriptionTemplateExternalSelectAuthType } from '@app/core/common/enum/description-template-external-select-auth-type';
import { DescriptionTemplateExternalSelectHttpMethodType } from '@app/core/common/enum/description-template-external-select-http-method-type';
import { DescriptionTemplateFieldDataExternalDatasetType } from '@app/core/common/enum/description-template-field-data-external-dataset-type';
import { DescriptionTemplateFieldType } from '@app/core/common/enum/description-template-field-type';
import { DescriptionTemplateStatus } from '@app/core/common/enum/description-template-status';
import { DescriptionTemplateTypeStatus } from '@app/core/common/enum/description-template-type-status';
import { PlanAccessType } from '@app/core/common/enum/plan-access-type';
import { PlanBlueprintFieldCategory } from '@app/core/common/enum/plan-blueprint-field-category';
import { PlanBlueprintStatus } from '@app/core/common/enum/plan-blueprint-status';
import { PlanBlueprintSystemFieldType } from '@app/core/common/enum/plan-blueprint-system-field-type';
import { PlanUserRole } from '@app/core/common/enum/plan-user-role';
import { PlanUserType } from '@app/core/common/enum/plan-user-type';
import { ExternalFetcherApiHTTPMethodType } from '@app/core/common/enum/external-fetcher-api-http-method-type';
import { ExternalFetcherSourceType } from '@app/core/common/enum/external-fetcher-source-type';
import { IsActive } from '@app/core/common/enum/is-active.enum';
import { LockTargetType } from '@app/core/common/enum/lock-target-type';
import { PrefillingSourceSystemTargetType } from '@app/core/common/enum/prefilling-source-system-target-type';
import { RecentActivityOrder } from '@app/core/common/enum/recent-activity-order';
import { ReferenceFieldDataType } from '@app/core/common/enum/reference-field-data-type';
import { ReferenceSourceType } from '@app/core/common/enum/reference-source-type';
import { RoleOrganizationType } from '@app/core/common/enum/role-organization-type';
import { SupportiveMaterialFieldType } from '@app/core/common/enum/supportive-material-field-type';
import { UserDescriptionTemplateRole } from '@app/core/common/enum/user-description-template-role';
import { TranslateService } from '@ngx-translate/core';
import { AppRole } from '../../common/enum/app-role';
import { PlanBlueprintExtraFieldDataType } from '../../common/enum/plan-blueprint-field-type';
import { PlanStatusEnum } from '../../common/enum/plan-status';
import { ValidationType } from '../../common/enum/validation-type';
import { UsageLimitTargetMetric } from '@app/core/common/enum/usage-limit-target-metric';
import { UsageLimitPeriodicityRange } from '@app/core/common/enum/usage-limit-periodicity-range';

@Injectable()
export class EnumUtils {
	constructor(private language: TranslateService) { }

	public getEnumValues<T>(T): Array<T> {

		//getting all numeric values
		const numericValues: any = Object.keys(T).map(key => T[key]).filter(value => typeof (value) === 'number');
		if (numericValues.length > 0) { return numericValues; }

		//getting all string values
		const stringValues: any = Object.keys(T).map(key => T[key]).filter(value => typeof (value) === 'string');
		if (stringValues.length > 0) { return stringValues; }

		return [];
	}
	

	toIsActiveString(status: IsActive): string {
		switch (status) {
			case IsActive.Active: return this.language.instant('TYPES.IS-ACTIVE.ACTIVE');
			case IsActive.Inactive: return this.language.instant('TYPES.IS-ACTIVE.INACTIVE');
		}
	}

	toAppRoleString(status: AppRole): string {
		switch (status) {
			case AppRole.Admin: return this.language.instant('TYPES.APP-ROLE.ADMIN');
			case AppRole.InstallationAdmin: return this.language.instant('TYPES.APP-ROLE.INSTALLATION-ADMIN');
			case AppRole.User: return this.language.instant('TYPES.APP-ROLE.USER');
			case AppRole.TenantAdmin: return this.language.instant('TYPES.APP-ROLE.TENANT-ADMIN');
			case AppRole.TenantUser: return this.language.instant('TYPES.APP-ROLE.TENANT-USER');
			case AppRole.TenantPlanManager: return this.language.instant('TYPES.APP-ROLE.TENANT-PLAN-MANAGER');
			case AppRole.TenantConfigManager: return this.language.instant('TYPES.APP-ROLE.TENANT-CONFIG-MANAGER');
		}
	}

	toPlanStatusString(status: PlanStatusEnum): string {
		switch (status) {
			case PlanStatusEnum.Draft: return this.language.instant('TYPES.PLAN.DRAFT');
			case PlanStatusEnum.Finalized: return this.language.instant('TYPES.PLAN.FINALISED');
		}
	}

	toDescriptionTemplateFieldValidationTypeString(status: ValidationType): string {
		switch (status) {
			case ValidationType.None: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-VALIDATION-TYPE.NONE');
			case ValidationType.Required: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-VALIDATION-TYPE.REQUIRED');
		}
	}

	toRoleOrganizationString(status: RoleOrganizationType): string {
		switch (status) {
			case RoleOrganizationType.Faculty: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.FACULTY');
			case RoleOrganizationType.Librarian: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.LIBRARIAN');
			case RoleOrganizationType.Researcher: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.RESEARCHER');
			case RoleOrganizationType.Student: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.STUDENT');
			case RoleOrganizationType.EarlyCareerResearcher: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.EARLY-CAREER-RESEARCHER');
			case RoleOrganizationType.ResearchAdministrator: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.RESEARCH-ADMINISTRATOR');
			case RoleOrganizationType.RepositoryManager: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.REPOSITORY-MANAGER');
			case RoleOrganizationType.ResearchInfrastructure: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.RESEARCH-INFRASTRUCTURE');
			case RoleOrganizationType.ServiceProvider: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.SERVICE-PROVIDER');
			case RoleOrganizationType.Publisher: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.PUBLISHER');
			case RoleOrganizationType.ResearchFunder: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.RESEARCH-FUNDER');
			case RoleOrganizationType.Policymaker: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.POLICY-MAKER');
			case RoleOrganizationType.SMEIndustry: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.SME-INDUSTRY');
			case RoleOrganizationType.Other: return this.language.instant('USER-PROFILE.ROLE-ORGANIZATION.OTHER');
		}
	}

	toDescriptionTemplateTypeStatusString(status: DescriptionTemplateTypeStatus): string {
		switch (status) {
			case DescriptionTemplateTypeStatus.Draft: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-TYPE-STATUS.DRAFT');
			case DescriptionTemplateTypeStatus.Finalized: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-TYPE-STATUS.FINALIZED');
		}
	}

	toDescriptionTemplateStatusString(status: DescriptionTemplateStatus): string {
		switch (status) {
			case DescriptionTemplateStatus.Draft: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-STATUS.DRAFT');
			case DescriptionTemplateStatus.Finalized: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-STATUS.FINALIZED');
		}
	}

	toPlanBlueprintStatusString(status: PlanBlueprintStatus): string {
		switch (status) {
			case PlanBlueprintStatus.Draft: return this.language.instant('TYPES.PLAN-BLUEPRINT-STATUS.DRAFT');
			case PlanBlueprintStatus.Finalized: return this.language.instant('TYPES.PLAN-BLUEPRINT-STATUS.FINALIZED');
		}
	}

	toPlanBlueprintSystemFieldTypeString(status: PlanBlueprintSystemFieldType): string {
		switch (status) {
			case PlanBlueprintSystemFieldType.Title: return this.language.instant('TYPES.PLAN-BLUEPRINT-SYSTEM-FIELD-TYPE.TITLE');
			case PlanBlueprintSystemFieldType.Description: return this.language.instant('TYPES.PLAN-BLUEPRINT-SYSTEM-FIELD-TYPE.DESCRIPTION');
			case PlanBlueprintSystemFieldType.Language: return this.language.instant('TYPES.PLAN-BLUEPRINT-SYSTEM-FIELD-TYPE.LANGUAGE');
			case PlanBlueprintSystemFieldType.Contact: return this.language.instant('TYPES.PLAN-BLUEPRINT-SYSTEM-FIELD-TYPE.CONTACT');
			case PlanBlueprintSystemFieldType.AccessRights: return this.language.instant('TYPES.PLAN-BLUEPRINT-SYSTEM-FIELD-TYPE.ACCESS_RIGHTS');
			case PlanBlueprintSystemFieldType.User: return this.language.instant('TYPES.PLAN-BLUEPRINT-SYSTEM-FIELD-TYPE.USER');
		}
	}

	toPlanBlueprintExtraFieldDataTypeString(status: PlanBlueprintExtraFieldDataType): string {
		switch (status) {
			case PlanBlueprintExtraFieldDataType.Text: return this.language.instant('TYPES.PLAN-BLUEPRINT-EXTRA-FIELD-DATA-TYPE.TEXT');
			case PlanBlueprintExtraFieldDataType.RichText: return this.language.instant('TYPES.PLAN-BLUEPRINT-EXTRA-FIELD-DATA-TYPE.RICH-TEXT');
			case PlanBlueprintExtraFieldDataType.Date: return this.language.instant('TYPES.PLAN-BLUEPRINT-EXTRA-FIELD-DATA-TYPE.DATE');
			case PlanBlueprintExtraFieldDataType.Number: return this.language.instant('TYPES.PLAN-BLUEPRINT-EXTRA-FIELD-DATA-TYPE.NUMBER');
		}
	}

	toDescriptionTemplateFieldTypeString(status: DescriptionTemplateFieldType): string {
		switch (status) {
			case DescriptionTemplateFieldType.SELECT: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.SELECT');
			case DescriptionTemplateFieldType.BOOLEAN_DECISION: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.BOOLEAN-DECISION');
			case DescriptionTemplateFieldType.RADIO_BOX: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.RADIO-BOX');
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_PLANS: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.INTERNAL-PLAN-ENTITIES-PLANS');
			case DescriptionTemplateFieldType.INTERNAL_ENTRIES_DESCRIPTIONS: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.INTERNAL-PLAN-ENTITIES-DESCRIPTIONS');
			case DescriptionTemplateFieldType.CHECK_BOX: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.CHECKBOX');
			case DescriptionTemplateFieldType.FREE_TEXT: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.FREE-TEXT');
			case DescriptionTemplateFieldType.TEXT_AREA: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.TEXT-AREA');
			case DescriptionTemplateFieldType.RICH_TEXT_AREA: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.RICH-TEXT-AREA');
			case DescriptionTemplateFieldType.UPLOAD: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.UPLOAD');
			case DescriptionTemplateFieldType.DATE_PICKER: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.DATE-PICKER');
			case DescriptionTemplateFieldType.TAGS: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.TAGS');
			case DescriptionTemplateFieldType.REFERENCE_TYPES: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.REFERENCE-TYPES');
			case DescriptionTemplateFieldType.DATASET_IDENTIFIER: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.DATASET-IDENTIFIER');
			case DescriptionTemplateFieldType.VALIDATION: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-TYPE.VALIDATION');
		}
	}

	toExternalFetcherSourceTypeString(status: ExternalFetcherSourceType): string {
		switch (status) {
			case ExternalFetcherSourceType.API: return this.language.instant('TYPES.EXTERNAL-FETCHER-SOURCE-TYPE.API');
			case ExternalFetcherSourceType.STATIC: return this.language.instant('TYPES.EXTERNAL-FETCHER-SOURCE-TYPE.STATIC');
		}
	}

	toReferenceFieldDataTypeString(status: ReferenceFieldDataType): string {
		switch (status) {
			case ReferenceFieldDataType.Text: return this.language.instant('TYPES.REFERENCE-FIELD-DATA-TYPE.TEXT');
			case ReferenceFieldDataType.Date: return this.language.instant('TYPES.REFERENCE-FIELD-DATA-TYPE.DATE');
		}
	}


	toExternalFetcherApiHTTPMethodTypeString(status: ExternalFetcherApiHTTPMethodType): string {
		switch (status) {
			case ExternalFetcherApiHTTPMethodType.GET: return this.language.instant('TYPES.EXTERNAL-FETCHER-API-HTTP-METHOD-TYPE.GET');
			case ExternalFetcherApiHTTPMethodType.POST: return this.language.instant('TYPES.EXTERNAL-FETCHER-API-HTTP-METHOD-TYPE.POST');
		}
	}

	toUserDescriptionTemplateRoleString(status: UserDescriptionTemplateRole): string {
		switch (status) {
			case UserDescriptionTemplateRole.Member: return this.language.instant('TYPES.USER-DESCRIPTION-TEMPLATE-ROLE.MEMBER');
			case UserDescriptionTemplateRole.Owner: return this.language.instant('TYPES.USER-DESCRIPTION-TEMPLATE-ROLE.OWNER');
		}
	}

	toReferenceSourceTypeString(status: ReferenceSourceType): string {
		switch (status) {
			case ReferenceSourceType.Internal: return this.language.instant('TYPES.REFERENCE-SOURCE-TYPE.INTERNAL');
			case ReferenceSourceType.External: return this.language.instant('TYPES.REFERENCE-SOURCE-TYPE.EXTERNAL');
		}
	}

	toSupportiveMaterialTypeString(status: SupportiveMaterialFieldType): string {
		switch (status) {
			case SupportiveMaterialFieldType.Faq: return this.language.instant('FOOTER.FAQ');
			case SupportiveMaterialFieldType.About: return this.language.instant('FOOTER.ABOUT');
			case SupportiveMaterialFieldType.Glossary: return this.language.instant('FOOTER.GLOSSARY');
			case SupportiveMaterialFieldType.TermsOfService: return this.language.instant('FOOTER.TERMS-OF-SERVICE');
			case SupportiveMaterialFieldType.UserGuide: return this.language.instant('FOOTER.GUIDE');
			case SupportiveMaterialFieldType.CookiePolicy: return this.language.instant('FOOTER.COOKIE-POLICY');
		}
	}

	toDescriptionStatusString(status: DescriptionStatusEnum): string {
		switch (status) {
			case DescriptionStatusEnum.Draft: return this.language.instant('TYPES.DESCRIPTION-STATUS.DRAFT');
			case DescriptionStatusEnum.Finalized: return this.language.instant('TYPES.DESCRIPTION-STATUS.FINALIZED');
			case DescriptionStatusEnum.Canceled: return this.language.instant('TYPES.DESCRIPTION-STATUS.CANCELED');
		}
	}

	toPlanUserRolesString(roles: PlanUserRole[]): string { 
		const distinctRoles =[...new Set(roles)]
		return distinctRoles?.map(x => this.toPlanUserRoleString(x)).join(', ') 
	}
	
	toPlanUserRoleString(role: PlanUserRole): string {
		switch (role) {
			case PlanUserRole.Owner: return this.language.instant('TYPES.PLAN-USER-ROLE.OWNER');
			case PlanUserRole.Viewer: return this.language.instant('TYPES.PLAN-USER-ROLE.VIEWER');
			case PlanUserRole.DescriptionContributor: return this.language.instant('TYPES.PLAN-USER-ROLE.DESCRIPTION-CONTRIBUTOR');
			case PlanUserRole.Reviewer: return this.language.instant('TYPES.PLAN-USER-ROLE.REVIEWER');
		}
	}

	toRecentActivityOrderString(status: RecentActivityOrder): string {
		switch (status) {
			case RecentActivityOrder.Label: return this.language.instant('TYPES.RECENT-ACTIVITY-ORDER.LABEL');
			case RecentActivityOrder.UpdatedAt: return this.language.instant('TYPES.RECENT-ACTIVITY-ORDER.MODIFIED');
			case RecentActivityOrder.Status: return this.language.instant('TYPES.RECENT-ACTIVITY-ORDER.STATUS');
			case RecentActivityOrder.PublishedAt: return this.language.instant('TYPES.RECENT-ACTIVITY-ORDER.PUBLISHED-AT');
		}
	}

	public toPlanAccessTypeString(value: PlanAccessType): string {
		switch (value) {
			case PlanAccessType.Public: return this.language.instant('TYPES.PLAN-ACCESS-TYPE.PUBLIC');
			case PlanAccessType.Restricted: return this.language.instant('TYPES.PLAN-ACCESS-TYPE.RESTRICTED');
		}
	}

	public toDescriptionTemplateFieldDataExternalDatasetTypeString(value: DescriptionTemplateFieldDataExternalDatasetType): string {
		switch (value) {
			case DescriptionTemplateFieldDataExternalDatasetType.ReusedDataset: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-EXTERNAL-DATASET-TYPE.REUSED');
			case DescriptionTemplateFieldDataExternalDatasetType.ProducedDataset: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-EXTERNAL-DATASET-TYPE.PRODUCED');
			case DescriptionTemplateFieldDataExternalDatasetType.Other: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-FIELD-EXTERNAL-DATASET-TYPE.OTHER');
		}
	}

	public toPlanUserTypeString(value: PlanUserType): string {
		switch (value) {
			case PlanUserType.Internal: return this.language.instant('TYPES.PLAN-USER-TYPE.INTERNAL');
			case PlanUserType.External: return this.language.instant('TYPES.PLAN-USER-TYPE.EXTERNAL');
		}
	}

	public toDescriptionTemplateExternalSelectHttpMethodTypeString(value: DescriptionTemplateExternalSelectHttpMethodType): string {
		switch (value) {
			case DescriptionTemplateExternalSelectHttpMethodType.GET: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-EXTERNAL-SELECT-HTTP-METHOD-TYPE.GET');
			case DescriptionTemplateExternalSelectHttpMethodType.POST: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-EXTERNAL-SELECT-HTTP-METHOD-TYPE.POST');
		}
	}

	public toDescriptionTemplateExternalSelectAuthTypeString(value: DescriptionTemplateExternalSelectAuthType): string {
		switch (value) {
			case DescriptionTemplateExternalSelectAuthType.BEARER: return this.language.instant('TYPES.DESCRIPTION-TEMPLATE-EXTERNAL-SELECT-AUTH-TYPE.BEARER');
		}
	}

	public toPlanBlueprintFieldCategoryString(value: PlanBlueprintFieldCategory): string {
		switch (value) {
			case PlanBlueprintFieldCategory.System: return this.language.instant('TYPES.PLAN-BLUEPRINT-FIELD-CATEGORY.SYSTEM');
			case PlanBlueprintFieldCategory.Extra: return this.language.instant('TYPES.PLAN-BLUEPRINT-FIELD-CATEGORY.EXTRA');
			case PlanBlueprintFieldCategory.ReferenceType: return this.language.instant('TYPES.PLAN-BLUEPRINT-FIELD-CATEGORY.REFERENCE-TYPE');
		}
	}

	public toPrefillingSourceSystemTargetTypeString(value: PrefillingSourceSystemTargetType): string {
		switch (value) {
			case PrefillingSourceSystemTargetType.Label: return this.language.instant('TYPES.PREFILLING-SOURCE-SYSTEM-TARGET-TYPE.LABEL');
			case PrefillingSourceSystemTargetType.Description: return this.language.instant('TYPES.PREFILLING-SOURCE-SYSTEM-TARGET-TYPE.DESCRIPTION');
			case PrefillingSourceSystemTargetType.Tags: return this.language.instant('TYPES.PREFILLING-SOURCE-SYSTEM-TARGET-TYPE.TAGS');
		}
	}

	public toAnnotationProtectionTypeString(value: AnnotationProtectionType): string {
		switch (value) {
			case AnnotationProtectionType.Private: return this.language.instant('TYPES.ANNOTATION-PROTECTION-TYPE.PRIVATE');
			case AnnotationProtectionType.EntityAccessors: return this.language.instant('TYPES.ANNOTATION-PROTECTION-TYPE.ENTITY-ACCESSORS');
			default: return '';
		}
	}

	public toLockTargetTypeString(status: LockTargetType): string {
		switch (status) {
			case LockTargetType.Plan: return this.language.instant('TYPES.LOCK-TARGET-TYPE.PLAN');
			case LockTargetType.Description: return this.language.instant('TYPES.LOCK-TARGET-TYPE.DESCRIPTION');
			case LockTargetType.PlanBlueprint: return this.language.instant('TYPES.LOCK-TARGET-TYPE.PLAN-BLUEPRINT');
			case LockTargetType.DescriptionTemplate: return this.language.instant('TYPES.LOCK-TARGET-TYPE.DESCRIPTION-TEMPLATE');
		}
	}

	public toUsageLimitTargetMetricString(value: UsageLimitTargetMetric): string {
		switch (value) {
			case UsageLimitTargetMetric.USER_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.USER-COUNT');
			case UsageLimitTargetMetric.PLAN_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.PLAN-COUNT');
			case UsageLimitTargetMetric.BLUEPRINT_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.BLUEPRINT-COUNT');
			case UsageLimitTargetMetric.DESCRIPTION_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.DESCRIPTION-COUNT');
			case UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.DESCRIPTION-TEMPLATE-COUNT');
			case UsageLimitTargetMetric.DESCRIPTION_TEMPLATE_TYPE_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.DESCRIPTION-TEMPLATE-TYPE-COUNT');
			case UsageLimitTargetMetric.PREFILLING_SOURCES_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.PREFILLING-SOURCES-COUNT');
			case UsageLimitTargetMetric.REFERENCE_TYPE_COUNT: return this.language.instant('TYPES.USAGE-LIMIT-TARGET-METRIC.REFERENCE-TYPE-COUNT');
			default: return '';
		}
	}

	public toUsageLimitPeriodicityRangeString(value: UsageLimitPeriodicityRange): string {
		switch (value) {
			case UsageLimitPeriodicityRange.Monthly: return this.language.instant('TYPES.USAGE-LIMIT-PERIODICITY-RANGE.MONTHLY');
			case UsageLimitPeriodicityRange.Yearly: return this.language.instant('TYPES.USAGE-LIMIT-PERIODICITY-RANGE.YEARLY');
			default: return '';
		}
	}

}
