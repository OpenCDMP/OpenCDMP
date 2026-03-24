import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { PlanUpdateRequestStatus } from '../common/enum/plan-update-request-status.enum';
import { PlanUpdateRequestActionType } from '../common/enum/plan-update-request-action-type.enum';

export class PlanUpdateRequestLookup extends Lookup implements PlanUpdateRequestFilter {
	ids: Guid[];
	excludedIds: Guid[];
	isActive: IsActive[];
	planIds: Guid[];
	approvedByIds: Guid[];
	actionTypes: PlanUpdateRequestActionType[];
	status: PlanUpdateRequestStatus[];

	constructor() {
		super();
	}
}

export interface PlanUpdateRequestFilter {
	ids: Guid[];
	excludedIds: Guid[];
	isActive: IsActive[];
	planIds: Guid[];
	approvedByIds: Guid[];
	actionTypes: PlanUpdateRequestActionType[];
	status: PlanUpdateRequestStatus[];
}
