import { TenantConfigurationType } from "@app/core/common/enum/tenant-configuration-type";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { StorageFile } from "../storage-file/storage-file";
import { Guid } from "@common/types/guid";

export interface TenantConfiguration extends BaseEntity{
	type?: TenantConfigurationType;
	cssColors?: CssColorsTenantConfiguration;
	defaultUserLocale?: DefaultUserLocaleTenantConfiguration;
	depositPlugins?: DepositTenantConfiguration;
	fileTransformerPlugins?: FileTransformerTenantConfiguration;
	logo?: LogoTenantConfiguration;
}

export interface CssColorsTenantConfiguration{
	primaryColor: string;
	primaryColor2: string;
	primaryColor3: string;
	secondaryColor: string;
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
export interface LogoTenantConfiguration{
	storageFile: StorageFile;
}
//persist

export interface TenantConfigurationPersist extends BaseEntityPersist{
	type: TenantConfigurationType;
	cssColors?: CssColorsTenantConfigurationPersist;
	defaultUserLocale?: DefaultUserLocaleTenantConfigurationPersist;
	depositPlugins?: DepositTenantConfigurationPersist;
	fileTransformerPlugins?: FileTransformerTenantConfigurationPersist;
	logo?: LogoTenantConfigurationPersist;
}

export interface CssColorsTenantConfigurationPersist{
	primaryColor: string;
	primaryColor2: string;
	primaryColor3: string;
	secondaryColor: string;
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
export interface LogoTenantConfigurationPersist{
	storageFileId: Guid;
}
