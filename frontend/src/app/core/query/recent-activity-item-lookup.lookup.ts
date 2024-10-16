import { Lookup } from "@common/model/lookup";
import { RecentActivityOrder } from "../common/enum/recent-activity-order";

export class RecentActivityItemLookup implements RecentActivityItemFilter {
	like: string;
	onlyDraft: boolean;
	onlyPlan: boolean;
	onlyDescription: boolean;
	userIds: string;
	page: Lookup.Paging;
	project: Lookup.FieldDirectives;
	orderField: RecentActivityOrder;

	constructor() {
	}
}


export interface RecentActivityItemFilter {
	like: string;
	onlyDraft: boolean;
	onlyPlan: boolean;
	onlyDescription: boolean;
	userIds: string;
	page: Lookup.Paging;
	project: Lookup.FieldDirectives;
	orderField: RecentActivityOrder;
}