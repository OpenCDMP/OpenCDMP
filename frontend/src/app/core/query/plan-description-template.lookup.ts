import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { IsActive } from '../common/enum/is-active.enum';

export class PlanDescriptionTemplateLookup extends Lookup implements PlanDescriptionTemplateFilter {
	ids: Guid[];
	excludedIds: Guid[];
	planIds: Guid[];
	descriptionTemplateGroupIds: Guid[];
	sectionIds: Guid[];
	isActive: IsActive[];


	constructor() {
		super();
	}
}

export interface PlanDescriptionTemplateFilter {
	ids: Guid[];
	excludedIds: Guid[];
	planIds: Guid[];
	descriptionTemplateGroupIds: Guid[];
	sectionIds: Guid[];
	isActive: IsActive[];
}
