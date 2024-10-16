import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';
import { DescriptionStatusEnum } from '../common/enum/description-status';
import { PlanLookup } from './plan.lookup';
import { DescriptionReferenceLookup } from './reference.lookup';
import { DescriptionTagLookup } from './tag.lookup';
import { DescriptionTemplateLookup } from './description-template.lookup';
import { TenantLookup } from './tenant.lookup';

export class DescriptionLookup extends Lookup implements DescriptionFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	createdAfter: Date;
	createdBefore: Date;
	finalizedAfter: Date;
	finalizedBefore: Date;
	isActive: IsActive[];
	statuses: DescriptionStatusEnum[];
	
	planSubQuery: PlanLookup;
	tenantSubQuery: TenantLookup;
	descriptionTemplateSubQuery: DescriptionTemplateLookup;
	descriptionTagSubQuery: DescriptionTagLookup;
	descriptionReferenceSubQuery: DescriptionReferenceLookup;
	
	constructor() {
		super();
	}
}

export interface DescriptionFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	createdAfter: Date;
	createdBefore: Date;
	finalizedAfter: Date;
	finalizedBefore: Date;
	isActive: IsActive[];
	statuses: DescriptionStatusEnum[];

	planSubQuery: PlanLookup;
	descriptionTemplateSubQuery: DescriptionTemplateLookup;
	descriptionTagSubQuery: DescriptionTagLookup;
	descriptionReferenceSubQuery: DescriptionReferenceLookup;
}

export class ReferencesWithType {
	referenceTypeId?: Guid;
	referenceIds?: Guid[];

	constructor() {
	}
}
