import { FileTransformerEntityType } from "@app/core/common/enum/file-transformer-entity-type";
import { PluginType } from "@app/core/common/enum/plugin-type";
import { ConfigurationField } from "../plugin-configuration/plugin-configuration";

export interface FileTransformerConfiguration {
	fileTransformerId: string;
	exportVariants: FileFormat[];
	exportEntityTypes: FileTransformerEntityType[];
	importVariants?: FileFormat[];
	importEntityTypes?: FileTransformerEntityType[];
	configurationFields?: ConfigurationField[];
	userConfigurationFields?: ConfigurationField[];
	pluginType?: PluginType;
}

export interface FileFormat {
	format: string;
	hasLogo: boolean;
	icon: string;
}
