import { BaseEntity } from "@common/base/base-entity.model";
import { BaseReference, Reference } from "../reference/reference";
import { Plan, PublicPlan } from "./plan";
import { Guid } from "@common/types/guid";

export interface PlanReference extends BaseEntity {
	plan: Plan;
	reference: Reference;
	data?: PlanReferenceData;
}

export interface PlanReferenceData {
	blueprintFieldId: Guid;
}