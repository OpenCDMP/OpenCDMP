import { EvaluatorEntityType } from "@app/core/common/enum/evaluator-entity-type";
import { RankType } from "./rank-type";
import { SelectionConfiguration } from "./evaluator-selection";
import { ValueRangeConfiguration } from "./evaluator-value-range";

export interface EvaluatorFormat {
	rankType: RankType[];
	selectionConfiguration: SelectionConfiguration;
	valueRangeConfiguration: ValueRangeConfiguration;
	evaluatorId: string;
	entityTypes: EvaluatorEntityType[];
}