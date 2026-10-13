import { DepositConfigurationStatus } from "@app/core/common/enum/deposit-configuration-status";
import { ConfigurationField } from "../plugin-configuration/plugin-configuration";
import { PluginType } from "@app/core/common/enum/plugin-type";
import { DepositFileKind } from "@app/core/common/enum/deposit-file-kind";

export class DepositConfiguration {
	depositType: DepositConfigurationStatus;
    repositoryId: string;
    repositoryAuthorizationUrl: string;
    repositoryRecordUrl: string;
    repositoryClientId: string;
    redirectUri: string;
    hasLogo: boolean;
    supportsPreview: boolean;
    configurationFields?: ConfigurationField[];
    userConfigurationFields?: ConfigurationField[];
    pluginType?: PluginType;
    uploadedFiles?: DepositFileKind[];
    oauthScope?: string;
}
