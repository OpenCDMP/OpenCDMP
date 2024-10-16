import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { DescriptionTemplateStatus } from '../common/enum/description-template-status';
import { IsActive } from '../common/enum/is-active.enum';
import { DescriptionTemplateVersionStatus } from '../common/enum/description-template-version-status';

export class DescriptionTemplateLookup extends Lookup implements DescriptionTemplateFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	typeIds: Guid[];
	statuses: DescriptionTemplateStatus[];
	groupIds: Guid[];
	excludedGroupIds: Guid[];
	versionStatuses: DescriptionTemplateVersionStatus[];
	onlyCanEdit: boolean;
	constructor() {
		super();
	}
}

export interface DescriptionTemplateFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	typeIds: Guid[];
	statuses: DescriptionTemplateStatus[];
	groupIds: Guid[];
	excludedGroupIds: Guid[];
	versionStatuses: DescriptionTemplateVersionStatus[];
	onlyCanEdit: boolean;

}
