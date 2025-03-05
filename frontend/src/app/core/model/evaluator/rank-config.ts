import { RankType } from "./rank-type";
import { ValueRangeConfiguration } from "./evaluator-value-range";
import { SelectionConfiguration } from "./evaluator-selection";

export class RankConfig{
  rankType: RankType;
  valueRangeConfiguration?: ValueRangeConfiguration;
  selectionConfiguration?: SelectionConfiguration;
}