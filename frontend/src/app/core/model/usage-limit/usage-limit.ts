import { UsageLimitPeriodicityRange } from "@app/core/common/enum/usage-limit-periodicity-range";
import { UsageLimitTargetMetric } from "@app/core/common/enum/usage-limit-target-metric";
import { BaseEntity, BaseEntityPersist } from "@common/base/base-entity.model";

export interface UsageLimit extends BaseEntity {
	label: string;
	targetMetric: UsageLimitTargetMetric;
	value: number;
	definition: UsageLimitDefinition;
}

export interface UsageLimitDefinition {
	hasPeriodicity: Boolean;
	periodicityRange: UsageLimitPeriodicityRange
}

// persist

export interface UsageLimitPersist extends BaseEntityPersist {
	label: string;
	targetMetric: UsageLimitTargetMetric;
	value: number;
	definition: UsageLimitDefinitionPersist;
}

export interface UsageLimitDefinitionPersist {
	hasPeriodicity: Boolean;
	periodicityRange: UsageLimitPeriodicityRange
}