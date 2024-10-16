import { Guid } from "@common/types/guid";

export class DepositRequest {
    repositoryId: string;
    planId: Guid;
    authorizationCode: String;
    project: DepositRequestFields;
}

export interface DepositRequestFields {
    fields: string[];
}

export class DepositAuthenticateRequest {
    repositoryId: string;
    code: string;
}
