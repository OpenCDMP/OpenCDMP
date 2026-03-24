import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";
import { Plan } from "../plan/plan";
import { User } from "../user/user";
import { PlanUpdateRequestActionType } from "@app/core/common/enum/plan-update-request-action-type.enum";
import { PlanUpdateRequestStatus } from "@app/core/common/enum/plan-update-request-status.enum";
import { DescriptionCommonModelConfig } from "../description/description-import";
import { Guid } from "@common/types/guid";


export interface PlanUpdateRequest extends BaseEntity {
	plan?: Plan;
	submitter?: User;
	actionType: PlanUpdateRequestActionType;
	status?: PlanUpdateRequestStatus;
	data?: any;
	requestAt?: Date;
	approvedBy?: User;
	approvedAt?: Date;
	sourceCode?: string;
}

//
// Persist
//
export interface PlanUpdateRequestPersist extends BaseEntityPersist {
	planId?: Guid;
	submitterId: Guid;
	actionType: PlanUpdateRequestActionType;
	status: PlanUpdateRequestStatus;
	data: any;
	sourceCode: string;
}

export interface PlanSuggestion {
	planUpdateRequestId?: Guid;
	blueprintId: Guid;
	descriptions: DescriptionCommonModelConfig[];
}
