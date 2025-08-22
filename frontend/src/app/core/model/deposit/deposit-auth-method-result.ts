import { DepositAuthMethod } from "@app/core/common/enum/deposit-auth-method";

export class DepositAuthMethodResult {
	hasMyAccountValue: boolean;
    depositAuthInfoTypes: DepositAuthMethod[];
}
