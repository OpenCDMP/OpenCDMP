import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { StorageFile } from "../storage-file/storage-file";
import { Guid } from "@common/types/guid";
import { PlanBlueprint } from "../plan-blueprint/plan-blueprint";
import { DescriptionTemplate } from "../description-template/description-template";

export interface TenantConfiguration extends BaseEntity{
	type?: TenantConfigurationType;
	cssColors?: CssColorsTenantConfiguration;
	defaultUserLocale?: DefaultUserLocaleTenantConfiguration;
	depositPlugins?: DepositTenantConfiguration;
	fileTransformerPlugins?: FileTransformerTenantConfiguration;
	logo?: LogoTenantConfiguration;
	evaluatorPlugins?: EvaluatorTenantConfiguration;
	featuredEntities?: FeaturedEntities;
	defaultPlanBlueprint?: DefaultPlanBlueprintConfiguration;
}

export interface CssColorsTenantConfiguration{
	primaryColor: string;
	cssOverride: string;
}

export interface DefaultUserLocaleTenantConfiguration{
	timezone: string;
	language: string;
	culture: string;
}

export interface DepositTenantConfiguration{
	disableSystemSources: boolean;
	sources: DepositSource[];
}

export interface FileTransformerTenantConfiguration{
	disableSystemSources: boolean;
	sources: FileTransformerSource[];
}

export interface EvaluatorTenantConfiguration{
	disableSystemSources: boolean;
	sources: EvaluatorSource[];

}

export interface DepositSource{
	repositoryId: string;
	url: string;
	issuerUrl: string;
    clientId: string;
	clientSecret: string;
    scope: string;
    pdfTransformerId: string;
    rdaTransformerId: string;
}

export interface FileTransformerSource{
	transformerId: string;
	url: string;
	issuerUrl: string;
    clientId: string;
	clientSecret: string;
    scope: string;
}

export interface EvaluatorSource{
	evaluatorId: string;
	url: string;
	issuerUrl: string;
    clientId: string;
	clientSecret: string;
    scope: string;
}
export interface LogoTenantConfiguration{
	storageFile: StorageFile;
}

export interface FeaturedEntities{
	planBlueprints: PlanBlueprint[];
	descriptionTemplates: DescriptionTemplate[];
}

export interface DefaultPlanBlueprintConfiguration{
	groupId: Guid;
}
//persist

export interface TenantConfigurationPersist extends BaseEntityPersist{
	type: TenantConfigurationType;
	cssColors?: CssColorsTenantConfigurationPersist;
	defaultUserLocale?: DefaultUserLocaleTenantConfigurationPersist;
	depositPlugins?: DepositTenantConfigurationPersist;
	fileTransformerPlugins?: FileTransformerTenantConfigurationPersist;
	logo?: LogoTenantConfigurationPersist;
	evaluatorPlugins?: EvaluatorTenantConfigurationPersist;
	featuredEntities?: FeaturedEntitiesPersist;
	defaultPlanBlueprint?: DefaultPlanBlueprintConfigurationPersist;
}

export interface CssColorsTenantConfigurationPersist{
	primaryColor: string;
	cssOverride: string;
}

export interface DefaultUserLocaleTenantConfigurationPersist{
	timezone: string;
	language: string;
	culture: string;
}

export interface DepositTenantConfigurationPersist{
	disableSystemSources: boolean;
	sources: DepositSourcePersist[];
}

export interface FileTransformerTenantConfigurationPersist{
	disableSystemSources: boolean;
	sources: FileTransformerSourcePersist[];
}

export interface EvaluatorTenantConfigurationPersist{
	disableSystemSources: boolean;
	sources: EvaluatorSourcePersist[];
}

export interface DepositSourcePersist{
	repositoryId: string;
	url: string;
	issuerUrl: string;
    clientId: string;
	clientSecret: string;
    scope: string;
    pdfTransformerId: string;
    rdaTransformerId: string;
}

export interface FileTransformerSourcePersist{
	transformerId: string;
	url: string;
	issuerUrl: string;
    clientId: string;
	clientSecret: string;
    scope: string;
}

export interface EvaluatorSourcePersist{
	evaluatorId: string;
	url: string;
	issuerUrl: string;
    clientId: string;
	clientSecret: string;
    scope: string;
}
export interface LogoTenantConfigurationPersist{
	storageFileId: Guid;
}

export interface FeaturedEntitiesPersist{
	planBlueprints: PlanBlueprintPersist[];
	descriptionTemplates: DescriptionTemplatePersist[];
}

export interface PlanBlueprintPersist {
	groupId: Guid;
	ordinal: number;
}

export interface DescriptionTemplatePersist {
	groupId: Guid;
	ordinal: number;
}

export interface DefaultPlanBlueprintConfigurationPersist{
	groupId: Guid;
}