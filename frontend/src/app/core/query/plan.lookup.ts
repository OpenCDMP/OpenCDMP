import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { PlanAccessType } from '../common/enum/plan-access-type';
import { PlanStatusEnum } from '../common/enum/plan-status';
import { PlanVersionStatus } from '../common/enum/plan-version-status';
import { IsActive } from '../common/enum/is-active.enum';
import { PlanDescriptionTemplateLookup } from './plan-description-template.lookup';
import { PlanUserLookup } from './plan-user.lookup';
import { PlanBlueprintLookup } from './plan-blueprint.lookup';
import { PlanReferenceLookup } from './reference.lookup';
import { TenantLookup } from './tenant.lookup';

export class PlanLookup extends Lookup implements PlanFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	versionStatuses: PlanVersionStatus[];
	statuses: PlanStatusEnum[];
	accessTypes: PlanAccessType[];
	versions: Number[];
	groupIds: Guid[];
	
	tenantSubQuery: TenantLookup;
	planUserSubQuery: PlanUserLookup;
	planBlueprintSubQuery: PlanBlueprintLookup;
	planDescriptionTemplateSubQuery: PlanDescriptionTemplateLookup;
	planReferenceSubQuery: PlanReferenceLookup;

	constructor() {
		super();
	}
}

export interface PlanFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	versionStatuses: PlanVersionStatus[];
	statuses: PlanStatusEnum[];
	accessTypes: PlanAccessType[];
	versions: Number[];
	groupIds: Guid[];

	planUserSubQuery: PlanUserLookup;
	planBlueprintSubQuery: PlanBlueprintLookup;
	planDescriptionTemplateSubQuery: PlanDescriptionTemplateLookup;
	planReferenceSubQuery: PlanReferenceLookup;
}
