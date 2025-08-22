import { Guid } from "@common/types/guid";
import { RankResultModel } from "../evaluator/evaluator-plan-model.model";
import { RankConfig } from "../evaluator/rank-config";

export interface EvaluationData {
    evaluatorId: Guid;
    rankConfig: RankConfig;
    rankResult: RankResultModel;
}