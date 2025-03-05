import { RankType } from "./rank-type";
import { EvaluatorEntityType } from "@app/core/common/enum/evaluator-entity-type";
import { RankConfig } from "./rank-config";

export class EvaluatorConfiguration{
    evaluatorId: string;
    rankType: RankType[];
    evaluatorEntityTypes: EvaluatorEntityType[];
    rankConfig: RankConfig[];
    hasLogo: boolean;
}