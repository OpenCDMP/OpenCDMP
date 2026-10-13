import { DepositAuthMethod } from "@app/core/common/enum/deposit-auth-method";

export class DepositAuthMethodResult {
	hasMyAccountValue: boolean;
    depositAuthInfoTypes: DepositAuthMethod[];
	preselected?: 'own' | 'organisation';
	organisationAccountName?: string;
}
