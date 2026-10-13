import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { StorageFile } from "../storage-file/storage-file";
import { Guid } from "@common/types/guid";
import { PlanBlueprint } from "../plan-blueprint/plan-blueprint";
import { DescriptionTemplate } from "../description-template/description-template";
import { PluginConfiguration, PluginConfigurationPersist } from "../plugin-configuration/plugin-configuration";
import { ReferenceType } from "../reference-type/reference-type";

export interface TenantConfiguration extends BaseEntity{
	type?: TenantConfigurationType;
	cssColors?: CssColorsTenantConfiguration;
	defaultUserLocale?: DefaultUserLocaleTenantConfiguration;
	depositPlugins?: DepositTenantConfiguration;
	fileTransformerPlugins?: FileTransformerTenantConfiguration;
	logo?: LogoTenantConfiguration;
	pluginConfiguration: PluginTenantConfiguration;
	evaluatorPlugins?: EvaluatorTenantConfiguration;
	featuredEntities?: FeaturedEntities;
	defaultPlanBlueprint?: DefaultPlanBlueprintConfiguration;
	viewPreferences?: ViewPreferencesConfiguration;
	termsAndConditions?: TermsAndConditionsTenantConfiguration;
	guidedTours?: GuidedToursTenantConfiguration;
}

export interface TermsAndConditionsTenantConfiguration {
	content: string;
	version: number;
}

/** Per-tenant switch for the guided ("what's new") tours. No stored configuration means enabled. */
export interface GuidedToursTenantConfiguration {
	enabled: boolean;
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
	/** Plugin ids (installation-provided or the tenant's own) switched off for this tenant. */
	disabledSourceIds?: string[];
	sources: DepositSource[];
}

export interface FileTransformerTenantConfiguration{
	disableSystemSources: boolean;
	/** Plugin ids (installation-provided or the tenant's own) switched off for this tenant. */
	disabledSourceIds?: string[];
	sources: FileTransformerSource[];
}

export interface EvaluatorTenantConfiguration{
	disableSystemSources: boolean;
	/** Plugin ids (installation-provided or the tenant's own) switched off for this tenant. */
	disabledSourceIds?: string[];
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
    maxInMemorySizeInBytes: number;
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

export interface PluginTenantConfiguration{
	pluginConfigurations: PluginConfiguration[];
}

export interface FeaturedEntities{
	planBlueprints: PlanBlueprint[];
	descriptionTemplates: DescriptionTemplate[];
}

export interface DefaultPlanBlueprintConfiguration{
	groupId: Guid;
}

export interface ViewPreferencesConfiguration {
	planPreferences: ViewPreference[];
	descriptionPreferences: ViewPreference[];
}

export interface ViewPreference {
	referenceType: ReferenceType;
	ordinal: number;
}
//persist

export interface TenantConfigurationPersist extends BaseEntityPersist{
	type: TenantConfigurationType;
	cssColors?: CssColorsTenantConfigurationPersist;
	defaultUserLocale?: DefaultUserLocaleTenantConfigurationPersist;
	depositPlugins?: DepositTenantConfigurationPersist;
	fileTransformerPlugins?: FileTransformerTenantConfigurationPersist;
	logo?: LogoTenantConfigurationPersist;
	pluginConfiguration?: PluginTenantConfigurationPersist;
	evaluatorPlugins?: EvaluatorTenantConfigurationPersist;
	featuredEntities?: FeaturedEntitiesPersist;
	defaultPlanBlueprint?: DefaultPlanBlueprintConfigurationPersist;
	viewPreferences?: ViewPreferencesConfigurationPersist;
	termsAndConditions?: TermsAndConditionsTenantConfigurationPersist;
	guidedTours?: GuidedToursTenantConfigurationPersist;
}

export interface TermsAndConditionsTenantConfigurationPersist {
	content: string;
	version: number;
}

export interface GuidedToursTenantConfigurationPersist {
	enabled: boolean;
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
	/** Plugin ids (installation-provided or the tenant's own) switched off for this tenant. */
	disabledSourceIds?: string[];
	sources: DepositSourcePersist[];
}

export interface FileTransformerTenantConfigurationPersist{
	disableSystemSources: boolean;
	/** Plugin ids (installation-provided or the tenant's own) switched off for this tenant. */
	disabledSourceIds?: string[];
	sources: FileTransformerSourcePersist[];
}

export interface EvaluatorTenantConfigurationPersist{
	disableSystemSources: boolean;
	/** Plugin ids (installation-provided or the tenant's own) switched off for this tenant. */
	disabledSourceIds?: string[];
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
    maxInMemorySizeInBytes: number;
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

export interface PluginTenantConfigurationPersist{
	pluginConfigurations: PluginConfigurationPersist[];
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

export interface ViewPreferencesConfigurationPersist {
	planPreferences: ViewPreferencePersist[];
	descriptionPreferences: ViewPreferencePersist[];
}

export interface ViewPreferencePersist {
	referenceTypeId: Guid;
	ordinal: number;
}
