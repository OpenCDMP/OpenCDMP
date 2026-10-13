import { PluginEntityType } from "@app/core/common/enum/plugin-entity-type";
import { Guid } from "@common/types/guid";

export class RankResultModel {
    rank: number;
    details: string;
    results: EvaluationResultModel[];
    /**
     * Which part of the evaluated entity this result is about. Absent for evaluators that
     * return a single result for the whole entity, and for evaluations recorded before
     * evaluators could report it.
     */
    target?: EvaluationTargetModel;
}

export class EvaluationTargetModel {
    entityType: PluginEntityType;
    entityId: Guid;
    label: string;
}

export class EvaluationResultModel {
    rank: number;
    benchmarkTitle: string;
    benchmarkDetails: string;
    metrics: EvaluationResultMetricModel[];
}

export class EvaluationResultMetricModel {
    rank: number;
    metricTitle: string;
    metricDetails: string;
    messages: EvaluationResultMessageModel[];
}


export class EvaluationResultMessageModel {
    title?: string;
    message: string;
}
