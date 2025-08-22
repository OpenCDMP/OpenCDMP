import { DepositConfigurationStatus } from "@app/core/common/enum/deposit-configuration-status";
import { ConfigurationField } from "../plugin-configuration/plugin-configuration";
import { PluginType } from "@app/core/common/enum/plugin-type";

export class DepositConfiguration {
	depositType: DepositConfigurationStatus;
    repositoryId: string;
    repositoryAuthorizationUrl: string;
    repositoryRecordUrl: string;
    repositoryClientId: string;
    redirectUri: string;
    hasLogo: boolean;
    configurationFields?: ConfigurationField[];
    userConfigurationFields?: ConfigurationField[];
    pluginType?: PluginType;
}
