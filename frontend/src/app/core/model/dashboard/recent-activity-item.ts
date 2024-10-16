import { RecentActivityItemType } from "@app/core/common/enum/recent-activity-item-type";
import { Description } from "../description/description";
import { Plan } from "../plan/plan";

export interface RecentActivityItem {
	type: RecentActivityItemType;
	plan: Plan;
	description: Description;
}
