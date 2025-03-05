import { Guid } from "@common/types/guid";
import { RankModel } from "../evaluator/evaluator-plan-model.model";
import { RankConfig } from "../evaluator/rank-config";

export interface EvaluationData {
    evaluatorId: Guid;
    rankConfig: RankConfig;
    rankModel: RankModel;
}