import { DepositAuthMethod } from "@app/core/common/enum/deposit-auth-method";
import { Guid } from "@common/types/guid";
import { PreviewGroupValues, StoredPreviewField } from "./deposit-preview";

export class DepositRequest {
    repositoryId: string;
    planId: Guid;
    authorizationCode: string;
    depositAuthInfoType: DepositAuthMethod;
    project: DepositRequestFields;
    storedFields?: StoredPreviewField[];
    storedGroups?: PreviewGroupValues[];
    authFields?: DepositAuthField[];
    saveAuthFields?: boolean;
}

export interface DepositAuthField {
    code: string;
    textValue: string;
}

export interface DepositRequestFields {
    fields: string[];
}

export class DepositAuthenticateRequest {
    repositoryId: string;
    code: string;
}
