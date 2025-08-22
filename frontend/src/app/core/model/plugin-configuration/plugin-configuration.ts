
import { Guid } from "@common/types/guid";
import { StorageFile } from "../storage-file/storage-file";
import { PluginType } from "@app/core/common/enum/plugin-type";
import { PluginDataType } from "@app/core/common/enum/plugin-data-type";
import { PluginEntityType } from "@app/core/common/enum/plugin-entity-type";

export interface PluginConfigurationConfigurationBase {
	pluginCode: string;
	pluginType: PluginType;
}
export interface PluginConfiguration extends PluginConfigurationConfigurationBase{
	fields: PluginConfigurationField[];
}

export interface PluginConfigurationUser extends PluginConfigurationConfigurationBase{
	userFields: PluginConfigurationUserField[];
}

export interface PluginConfigurationFieldBase {
	code: string;
	fileValue: StorageFile;
	textValue: string;
}

export interface PluginConfigurationField extends PluginConfigurationFieldBase{
}

export interface PluginConfigurationUserField extends PluginConfigurationFieldBase{
}

//
// Persist
//
export interface PluginConfigurationPersistBase {
	pluginCode: string;
	pluginType: PluginType;
}

export interface PluginConfigurationPersist extends PluginConfigurationPersistBase{
	fields: PluginConfigurationFieldPersist[];
}

export interface PluginConfigurationUserPersist extends PluginConfigurationPersistBase{
	userFields: PluginConfigurationUserFieldPersist[];
}

export interface PluginConfigurationFieldPersistBase {
	code: string;
	fileValue: Guid;
	textValue: string;
}

export interface PluginConfigurationFieldPersist extends PluginConfigurationFieldPersistBase{
}

export interface PluginConfigurationUserFieldPersist extends PluginConfigurationFieldPersistBase{
}

// Configuration Field
export interface ConfigurationField {
	code: string;
	type: PluginDataType;
	label: string;
	appliesTo: PluginEntityType[];
}

// PluginRepositoryConfiguration used to define data type in form group
export interface PluginRepositoryConfigurationBase {
	repositoryId: string;
	pluginType: PluginType;
	fields: ConfigurationField[];
}
export interface PluginRepositoryConfiguration extends PluginRepositoryConfigurationBase{
}

export interface PluginRepositoryUserConfiguration extends PluginRepositoryConfigurationBase{
}