import { Lookup } from "@common/model/lookup";
import { RecentActivityOrder } from "../common/enum/recent-activity-order";
import { Guid } from "@common/types/guid";
import { PlanLookup } from "./plan.lookup";
import { DescriptionLookup } from "./description.lookup";

export class RecentActivityItemLookup implements RecentActivityItemFilter {
	like: string;
	onlyDraft: boolean;
	onlyPlan: boolean;
	onlyDescription: boolean;
	userIds: string;
	statusIds: Guid[];
	/** The My Plans filter panel's lookup; only its filter parts are applied. */
	planFilter: PlanLookup;
	/** The My Assets filter panel's lookup; only its filter parts are applied. */
	descriptionFilter: DescriptionLookup;
	page: Lookup.Paging;
	project: Lookup.FieldDirectives;
	order: Order;

	constructor() {
	}
}


export interface RecentActivityItemFilter {
	like: string;
	onlyDraft: boolean;
	onlyPlan: boolean;
	onlyDescription: boolean;
	userIds: string;
	statusIds: Guid[];
	/** The My Plans filter panel's lookup; only its filter parts are applied. */
	planFilter: PlanLookup;
	/** The My Assets filter panel's lookup; only its filter parts are applied. */
	descriptionFilter: DescriptionLookup;
	page: Lookup.Paging;
	project: Lookup.FieldDirectives;
	order: Order;
}

export interface Order {
	orderField: RecentActivityOrder;
	sortDirection: SortDirection;
}

export enum SortDirection {
	ASC,
	DESC
}