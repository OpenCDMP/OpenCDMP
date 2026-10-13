
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
	secret?: boolean;
	/** For a secret: whether one is stored. Its value never reaches the browser. */
	secretSet?: boolean;
}

export interface PluginConfigurationUserField extends PluginConfigurationFieldBase{
	/** For a secret: whether one is stored. Its value never reaches the browser. */
	secretSet?: boolean;
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
	/** For a secret, textValue null keeps the stored value and '' removes it. */
	secret?: boolean;
}

export interface PluginConfigurationUserFieldPersist extends PluginConfigurationFieldPersistBase{
	/** For a secret, textValue null keeps the stored value and '' removes it. */
	secret?: boolean;
}

// Configuration Field
export interface ConfigurationField {
	code: string;
	type: PluginDataType;
	label: string;
	appliesTo: PluginEntityType[];
	options?: ConfigurationFieldOption[];
	authInfo?: boolean;
	/** What the plugin uses when the field is left empty: shown by editors, never stored. */
	defaultValue?: string;
	/** Heading editors group the field under; fields keep their declared order within it. */
	group?: string;
	description?: string;
	/** Where more options come from: "referenceTypes" adds one per reference type (value: code). */
	optionsSource?: string;
}

export interface ConfigurationFieldOption {
	label: string;
	value: string;
}

// PluginRepositoryConfiguration used to define data type in form group
export interface PluginRepositoryConfigurationBase {
	repositoryId: string;
	pluginType: PluginType;
	fields: ConfigurationField[];
}
export type PluginRepositoryConfiguration = PluginRepositoryConfigurationBase;

export type PluginRepositoryUserConfiguration = PluginRepositoryConfigurationBase;