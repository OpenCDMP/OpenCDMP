import { Guid } from '@common/types/guid';

export interface TransferExportRequest {
	includeAllReferenceTypes?: boolean;
	includeAllPrefillingSources?: boolean;
	includeAllDescriptionTemplateTypes?: boolean;
	includeAllPlanBlueprintTypes?: boolean;
	includeAllStatusesAndWorkflows?: boolean;
	includeAllLanguages?: boolean;
	includeSupportiveMaterial?: boolean;
	includeAllDescriptionTemplates?: boolean;
	descriptionTemplateIds?: Guid[];
	includeAllPlanBlueprints?: boolean;
	planBlueprintIds?: Guid[];
	includeAllPlans?: boolean;
	planIds?: Guid[];
}

export interface TenantCopyRequest extends TransferExportRequest {
	sourceTenantCode: string;
}

export interface TransferImportReport {
	applied: boolean;
	sourceTenantCode: string;
	types: TransferImportTypeReport[];
	warnings: string[];
}

export interface TransferImportTypeReport {
	entityType: string;
	total: number;
	toCreate: number;
	matchedById: number;
	matchedByKey: number;
	conflicts: number;
	messages: string[];
}

export interface TenantPurgeRequest {
	deletePlans?: boolean;
	deleteReferences?: boolean;
	deleteTags?: boolean;
	deleteDescriptionTemplates?: boolean;
	deletePlanBlueprints?: boolean;
	deleteReferenceTypes?: boolean;
	deletePrefillingSources?: boolean;
	deleteStatusesAndWorkflows?: boolean;
	deleteBlueprintTypes?: boolean;
	deleteTemplateTypes?: boolean;
	deleteStorageFiles?: boolean;
}

export interface TenantPurgeReport {
	applied: boolean;
	tenantCode: string;
	types: TenantPurgeTypeReport[];
	warnings: string[];
}

export interface TenantPurgeTypeReport {
	entityType: string;
	toDelete: number;
	kept: number;
}
