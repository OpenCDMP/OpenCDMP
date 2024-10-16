import { DepositConfigurationStatus } from "@app/core/common/enum/deposit-configuration-status";

export class DepositConfiguration {
	depositType: DepositConfigurationStatus;
    repositoryId: string;
    repositoryAuthorizationUrl: string;
    repositoryRecordUrl: string;
    repositoryClientId: string;
    redirectUri: string;
    hasLogo: boolean;
}
