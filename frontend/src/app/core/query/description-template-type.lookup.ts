import { Lookup } from "@common/model/lookup";
import { Guid } from "@common/types/guid";
import { IsActive } from "../common/enum/is-active.enum";
import { DescriptionTemplateTypeStatus } from "../common/enum/description-template-type-status";

export class DescriptionTemplateTypeLookup extends Lookup implements DescriptionTemplateTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: DescriptionTemplateTypeStatus[];

	constructor() {
		super();
	}
}

export interface DescriptionTemplateTypeFilter {
	ids: Guid[];
	excludedIds: Guid[];
	like: string;
	isActive: IsActive[];
	statuses: DescriptionTemplateTypeStatus[];
}