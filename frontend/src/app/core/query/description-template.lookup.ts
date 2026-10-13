import { Lookup } from '@common/model/lookup';
import { Guid } from '@common/types/guid';
import { DescriptionTemplateStatus } from '../common/enum/description-template-status';
import { IsActive } from '../common/enum/is-active.enum';
import { DescriptionTemplateVersionStatus } from '../common/enum/description-template-version-status';
import { TenantLookup } from './tenant.lookup';

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
	languages: string[];
	onlyCanEdit: boolean;
	tenantSubQuery: TenantLookup;
	/** false: only the default tenant's shared rows; true: only tenant-owned rows; absent: both. */
	tenantIsSet?: boolean;
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
	languages: string[];
	onlyCanEdit: boolean;
	tenantSubQuery: TenantLookup;
}
