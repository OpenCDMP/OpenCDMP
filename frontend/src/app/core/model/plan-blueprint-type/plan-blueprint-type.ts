import { PlanBlueprintTypeStatus } from "@app/core/common/enum/plan-blueprint-type-status";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";

export interface PlanBlueprintType extends BaseEntity {
	name: string;
	code: string;
	status: PlanBlueprintTypeStatus;
}

export interface PlanBlueprintTypePersist extends BaseEntityPersist {
	name: string;
	code: string;
	status: PlanBlueprintTypeStatus;
}